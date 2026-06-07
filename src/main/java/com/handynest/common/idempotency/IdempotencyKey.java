package com.handynest.common.idempotency;

import com.handynest.identity.User;
import com.handynest.common.domain.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "idempotency_key")
public class IdempotencyKey extends BaseAuditEntity {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Getter
    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String key;

    @Getter
    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Getter
    @Column(name = "response_status")
    private Integer responseStatus;

    @Getter
    @Column(name = "response_body_hash", length = 64)
    private String responseBodyHash;

    @Getter
    @Column(name = "response_resource_type", length = 80)
    private String responseResourceType;

    @Getter
    @Column(name = "response_resource_id", length = 80)
    private String responseResourceId;

    @Getter
    @Column(name = "target_endpoint", nullable = false, length = 300)
    private String targetEndpoint;

    @Getter
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected IdempotencyKey() {
    }

    public IdempotencyKey(User user, String key, String requestHash, String targetEndpoint, Instant expiresAt) {
        this.user = user;
        this.key = key;
        this.requestHash = requestHash;
        this.targetEndpoint = targetEndpoint;
        this.expiresAt = expiresAt;
    }

    public void complete(
            int responseStatus,
            String responseBodyHash,
            String responseResourceType,
            String responseResourceId
    ) {
        this.responseStatus = responseStatus;
        this.responseBodyHash = responseBodyHash;
        this.responseResourceType = responseResourceType;
        this.responseResourceId = responseResourceId;
    }
}
