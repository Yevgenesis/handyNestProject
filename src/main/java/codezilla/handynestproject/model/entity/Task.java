package codezilla.handynestproject.model.entity;//package codezilla.hendynestproject.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.Set;
import java.util.UUID;

@Entity
@Builder
@Table(name = "task")
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", columnDefinition = "uuid", unique = true, nullable = false, updatable = false)
    private UUID id;

    private String title;
    private String description;
    private Double price;
    private String location;
    private String state;

    @OneToOne
    private WorkingTime workingTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @OneToMany(
            mappedBy = "task",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Feedback> feedbacks;

}
