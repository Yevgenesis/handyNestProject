package codezilla.handynestproject.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "\"user\"")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Для всех пользователей
     */

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

    @Transient
    private String passwordConfirm;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDate created_on;

    @Column(name = "updated_on", nullable = false, updatable = false)
    private LocalDate updated_on;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;


    /**
     * Только для исполнителей
     */

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "is_phone_verified", nullable = false)
    private boolean isPhoneVerified;

    @Column(name = "is_passport_verified", nullable = false)
    private boolean isPassportVerified;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "country", nullable = false, length = 70)
    private String country;

    @Column(name = "city", nullable = false, length = 70)
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

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"
            ))
    private Collection<Role> roles;

    public void addTask(Task task) {
        this.tasks.add(task);
    }
    public void removeTask(Task task) {
        this.tasks.remove(task);
    }

    public void addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
    }
    public void removeAttachment(Attachment attachment) {
        this.attachments.remove(attachment);
    }

    public void addFeedback(Feedback feedback) {
        this.receivedFeedbacks.add(feedback);
    }
    public void removeFeedback(Feedback feedback) {
        this.receivedFeedbacks.remove(feedback);
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
