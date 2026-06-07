package com.handynest.retention;

import com.handynest.identity.User;
import com.handynest.identity.UserRepository;
import com.handynest.identity.UserStatus;
import com.handynest.marketplace.AttachmentType;
import com.handynest.marketplace.AttachmentVisibility;
import com.handynest.marketplace.MarketplaceAttachment;
import com.handynest.marketplace.MarketplaceAttachmentRepository;
import com.handynest.marketplace.StorageProvider;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataRetentionServiceTest {

    @Mock
    private DataRetentionPolicyRepository dataRetentionPolicyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MarketplaceAttachmentRepository marketplaceAttachmentRepository;

    @Test
    void runRetentionAnonymizesDeletedUsersAndExpiresRejectedVerificationDocuments() {
        DataRetentionProperties properties = new DataRetentionProperties();
        properties.setBatchSize(20);
        properties.setDeletedUserAnonymizationDays(30);
        properties.setVerificationDocumentRetentionDays(90);

        DataRetentionService service = new DataRetentionService(
                dataRetentionPolicyRepository,
                properties,
                userRepository,
                marketplaceAttachmentRepository,
                new BCryptPasswordEncoder()
        );

        Instant now = Instant.parse("2026-06-07T12:00:00Z");
        User deletedUser = deletedUser(now.minus(31, ChronoUnit.DAYS));
        MarketplaceAttachment verificationDocument = verificationDocument(deletedUser);

        when(dataRetentionPolicyRepository.findFirstByActiveTrueOrderByIdAsc())
                .thenReturn(Optional.empty());
        when(userRepository.findAllByStatusAndDeletedAtBefore(
                eq(UserStatus.DELETED),
                any(Instant.class),
                any(Pageable.class)
        )).thenReturn(List.of(deletedUser));
        when(marketplaceAttachmentRepository.findRejectedVerificationDocumentsForRetention(
                any(Instant.class),
                any(Pageable.class)
        )).thenReturn(List.of(verificationDocument));

        DataRetentionRunResult result = service.runRetention(now);

        assertThat(result.anonymizedDeletedUsers()).isEqualTo(1);
        assertThat(result.expiredVerificationDocuments()).isEqualTo(1);
        assertThat(deletedUser.getEmail()).isEqualTo("deleted+01kaccountdeleted0000000001@deleted.handynest.local");
        assertThat(deletedUser.getFirstName()).isEqualTo("Deleted");
        assertThat(deletedUser.getPhone()).isNull();
        assertThat(verificationDocument.getDeletedAt()).isEqualTo(now);
        verify(userRepository).saveAll(List.of(deletedUser));
        verify(marketplaceAttachmentRepository).saveAll(List.of(verificationDocument));
    }

    private User deletedUser(Instant deletedAt) {
        User user = new User();
        user.setPublicId("01KACCOUNTDELETED0000000001");
        user.setEmail("person@example.kz");
        user.setPassword("encoded-password");
        user.setFirstName("Personal");
        user.setLastName("Data");
        user.setPhone("+77001234567");
        user.setPhoneVerified(true);
        user.setEmailVerified(true);
        user.setStatus(UserStatus.DELETED);
        user.setDeleted(true);
        user.setDeletedAt(deletedAt);
        return user;
    }

    private MarketplaceAttachment verificationDocument(User user) {
        return new MarketplaceAttachment(
                "01KVERIFICATIONDOC000000001",
                user,
                null,
                null,
                AttachmentType.VERIFICATION_DOCUMENT,
                StorageProvider.MINIO,
                "handynest-verification",
                "verification/user/document.pdf",
                "document.pdf",
                "application/pdf",
                1024,
                null,
                AttachmentVisibility.ADMIN_ONLY
        );
    }
}
