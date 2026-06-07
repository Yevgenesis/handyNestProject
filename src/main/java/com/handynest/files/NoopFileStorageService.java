package com.handynest.files;

import com.handynest.marketplace.MarketplaceAttachment;
import com.handynest.marketplace.StorageProvider;
import java.time.Instant;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.storage.presigned-enabled", havingValue = "false")
public class NoopFileStorageService implements FileStorageService {

    private static final String MOCK_URL_PREFIX = "mock-storage://";
    private final FileStorageProperties properties;

    public NoopFileStorageService(FileStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public StorageProvider provider() {
        return properties.getDefaultProvider();
    }

    @Override
    public PresignedStorageUrl createUploadUrl(MarketplaceAttachment attachment) {
        return new PresignedStorageUrl(
                MOCK_URL_PREFIX + attachment.getBucket() + "/" + attachment.getStorageKey(),
                "PUT",
                Map.of("Content-Type", attachment.getContentType()),
                Instant.now().plusSeconds(600)
        );
    }

    @Override
    public PresignedStorageUrl createDownloadUrl(MarketplaceAttachment attachment) {
        return new PresignedStorageUrl(
                MOCK_URL_PREFIX + attachment.getBucket() + "/" + attachment.getStorageKey(),
                "GET",
                Map.of(),
                Instant.now().plusSeconds(300)
        );
    }
}
