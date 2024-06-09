package codezilla.handynestproject.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_messages")
@NamedEntityGraph(name = "MessageWithSenderAndChat", attributeNodes = {
        @NamedAttributeNode("sender"),
        @NamedAttributeNode("chat")
})
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Entity representing a message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Message id", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @Schema(description = "Sender id", example = "1")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    @Schema(description = "chat id", example = "1")
    private Chat chat;

    @Column(nullable = false)
    @Schema(description = "Message text", example = "Hello!")
    private String text;

    @CreatedDate
    @Column(nullable = false)
    @Schema(description = "Message creation timestamp", example = "2024-05-28")
    private Timestamp createdOn;

    @LastModifiedDate
    @Column(nullable = false)
    @Schema(description = "Message last update timestamp", example = "2024-05-28")
    private Timestamp updatedOn;

    @Column(nullable = false)
    @Schema(description = "Read status of the message", example = "false")
    private boolean isRead;
}
