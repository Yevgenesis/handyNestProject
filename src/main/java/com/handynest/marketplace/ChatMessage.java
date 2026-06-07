package com.handynest.marketplace;

import com.handynest.identity.User;
import com.handynest.common.domain.PublicIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "chat_message")
public class ChatMessage extends PublicIdEntity {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private DealChat chat;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 32)
    private ChatMessageType messageType;

    @Getter
    @Column(nullable = false, length = 4000)
    private String text;

    @Getter
    @Column(name = "system_code", length = 64)
    private String systemCode;

    @Getter
    @Column(name = "attachment_id")
    private Long attachmentId;

    @Getter
    @Column(name = "read_at")
    private Instant readAt;

    @Getter
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Getter
    @Column(name = "risk_flag", nullable = false)
    private boolean riskFlag;

    protected ChatMessage() {
    }

    public ChatMessage(
            DealChat chat,
            User sender,
            ChatMessageType messageType,
            String text,
            String systemCode,
            Long attachmentId,
            boolean riskFlag
    ) {
        this.chat = chat;
        this.sender = sender;
        this.messageType = messageType;
        this.text = text;
        this.systemCode = systemCode;
        this.attachmentId = attachmentId;
        this.riskFlag = riskFlag;
    }

    public void markRead(Instant readAt) {
        if (this.readAt == null) {
            this.readAt = readAt;
        }
    }
}
