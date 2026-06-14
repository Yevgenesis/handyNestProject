package com.handynest.files;

import com.handynest.marketplace.MarketplaceAttachment;
import com.handynest.marketplace.StorageProvider;

public interface FileStorageService {

  StorageProvider provider();

  PresignedStorageUrl createUploadUrl(MarketplaceAttachment attachment);

  PresignedStorageUrl createDownloadUrl(MarketplaceAttachment attachment);

  StoredObjectMetadata inspectObject(MarketplaceAttachment attachment);

  void deleteObject(MarketplaceAttachment attachment);
}
