package codezilla.handynestproject.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @JsonIgnore
    @Column(name = "parent_id")
    private Long parentId;
    @JsonIgnore
    private int weight;

    @OneToMany(mappedBy = "category",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private Set<Task> tasks = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "performer_categories",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "performer_id"))
    @JsonIgnore
    private Set<Performer> performers = new HashSet<>();


}
