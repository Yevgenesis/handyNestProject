//package codezilla.handynestproject.model.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.NoArgsConstructor;
//
//import java.util.Collection;
//import java.util.HashSet;
//
//@Entity
//@Builder
//@Table(name = "role")
//@AllArgsConstructor
//@NoArgsConstructor
//public class Role {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "role_name", nullable = false)
//    private String roleName;
//
//    @OneToMany
//    @JoinTable(name = "user_role",
//            joinColumns = @JoinColumn(name = "role_id"),
//            inverseJoinColumns = @JoinColumn(name = "user_id"))
//    private Collection<User> users = new HashSet<>();
//
//    public void addUser(User user) {
//        users.add(user);
//    }
//    public void removeUser(User user) {
//        users.remove(user);
//    }
//
//
//}
