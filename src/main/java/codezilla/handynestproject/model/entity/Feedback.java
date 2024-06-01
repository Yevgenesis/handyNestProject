package codezilla.handynestproject.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
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

import static jakarta.persistence.GenerationType.IDENTITY;

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
public class Feedback {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Schema(name = "Feedback id",example = "1", required = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    @Schema(name = "Sender id",example = "1", required = true)
    private User sender;

    @Schema(name = "Feedback text",example = "отличная работа!", required = true)
    private String text;
    @Schema(name = "Feedback grade",example = "100", required = true)
    private Long grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @Schema(name = "Task to which added feedback ",example = "1", required = true)
    private Task task;

    @CreatedDate
    @Column(name = "created_on", updatable = false, nullable = false)
    @Schema(name = "When feedback was created",example = "01.01.2024", required = true)
    private Timestamp createdOn;

    //feedback | user_id | task_id |user_type (user, performer)


}
