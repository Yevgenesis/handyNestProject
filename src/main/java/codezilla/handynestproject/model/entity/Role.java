package codezilla.handynestproject.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
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

    @ManyToMany(mappedBy = "roles", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @ManyToMany(mappedBy = "roles", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Authority> authorities = new HashSet<>();










}
