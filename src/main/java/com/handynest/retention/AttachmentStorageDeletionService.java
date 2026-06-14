package com.handynest.retention;

import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.marketplace.MarketplaceAttachment;
import com.handynest.marketplace.MarketplaceAttachmentRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttachmentStorageDeletionService {

  private static final int MAX_ERROR_LENGTH = 1000;

  private final MarketplaceAttachmentRepository attachmentRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markSucceeded(Long attachmentId, Instant now) {
    find(attachmentId).markStorageDeletionSucceeded(now);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markFailed(Long attachmentId, Exception exception) {
    String message =
        exception.getMessage() == null
            ? exception.getClass().getSimpleName()
            : exception.getMessage();
    find(attachmentId)
        .markStorageDeletionFailed(
            message.substring(0, Math.min(message.length(), MAX_ERROR_LENGTH)));
  }

  private MarketplaceAttachment find(Long attachmentId) {
    return attachmentRepository
        .findById(attachmentId)
        .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId.toString()));
  }
}
