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
})
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Entity representing a performer")
public class Performer {

    @Id
    @Schema(description = "Unique identifier of the performer", example = "1")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @MapsId
    @Schema(description = "User id from which the performer was created", example = "1")
    private User user;

    @Schema(description = "Performer phone number", example = "+4912345678978")
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "is_phone_verified")
    @Schema(description = "Indicates if the phone number is verified")
    private boolean isPhoneVerified;

    @Column(name = "is_passport_verified")
    @Schema(description = "Indicates if the passport is verified")
    private boolean isPassportVerified;

    @Column(name = "description")
    @Schema(description = "Description of the performer", example = "Experienced plumber")
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

    @Column(name = "is_available")
    @Schema(description = "Indicates if the performer is available", example = "true")
    private boolean isAvailable;

    @Column(name = "performer_rating")
    @Builder.Default
    @Schema(description = "Positive feedback percentage of the performer", example = "0.0")
    private Double positiveFeedbackPercent = 100.0;

    @Builder.Default
    @Column(name = "task_count", nullable = false)
    @Schema(description = "Number of tasks completed by the performer", example = "0")
    private Long taskCount = 0L;

    @CreatedDate
    @Column(name = "created_on", updatable = false, nullable = false)
    @Schema(description = "Date when the performer was created", example = "2024-01-01")
    private Timestamp createdOn;

    @LastModifiedDate
    @Column(name = "updated_on", nullable = false)
    @Schema(description = "Date when the performer was last updated", example = "2024-01-01")
    private Timestamp updatedOn;

    public void increaseTaskCounter() {
        this.taskCount++;
    }
}



