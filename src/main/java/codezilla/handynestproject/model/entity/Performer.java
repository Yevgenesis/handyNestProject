package codezilla.handynestproject.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
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
import java.util.HashSet;
import java.util.Set;


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "performer")
@NamedEntityGraph(name = "Performer.withUserAndCategoriesAndAddress", attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode("categories"),
        @NamedAttributeNode("address")
}
)
@EntityListeners(AuditingEntityListener.class)
public class Performer {

    @Id
    @Schema(name = "Performer id",example = "1", required = true)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @MapsId
    @Schema(description = "User id from which the performer was created", example = "1", required = true)
    private User user;

    @Schema(name = "Performer phone number",example = "+4912345678978", required = true)
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Schema(description = "Indicates if the phone number is verified", example = "true")
    @Column(name = "is_phone_verified")
    private boolean isPhoneVerified;

    @Schema(description = "Indicates if the passport is verified", example = "true")
    @Column(name = "is_passport_verified")
    private boolean isPassportVerified;

    @Schema(description = "Description of the performer", example = "Experienced plumber")
    @Column(name = "description")
    private String description;


    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "performer_categories",
            joinColumns = @JoinColumn(name = "performer_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "id")
    )
    @Schema(description = "Categories associated with the performer")
    private Set<Category> categories = new HashSet<>();


    @OneToOne(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Schema(description = "Address of the performer")
    private Address address;

    @OneToMany(
            mappedBy = "performer",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Schema(description = "Tasks assigned to the performer")
    private Set<Task> tasks = new HashSet<>();

    @Schema(description = "Indicates if the performer is available", example = "true")
    @Column(name = "is_available")
    private boolean isAvailable;

    @Schema(description = "Positive feedback percentage of the performer", example = "0.0")
    @Column(name = "performer_rating")
    @Builder.Default
    private Double positiveFeedbackPercent = 0.0;

    @Builder.Default
    @Column(name = "task_count", nullable = false)
    @Schema(description = "Number of tasks completed by the performer", example = "0")
    private Long taskCount = 0L;

    @Schema(description = "Date when the performer was created", example = "2024-01-01T00:00:00.000Z")
    @CreatedDate
    @Column(name = "created_on", updatable = false, nullable = false)
    private Timestamp createdOn;

    @LastModifiedDate
    @Column(name = "updated_on", nullable = false)
    @Schema(description = "Date when the performer was last updated", example = "2024-01-01T00:00:00.000Z")
    private Timestamp updatedOn;

    public void increaseTaskCounter() {
        this.taskCount++;
    }

    //     установка значений по умолчанию при добавлении в базу
//    @PrePersist
//    public void prePersist() {
//        this.createdOn = new Timestamp(System.currentTimeMillis());
//        this.updatedOn = new Timestamp(System.currentTimeMillis());
//        this.isAvailable = true;
//        this.feedbackCount = 0L;
//        this.positiveFeedbackPercent = 0.0;
//    }

    //     установка значений при обновлении
//    @PreUpdate
//    public void preUpdate() {
//        this.updatedOn = new Timestamp(System.currentTimeMillis());
//    }


}





