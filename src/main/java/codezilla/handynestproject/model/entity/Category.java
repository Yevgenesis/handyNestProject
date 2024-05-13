package codezilla.handynestproject.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "category")
//@EqualsAndHashCode(exclude = {"tasks", "performers"})
//@ToString(exclude = {"tasks", "performers"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;


    @Column(name = "parent_id")
    private Long parentId;

    private int weight;

    @OneToMany(mappedBy = "category",
            cascade = CascadeType.ALL,
            orphanRemoval = true)

    private Set<Task> tasks = new HashSet<>();

//
//    @ManyToMany
//    private Set<Performer> performers = new HashSet<>();

}
