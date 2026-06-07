package com.handynest.files;

import com.handynest.marketplace.MarketplaceAttachment;
import com.handynest.marketplace.StorageProvider;
import java.time.Instant;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@ConditionalOnProperty(name = "app.storage.presigned-enabled", havingValue = "true", matchIfMissing = true)
public class S3FileStorageService implements FileStorageService {

    private final FileStorageProperties properties;
    private final S3Presigner presigner;

    public S3FileStorageService(FileStorageProperties properties) {
        this.properties = properties;
        if (isBlank(properties.getAccessKey()) || isBlank(properties.getSecretKey())) {
            throw new IllegalStateException("Storage access key and secret key must be configured");
        }
        this.presigner = S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        properties.getAccessKey(),
                        properties.getSecretKey()
                )))
                .endpointOverride(properties.getEndpoint())
                .region(Region.of(properties.getRegion()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(properties.isPathStyleAccess())
                        .build())
                .build();
    }

    @Override
    public StorageProvider provider() {
        return properties.getDefaultProvider();
    }

    @Override
    public PresignedStorageUrl createUploadUrl(MarketplaceAttachment attachment) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(attachment.getBucket())
                .key(attachment.getStorageKey())
                .contentType(attachment.getContentType())
                .contentLength(attachment.getSizeBytes())
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(properties.getUploadUrlTtl())
                .putObjectRequest(putObjectRequest)
                .build();
        var presignedRequest = presigner.presignPutObject(presignRequest);
        return new PresignedStorageUrl(
                presignedRequest.url().toString(),
                presignedRequest.httpRequest().method().name(),
                Map.of("Content-Type", attachment.getContentType()),
                Instant.now().plus(properties.getUploadUrlTtl())
        );
    }

    @Override
    public PresignedStorageUrl createDownloadUrl(MarketplaceAttachment attachment) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(attachment.getBucket())
                .key(attachment.getStorageKey())
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(properties.getDownloadUrlTtl())
                .getObjectRequest(getObjectRequest)
                .build();
        var presignedRequest = presigner.presignGetObject(presignRequest);
        return new PresignedStorageUrl(
                presignedRequest.url().toString(),
                presignedRequest.httpRequest().method().name(),
                Map.of(),
                Instant.now().plus(properties.getDownloadUrlTtl())
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
