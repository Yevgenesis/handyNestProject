package com.handynest.retention;

import com.handynest.files.FileStorageService;
import com.handynest.marketplace.MarketplaceAttachment;
import com.handynest.marketplace.MarketplaceAttachmentRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentStorageCleanupWorker {

  private final MarketplaceAttachmentRepository attachmentRepository;
  private final FileStorageService fileStorageService;
  private final AttachmentStorageDeletionService deletionService;
  private final DataRetentionProperties properties;

  @Scheduled(fixedDelayString = "#{@dataRetentionProperties.cleanupInterval.toMillis()}")
  public void deleteMarkedObjects() {
    List<MarketplaceAttachment> attachments =
        attachmentRepository.findAllByDeletedAtIsNotNullAndStorageDeletedAtIsNullOrderByIdAsc(
            PageRequest.of(0, properties.getBatchSize()));
    for (MarketplaceAttachment attachment : attachments) {
      delete(attachment);
    }
  }

  private void delete(MarketplaceAttachment attachment) {
    try {
      fileStorageService.deleteObject(attachment);
      deletionService.markSucceeded(attachment.getId(), Instant.now());
    } catch (Exception exception) {
      deletionService.markFailed(attachment.getId(), exception);
      log.warn("Storage deletion failed for attachment {}", attachment.getPublicId());
    }
  }
}
