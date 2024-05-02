package codezilla.handynestproject.model.entity;//package codezilla.hendynestproject.model.entity;

import codezilla.handynestproject.model.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@Builder
@Table(name = "task")
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Double price;
    @OneToOne
    private Address address;

    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;
    @Builder.Default
    private boolean isPublish = true;

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
