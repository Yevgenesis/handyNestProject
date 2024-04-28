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
@Table(name = "performer")
public class Performer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "is_phone_verified")
    private boolean isPhoneVerified;

    @Column(name = "is_passport_verified")
    private boolean isPassportVerified;

    @Column(name = "rating")
    @Enumerated(EnumType.STRING)
    private Rating rating;

    @Column(name = "country", length = 70)
    private String country;

    @Column(name = "city", length = 70)
    private String city;

    @OneToOne(mappedBy = "performer", cascade = CascadeType.ALL)
    private UserInfo userInfo;


    @OneToMany(
            mappedBy = "performer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Task> tasks = new HashSet<>();


    @OneToMany(
            mappedBy = "performer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Attachment> attachments = new HashSet<>();


}





