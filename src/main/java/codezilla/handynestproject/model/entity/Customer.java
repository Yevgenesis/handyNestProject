package codezilla.handynestproject.model.entity;

import codezilla.handynestproject.model.entity.enums.Rating;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer")
public class Customer {

    @Id
    @MapsId
    @OneToOne(fetch = FetchType.EAGER)
    private UserInfo userInfo;

    @Column(name = "rating")
    @Enumerated(EnumType.STRING)
    private Rating rating;

    @OneToMany(
            mappedBy = "customer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Task> tasks = new HashSet<>();

    }





