package codezilla.handynestproject.model.entity;//package codezilla.hendynestproject.model.entity;

import codezilla.handynestproject.model.enums.TaskStatus;
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
import java.util.Set;

@Entity
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "task")
@NamedEntityGraph(name = "Task.withAddressAndCategoryAndUserAndPerformer",
        attributeNodes = {
                @NamedAttributeNode("address"),
                @NamedAttributeNode("category"),
                @NamedAttributeNode("user"),
                @NamedAttributeNode("performer")
        })
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Entity representing a task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the task", example = "1")
    private Long id;

    @Schema(description = "Title of the task", example = "Fix the sink")
    private String title;

    @Schema(description = "Description of the task", example = "The sink is leaking and needs to be fixed")
    private String description;

    @Schema(description = "Price for the task", example = "50.0")
    private Double price;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Schema(description = "Address where the task is located")
    private Address address;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Status of the task", example = "OPEN")
    private TaskStatus taskStatus;

    @Builder.Default
    @Schema(description = "Indicates if the task is published", example = "true")
    private boolean isPublish = true;

    @ManyToOne
    @JoinColumn(name = "working_time_id")
    @Schema(description = "Working time associated with the task")
    private WorkingTime workingTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @Schema(description = "Category of the task")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @Schema(description = "User who created the task")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @Schema(description = "Performer assigned to the task")
    private Performer performer;

    @OneToMany(
            mappedBy = "task",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Schema(description = "Feedbacks associated with the task")
    private Set<Feedback> feedbacks;


    @CreatedDate
    @Column(name = "created_on", updatable = false, nullable = false)
    @Schema(description = "Date when the task was created", example = "2024-01-01")
    private Timestamp createdOn;

    @LastModifiedDate
    @Column(name = "updated_on", nullable = false)
    @Schema(description = "Date when the task was last updated", example = "2024-01-01")
    private Timestamp updatedOn;
}