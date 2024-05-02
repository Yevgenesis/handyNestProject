//package codezilla.handynestproject.model.entity;
//
//import codezilla.handynestproject.model.entity.enums.Rating;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.HashSet;
//import java.util.Set;
//
//@Data
//@Embeddable
//public class Performer {
//
//    @Column(name = "phone_number", length = 20)
//    private String phoneNumber;
//
//    @Column(name = "is_phone_verified")
//    private boolean isPhoneVerified;
//
//    @Column(name = "is_passport_verified")
//    private boolean isPassportVerified;
//
//    @Column(name = "rating")
//    @Enumerated(EnumType.STRING)
//    private Rating rating;
//
//    @Column(name = "country", length = 70)
//    private String country;
//
//    @Column(name = "city", length = 70)
//    private String city;
//
//
//    @OneToMany(
//            mappedBy = "user",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true
//    )
//    private Set<Task> tasks = new HashSet<>();
//
//
//    @OneToMany(
//            mappedBy = "performer",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true
//    )
//    private Set<Attachment> attachments = new HashSet<>();
//
//
//}
//
//
//
//
//
