package codezilla.handynestproject.model.entity;

import codezilla.handynestproject.generator.UuidTimeSequenceGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Builder
@Table(name = "attachment")
@AllArgsConstructor
@NoArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", type = UuidTimeSequenceGenerator.class)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String fileName;
    @Column(name = "last_name", nullable = false)
    private String filePath;


    @ManyToOne(fetch = FetchType.LAZY)
    private Performer performer;




}
