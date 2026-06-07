package com.handynest.files;

import com.handynest.marketplace.AttachmentType;
import com.handynest.marketplace.AttachmentVisibility;
import com.handynest.marketplace.StorageProvider;
import java.net.URI;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage")
public class FileStorageProperties {

    private StorageProvider defaultProvider = StorageProvider.MINIO;
    private String publicBucket = "handynest-public";
    private String participantsBucket = "handynest-private";
    private String verificationBucket = "handynest-verification";
    private URI endpoint = URI.create("http://localhost:9000");
    private String region = "us-east-1";
    private String accessKey;
    private String secretKey;
    private boolean pathStyleAccess = true;
    private boolean presignedEnabled = true;
    private Duration uploadUrlTtl = Duration.ofMinutes(10);
    private Duration downloadUrlTtl = Duration.ofMinutes(5);

    public String bucketFor(AttachmentType attachmentType, AttachmentVisibility visibility) {
        if (attachmentType == AttachmentType.VERIFICATION_DOCUMENT || visibility == AttachmentVisibility.ADMIN_ONLY) {
            return verificationBucket;
        }
        if (visibility == AttachmentVisibility.PUBLIC) {
            return publicBucket;
        }
        return participantsBucket;
    }
}
