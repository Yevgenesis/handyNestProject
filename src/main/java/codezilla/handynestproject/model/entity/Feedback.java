package codezilla.handynestproject.model.entity;

import codezilla.handynestproject.generator.UuidTimeSequenceGenerator;
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
@Table(name = "feedback")
@AllArgsConstructor
@NoArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", type = UuidTimeSequenceGenerator.class)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserInfo sender_id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserInfo receiver_id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;

}
