package codezilla.handynestproject.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EntityScan
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_info")
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;


    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * Email используется для авторизации как Username
     */

    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified;

    @Column(name = "password", nullable = false, length = 50)
    private String password;

    @Column(name = "created_on", nullable = false, updatable = false)
    @CreatedDate
    private Timestamp created_on;

    @LastModifiedDate
    @Column(name = "updated_on", nullable = false)
    private Timestamp updated_on;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Customer customer;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Performer performer;


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
