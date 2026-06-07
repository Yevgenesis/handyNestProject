package com.handynest.retention;

import com.handynest.identity.User;
import com.handynest.identity.UserRepository;
import com.handynest.common.publicid.PublicIdGenerator;
import com.handynest.identity.UserStatus;
import com.handynest.marketplace.MarketplaceAttachment;
import com.handynest.marketplace.MarketplaceAttachmentRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DataRetentionService {

    private static final String ANONYMIZED_EMAIL_DOMAIN = "@deleted.handynest.local";

    private final DataRetentionPolicyRepository dataRetentionPolicyRepository;
    private final DataRetentionProperties dataRetentionProperties;
    private final UserRepository userRepository;
    private final MarketplaceAttachmentRepository marketplaceAttachmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Scheduled(fixedDelayString = "#{@dataRetentionProperties.cleanupInterval.toMillis()}")
    @Transactional
    public void runScheduledRetention() {
        runRetention(Instant.now());
    }

    @Transactional
    public DataRetentionRunResult runRetention(Instant now) {
        RetentionPolicySnapshot policy = activePolicy();
        long anonymizedUsers = anonymizeDeletedUsers(
                now,
                policy.deletedUserAnonymizationDays()
        );
        long expiredVerificationDocuments = expireRejectedVerificationDocuments(
                now,
                policy.verificationDocumentRetentionDays()
        );
        return new DataRetentionRunResult(anonymizedUsers, expiredVerificationDocuments);
    }

    private long anonymizeDeletedUsers(Instant now, int retentionDays) {
        Instant deletedBefore = now.minus(retentionDays, ChronoUnit.DAYS);
        List<User> users = userRepository.findAllByStatusAndDeletedAtBefore(
                UserStatus.DELETED,
                deletedBefore,
                PageRequest.of(0, dataRetentionProperties.getBatchSize())
        );

        List<User> changed = new ArrayList<>();
        for (User user : users) {
            if (isAnonymized(user)) {
                continue;
            }
            anonymize(user);
            changed.add(user);
        }

        userRepository.saveAll(changed);
        return changed.size();
    }

    private long expireRejectedVerificationDocuments(Instant now, int retentionDays) {
        Instant reviewedBefore = now.minus(retentionDays, ChronoUnit.DAYS);
        List<MarketplaceAttachment> documents =
                marketplaceAttachmentRepository.findRejectedVerificationDocumentsForRetention(
                        reviewedBefore,
                        PageRequest.of(0, dataRetentionProperties.getBatchSize())
                );

        documents.forEach(document -> document.markDeleted(now));
        marketplaceAttachmentRepository.saveAll(documents);
        return documents.size();
    }

    private RetentionPolicySnapshot activePolicy() {
        return dataRetentionPolicyRepository.findFirstByActiveTrueOrderByIdAsc()
                .map(policy -> new RetentionPolicySnapshot(
                        policy.getVerificationDocumentRetentionDays(),
                        policy.getDeletedUserAnonymizationDays()
                ))
                .orElseGet(() -> new RetentionPolicySnapshot(
                        dataRetentionProperties.getVerificationDocumentRetentionDays(),
                        dataRetentionProperties.getDeletedUserAnonymizationDays()
                ));
    }

    private void anonymize(User user) {
        String publicId = user.getPublicId().toLowerCase(Locale.ROOT);
        user.setEmail("deleted+" + publicId + ANONYMIZED_EMAIL_DOMAIN);
        user.setPassword(passwordEncoder.encode(PublicIdGenerator.defaultGenerator().newUlid()));
        user.setFirstName("Deleted");
        user.setLastName("User");
        user.setPhone(null);
        user.setPhoneVerified(false);
        user.setEmailVerified(false);
        user.setLogo(null);
        user.setAvatarAttachmentId(null);
        user.setCountry(null);
        user.setCity(null);
        user.setDistrict(null);
        user.setPreferredServiceRadiusKm(null);
    }

    private boolean isAnonymized(User user) {
        return user.getEmail() != null
                && user.getEmail().startsWith("deleted+")
                && user.getEmail().endsWith(ANONYMIZED_EMAIL_DOMAIN);
    }

    private record RetentionPolicySnapshot(
            int verificationDocumentRetentionDays,
            int deletedUserAnonymizationDays
    ) {
    }
}
