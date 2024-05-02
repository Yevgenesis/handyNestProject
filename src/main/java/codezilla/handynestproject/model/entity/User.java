package codezilla.handynestproject.model.entity;

import codezilla.handynestproject.model.entity.enums.Rating;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "\"user\"")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * Email используется для авторизации как Username
     */

    @Column(name = "email", updatable = false, nullable = false, length = 50, unique = true)
    private String email;

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified;

    @Column(name = "password", nullable = false, length = 50)
    private String password;

    @CreatedDate
    @Column(name = "created_on", nullable = false, updatable = false)
    private Timestamp created_on;

    @LastModifiedDate
    @Column(name = "updated_on", nullable = false)
    private Timestamp updated_on;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "is_phone_verified")
    private boolean isPhoneVerified;

    @Column(name = "is_passport_verified")
    private boolean isPassportVerified;

    @Column(name = "rating")
    @Enumerated(EnumType.STRING)
    private Rating rating;

    @Column(name = "country", length = 70)
    private String country;

    @Column(name = "city", length = 70)
    private String city;


    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Task> tasks = new HashSet<>();


    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Attachment> attachments = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn (name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToMany(
            mappedBy = "sender_id",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Feedback> sentFeedbacks = new HashSet<>();


    @OneToMany(
            mappedBy = "receiver_id",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Feedback> receivedFeedbacks = new HashSet<>();

}
