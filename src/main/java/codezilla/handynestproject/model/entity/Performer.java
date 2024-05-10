package codezilla.handynestproject.model.entity;

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
}
)
//@EqualsAndHashCode(exclude = {"tasks", "address", "categories","user"})
//@ToString(exclude = {"tasks", "address", "categories","user"})
@EntityListeners(AuditingEntityListener.class)
public class Performer {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id")
    @MapsId
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
    private boolean isAvailable;

    @Column(name = "performer_rating")
    @Builder.Default
    private Double positiveFeedbackPercent = 0.0;

    @Column(name = "feedback_count")
    private Long feedbackCount;


    @CreatedDate
    @Column(name = "created_on", updatable = false, nullable = false)
    private Timestamp createdOn;

    // ToDo BAG - updatedOn не обновляется при обновлении
    @LastModifiedDate
    @Column(name = "updated_on", nullable = false)
    private Timestamp updatedOn;


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





