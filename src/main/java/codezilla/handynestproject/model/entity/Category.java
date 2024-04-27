package codezilla.handynestproject.model.entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@Table(name = "category")
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int parentId;
    private int position;


    @OneToMany(mappedBy = "category",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<Task> tasks = new HashSet<>();

//    @Builder.Default
//    @OneToMany(
//            mappedBy = "category",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true
//    )
//    private Set<Feedback> feedbacks = new HashSet<>();

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
    }

//    public void addFeedback(Feedback feedback) {
//        feedbacks.add(feedback);
//    }

//    public void removeFeedback(Feedback feedback) {
//        feedbacks.remove(feedback);
//    }


}
