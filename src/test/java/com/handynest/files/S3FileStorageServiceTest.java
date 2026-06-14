package com.handynest.files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.handynest.marketplace.AttachmentType;
import com.handynest.marketplace.AttachmentVisibility;
import com.handynest.marketplace.MarketplaceAttachment;
import com.handynest.marketplace.StorageProvider;
import java.net.URI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Testcontainers
class S3FileStorageServiceTest {

  private static final String ACCESS_KEY = "handynest-test";
  private static final String SECRET_KEY = "handynest-test-secret";
  private static final String BUCKET = "handynest-cleanup-test";
  private static final String STORAGE_KEY = "retention/deleted-object.txt";

  @Container
  static final GenericContainer<?> minio =
      new GenericContainer<>(
              DockerImageName.parse("quay.io/minio/minio:RELEASE.2024-05-10T01-41-38Z"))
          .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
          .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
          .withCommand("server", "/data")
          .withExposedPorts(9000)
          .waitingFor(Wait.forHttp("/minio/health/ready").forPort(9000));

  private static S3Client client;
  private static S3FileStorageService storageService;

  @BeforeAll
  static void setUpStorage() {
    URI endpoint = URI.create("http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
    StaticCredentialsProvider credentials =
        StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY));
    client =
        S3Client.builder()
            .endpointOverride(endpoint)
            .credentialsProvider(credentials)
            .region(Region.US_EAST_1)
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
            .build();
    client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());

    FileStorageProperties properties = new FileStorageProperties();
    properties.setEndpoint(endpoint);
    properties.setAccessKey(ACCESS_KEY);
    properties.setSecretKey(SECRET_KEY);
    properties.setDefaultProvider(StorageProvider.MINIO);
    storageService = new S3FileStorageService(properties);
  }

  @AfterAll
  static void closeClients() {
    storageService.closeClients();
    client.close();
  }

  @Test
  void deletesPhysicalObjectFromMinio() {
    client.putObject(
        PutObjectRequest.builder()
            .bucket(BUCKET)
            .key(STORAGE_KEY)
            .contentType("text/plain")
            .build(),
        RequestBody.fromString("retention test"));
    MarketplaceAttachment attachment =
        new MarketplaceAttachment(
            "06TESTATTACHMENT00000000001",
            null,
            null,
            null,
            AttachmentType.CHAT_FILE,
            StorageProvider.MINIO,
            BUCKET,
            STORAGE_KEY,
            "deleted-object.txt",
            "text/plain",
            14,
            null,
            AttachmentVisibility.PARTICIPANTS_ONLY);

    storageService.deleteObject(attachment);

    assertThatThrownBy(
            () ->
                client.headObject(
                    HeadObjectRequest.builder().bucket(BUCKET).key(STORAGE_KEY).build()))
        .isInstanceOf(S3Exception.class)
        .satisfies(
            exception ->
                org.assertj.core.api.Assertions.assertThat(((S3Exception) exception).statusCode())
                    .isEqualTo(404));
  }

  @Test
  void inspectsUploadedObjectMetadata() {
    String key = "uploads/inspection.txt";
    client.putObject(
        PutObjectRequest.builder().bucket(BUCKET).key(key).contentType("text/plain").build(),
        RequestBody.fromString("inspection"));
    MarketplaceAttachment attachment =
        new MarketplaceAttachment(
            "06TESTATTACHMENT00000000002",
            null,
            null,
            null,
            AttachmentType.CHAT_FILE,
            StorageProvider.MINIO,
            BUCKET,
            key,
            "inspection.txt",
            "text/plain",
            10,
            null,
            AttachmentVisibility.PARTICIPANTS_ONLY);

    StoredObjectMetadata metadata = storageService.inspectObject(attachment);

    assertThat(metadata.exists()).isTrue();
    assertThat(metadata.sizeBytes()).isEqualTo(10);
    assertThat(metadata.contentType()).isEqualTo("text/plain");
  }
}
