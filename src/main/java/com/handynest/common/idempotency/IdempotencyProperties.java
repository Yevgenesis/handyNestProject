package com.handynest.common.idempotency;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.idempotency")
public class IdempotencyProperties {

    private Duration requestTtl = Duration.ofHours(24);
    private Duration cleanupInterval = Duration.ofHours(1);
}
