package codezilla.handynestproject.model.entity;

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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    private String text;

    private Long grade;

    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;

    @CreatedDate
    @Column(name = "created_on", updatable = false, nullable = false)
    private Timestamp createdOn;


}
