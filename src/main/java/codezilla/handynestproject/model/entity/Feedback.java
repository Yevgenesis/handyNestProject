package codezilla.handynestproject.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "feedback")
@NamedEntityGraph(name = "FeedbackWithUserAndTask", attributeNodes = {
        @NamedAttributeNode("sender"),
        @NamedAttributeNode("task")
})
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Entity representing a feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Feedback id", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    @Schema(description = "Sender id", example = "1")
    private User sender;

    @Schema(description = "Feedback text", example = "отличная работа!")
    private String text;

    @Schema(description = "Feedback grade", example = "100")
    private Long grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @Schema(description = "Task to which feedback is added", example = "1")
    private Task task;

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    @Column(name = "created_on", updatable = false, nullable = false)
    @Schema(description = "Feedback creation timestamp", example = "2024-01-01")
    private Timestamp createdOn;
}