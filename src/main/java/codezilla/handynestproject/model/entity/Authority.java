//package codezilla.handynestproject.model.entity;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.JoinTable;
//import jakarta.persistence.ManyToMany;
//import jakarta.persistence.Table;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import static jakarta.persistence.GenerationType.IDENTITY;
//
//@Data
//@Entity
//@Builder
//@Table(name = "authority")
//@AllArgsConstructor
//@NoArgsConstructor
//public class Authority {
//
//    @Id
//    @GeneratedValue(strategy = IDENTITY)
//    private Long id;
//
//    @Column(name = "authority")
//    private String authority;
//
//    @ManyToMany
//    @JoinTable(name = "authority_role",
//            joinColumns = @JoinColumn(name = "authority_id"),
//            inverseJoinColumns = @JoinColumn(name = "role_id"))
//    private Set<Role> roles = new HashSet<>();
//
//
//
//}
