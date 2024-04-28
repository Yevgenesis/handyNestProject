package codezilla.handynestproject.model.entity;

import codezilla.handynestproject.model.entity.enums.Rating;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;


    @Column(name = "rating")
    @Enumerated(EnumType.STRING)
    private Rating rating;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
    private UserInfo userInfo;

    @OneToMany(
            mappedBy = "customer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Task> tasks = new HashSet<>();






    }





