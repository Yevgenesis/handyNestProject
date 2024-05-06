package codezilla.handynestproject.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "performer")
public class Performer {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "is_phone_verified")
    private boolean isPhoneVerified;

    @Column(name = "is_passport_verified")
    private boolean isPassportVerified;

    @Column(name = "description")
    private String description;


    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "performer_categories",
            joinColumns = @JoinColumn(name = "performer_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "id")
    )
    private Set<Category> categories = new HashSet<>();


    @OneToOne(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private Address address;

    @OneToMany(
            mappedBy = "performer",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<Task> tasks = new HashSet<>();

    @Column(name = "is_available")
    boolean isAvailable;

    @Column(name = "performer_rating")
    private Double positiveFeedbackPercent;

    @Column(name = "feedback_count")
    private Long feedbackCount;

    @CreatedDate
    @Column(name = "created_on", nullable = false, updatable = false)
    private Timestamp createdOn;

    @LastModifiedDate
    @Column(name = "updated_on", nullable = false)
    private Timestamp updatedOn;


}





