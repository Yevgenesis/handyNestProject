package codezilla.handynestproject.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.sql.Timestamp;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat")
@Schema(description = "Entity representing a chat")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "chat id", example = "1", required = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Schema(description = "User associated with the chat", example = "1", required = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_id")
    @Schema(description = "Performer associated with the chat", example = "1", required = true)
    private Performer performer;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Messages in the chat")
    private Set<Message> messages;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id")
    @Schema(description = "Task associated with the chat", example = "1")
    private Task task;

    @CreatedDate
    @Column(nullable = false)
    @Schema(description = "chat creation timestamp", example = "2024-05-28 10:00:00")
    private Timestamp createdOn;

    @LastModifiedDate
    @Column(nullable = false)
    @Schema(description = "chat last update timestamp", example = "2024-05-28 10:00:00")
    private Timestamp updatedOn;

    @Column(nullable = false)
    @Schema(description = "chat deletion status", example = "false")
    private boolean isDeleted;
}