package codezilla.handynestproject.model.entity;

import codezilla.handynestproject.model.enums.RoleName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RoleName roleName;

    @ManyToOne
    private User users;

}
