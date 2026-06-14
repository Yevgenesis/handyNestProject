package com.handynest.marketplace.legacy;

import com.handynest.identity.User;
import com.handynest.performer.legacy.Performer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

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
  @Schema(description = "Chat id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @Schema(
      description = "User associated with the chat",
      example = "1",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "performer_id")
  @Schema(
      description = "Performer associated with the chat",
      example = "1",
      requiredMode = Schema.RequiredMode.REQUIRED)
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
  @Schema(description = "Chat creation timestamp", example = "2024-05-28 10:00:00")
  private Timestamp createdOn;

  @LastModifiedDate
  @Column(nullable = false)
  @Schema(description = "Chat last update timestamp", example = "2024-05-28 10:00:00")
  private Timestamp updatedOn;

  @Column(nullable = false)
  @Schema(description = "Chat deletion status", example = "false")
  private boolean isDeleted;
}
