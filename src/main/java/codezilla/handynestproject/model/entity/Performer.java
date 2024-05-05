package codezilla.handynestproject.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
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


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "performer")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Performer {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
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

    @ManyToMany(mappedBy = "performers", cascade = CascadeType.ALL)
    private Set<Category> categories = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Address address;

    @OneToMany(mappedBy = "performer", cascade = CascadeType.ALL,
            orphanRemoval = true, targetEntity = Task.class)
    private Set<Task> tasks = new HashSet<>();

    @Column(name = "is_available")
    boolean isAvailable;

    @Column(name = "performer_rating")
    private Double positiveFeedbackPercent;

    @Column(name = "feedback_count")
    private Long feedbackCount;

    @CreatedDate
    @Column(name = "created_on", nullable = false, updatable = false)
    private Timestamp created_on;

    @LastModifiedDate
    @Column(name = "updated_on", nullable = false)
    private Timestamp updated_on;


}





