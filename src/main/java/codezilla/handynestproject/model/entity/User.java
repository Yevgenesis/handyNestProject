package codezilla.handynestproject.model.entity;

import codezilla.handynestproject.model.enums.RoleName;
import com.fasterxml.jackson.annotation.JsonFormat;
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


@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "handy_user")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Entity representing a user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    @Schema(description = "First name of the user", example = "John")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @Schema(description = "Last name of the user", example = "Doe")
    private String lastName;

    @Column(name = "email", updatable = false, nullable = false, length = 50, unique = true)
    @Schema(description = "Email of the user", example = "john.doe@example.com")
    private String email;

    @Column(name = "is_email_verified", nullable = false)
    @Schema(description = "Indicates if the email is verified", example = "true")
    private boolean isEmailVerified;

    @Column(name = "password", nullable = false, length = 100)
    @Schema(description = "Password of the user", example = "password123")
    private String password;

    @Builder.Default
    @Column(name = "task_count", nullable = false)
    @Schema(description = "Number of tasks created by the user", example = "5")
    private Long taskCount = 0L;

    @Column(name = "is_deleted", nullable = false)
    @Schema(description = "Indicates if the user is deleted", example = "false")
    private boolean isDeleted;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Schema(description = "Tasks created by the user")
    private Set<Task> tasks = new HashSet<>();

    @Column(name = "logo")
    @Schema(description = "Logo of the user", example = "logo.png")
    private String logo;

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    @Column(name = "created_on", nullable = false, updatable = false)
    @Schema(description = "Date when the user was created", example = "2024-01-01")
    private Timestamp created_on;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    @Column(name = "updated_on", nullable = false)
    @Schema(description = "Date when the user was last updated", example = "2024-01-01")
    private Timestamp updated_on;

    @ElementCollection(targetClass = RoleName.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_name"}))
    @Column(name = "role_name")
    @Enumerated(EnumType.STRING)
    @Schema(description = "Roles assigned to the user")
    private Set<RoleName> roles = new HashSet<>();

    @OneToMany(
            mappedBy = "sender",
            cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY
    )
    @Schema(description = "Feedbacks sent by the user")
    private Set<Feedback> sentFeedbacks = new HashSet<>();

    @OneToMany(
            mappedBy = "sender",
            cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY
    )
    @Schema(description = "Messages sent by the user")
    private Set<Message> sendMessages = new HashSet<>();

    @Column(name = "user_rating")
    @Builder.Default
    @Schema(description = "Positive feedback percentage of the user", example = "100.0")
    private Double positiveFeedbackPercent = 100.0;

    public void increaseTaskCounter() {
        this.taskCount++;
    }
}