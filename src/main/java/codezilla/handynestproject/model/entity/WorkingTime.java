package codezilla.handynestproject.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "working_time")
public class WorkingTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

//    @OneToMany(mappedBy = "workingTime",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true)
//    private Set<Task> tasks = new HashSet<>();



}
