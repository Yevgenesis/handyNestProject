package codezilla.handynestproject.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Builder
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, length = 20)
    private String roleName;

//    @ManyToMany(mappedBy = "roles", cascade = CascadeType.ALL)
//    @Builder.Default
//    private Set<User> users = new HashSet<>();

}
