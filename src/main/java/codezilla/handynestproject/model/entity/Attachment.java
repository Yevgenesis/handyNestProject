package codezilla.handynestproject.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

@Entity
@Builder
@Table(name = "attachment")
@AllArgsConstructor
@NoArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String fileName;
    @Column(name = "last_name", nullable = false)
    private String filePath;

    @Transient
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;




}
