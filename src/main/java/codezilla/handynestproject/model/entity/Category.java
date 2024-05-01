package codezilla.handynestproject.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Builder
@Table(name = "category")
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", columnDefinition = "uuid", unique = true, nullable = false, updatable = false)
    private UUID id;

    private String name;
    private int parentId;
    private int weight;

    @OneToMany(mappedBy = "category",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();





}
