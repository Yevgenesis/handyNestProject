package codezilla.handynestproject.model.entity;//package codezilla.hendynestproject.model.entity;

import codezilla.handynestproject.model.enums.TaskStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;

@Entity
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "task")
@JsonInclude(JsonInclude.Include.NON_NULL)// если есть поля null, чтобы они не высвечивались.
// Например Перформер в открытом таске.
@NamedEntityGraph(name = "Task.withAddressAndCategoryAndUserAndPerformer",
                attributeNodes = {
                        @NamedAttributeNode("address"),
                        @NamedAttributeNode("category"),
                        @NamedAttributeNode("user"),
                        @NamedAttributeNode("performer")
})
@EntityListeners(AuditingEntityListener.class)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Double price;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Address address;

    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;

    @Builder.Default
    private boolean isPublish = true;

    @ManyToOne
    @JoinColumn(name = "working_time_id")
    private WorkingTime workingTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "performer_id")
    private Performer performer;


//    @OneToMany(
//            mappedBy = "task",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true
//    )
//    private Set<Feedback> feedbacks;

    @CreatedDate
    @Column(name = "created_on", updatable = false, nullable = false)
    private Timestamp createdOn;

    @LastModifiedDate
    @Column(name = "updated_on", nullable = false)
    private Timestamp updatedOn;

}
