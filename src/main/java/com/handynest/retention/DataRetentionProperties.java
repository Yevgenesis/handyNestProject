package com.handynest.retention;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.data-retention")
public class DataRetentionProperties {

    private Duration cleanupInterval = Duration.ofHours(24);
    private int batchSize = 100;
    private int verificationDocumentRetentionDays = 90;
    private int deletedUserAnonymizationDays = 30;
    private int chatRetentionDays = 1095;
    private int auditLogRetentionDays = 1825;
    private int attachmentRetentionDays = 365;
}
