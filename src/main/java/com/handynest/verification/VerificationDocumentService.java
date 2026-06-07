package com.handynest.verification;

import com.handynest.identity.User;
import com.handynest.files.FileStorageProperties;
import com.handynest.files.FileStorageService;
import com.handynest.files.PresignedStorageUrl;
import com.handynest.identity.UserProfileService;
import com.handynest.marketplace.AttachmentMetadataRequest;
import com.handynest.marketplace.AttachmentResponse;
import com.handynest.marketplace.AttachmentType;
import com.handynest.marketplace.AttachmentUploadResponse;
import com.handynest.marketplace.AttachmentVisibility;
import com.handynest.marketplace.MarketplaceAttachment;
import com.handynest.marketplace.MarketplaceAttachmentRepository;
import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.publicid.PublicIdGenerator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificationDocumentService {

    private static final long MAX_VERIFICATION_DOCUMENT_SIZE_BYTES = 10_485_760L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "bat", "cmd", "com", "dll", "exe", "js", "jar", "msi", "ps1", "scr", "sh"
    );
    private static final Pattern SAFE_FILENAME_CHARS = Pattern.compile("[^A-Za-z0-9._-]");

    private final UserProfileService userProfileService;
    private final MarketplaceAttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;
    private final FileStorageProperties fileStorageProperties;

    @Transactional
    public AttachmentUploadResponse createUploadUrl(UserDetails userDetails, AttachmentMetadataRequest request) {
        User owner = userProfileService.currentUser(userDetails);
        validateDocument(request);
        String publicId = PublicIdGenerator.defaultGenerator().newUlid();
        MarketplaceAttachment attachment = attachmentRepository.save(new MarketplaceAttachment(
                publicId,
                owner,
                null,
                null,
                AttachmentType.VERIFICATION_DOCUMENT,
                fileStorageService.provider(),
                fileStorageProperties.bucketFor(AttachmentType.VERIFICATION_DOCUMENT, AttachmentVisibility.ADMIN_ONLY),
                storageKey(owner, publicId, request.originalFilename()),
                request.originalFilename().trim(),
                request.contentType().trim().toLowerCase(Locale.ROOT),
                request.sizeBytes(),
                blankToNull(request.checksum()),
                AttachmentVisibility.ADMIN_ONLY
        ));
        PresignedStorageUrl uploadUrl = fileStorageService.createUploadUrl(attachment);
        return new AttachmentUploadResponse(
                toAttachmentResponse(attachment),
                uploadUrl.url(),
                uploadUrl.method(),
                uploadUrl.headers(),
                uploadUrl.expiresAt()
        );
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> myDocuments(UserDetails userDetails) {
        User owner = userProfileService.currentUser(userDetails);
        return attachmentRepository.findAllByOwnerIdAndAttachmentTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
                        owner.getId(),
                        AttachmentType.VERIFICATION_DOCUMENT
                )
                .stream()
                .map(this::toAttachmentResponse)
                .toList();
    }

    private void validateDocument(AttachmentMetadataRequest request) {
        String filename = blankToNull(request.originalFilename());
        String contentType = blankToNull(request.contentType());
        if (filename == null) {
            throw new BadRequestBusinessException("originalFilename is required");
        }
        if (contentType == null) {
            throw new BadRequestBusinessException("contentType is required");
        }
        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new BadRequestBusinessException("Verification document content type is not allowed");
        }
        if (request.sizeBytes() <= 0 || request.sizeBytes() > MAX_VERIFICATION_DOCUMENT_SIZE_BYTES) {
            throw new BadRequestBusinessException("Verification document size exceeds allowed limit");
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            String extension = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
            if (BLOCKED_EXTENSIONS.contains(extension)) {
                throw new BadRequestBusinessException("Verification document file type is not allowed");
            }
        }
    }

    private String storageKey(User owner, String publicId, String originalFilename) {
        return "verification-documents/"
                + owner.getPublicId()
                + "/"
                + publicId
                + "/"
                + safeFilename(originalFilename);
    }

    private String safeFilename(String originalFilename) {
        String trimmed = originalFilename == null ? "document" : originalFilename.trim();
        String safe = SAFE_FILENAME_CHARS.matcher(trimmed).replaceAll("_");
        return safe.isBlank() ? "document" : safe;
    }

    private AttachmentResponse toAttachmentResponse(MarketplaceAttachment attachment) {
        return new AttachmentResponse(
                attachment.getPublicId(),
                attachment.getOwner().getPublicId(),
                attachment.getTask() == null ? null : attachment.getTask().getPublicId(),
                attachment.getChatMessage() == null ? null : attachment.getChatMessage().getPublicId(),
                attachment.getDisputeCase() == null ? null : attachment.getDisputeCase().getPublicId(),
                attachment.getAttachmentType().name(),
                attachment.getStorageProvider().name(),
                attachment.getBucket(),
                attachment.getStorageKey(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getChecksum(),
                attachment.getVisibility().name(),
                attachment.getCreatedAt()
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
