package codezilla.handynestproject.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@Table(name = "attachment")
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entity representing a attachment")
@EntityListeners(AuditingEntityListener.class)
public class Attachment {

    //TODO add createdOn
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Attachment id", example = "1")
    private Long id;

    @Schema(description = "File name", example = "my file")
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Schema(description = "Attachment type", example = "type")
    @Column(name = "type", nullable = false)
    private String type;

    @Schema(description = "Attachment url", example = "http://...")
    @Column(name = "url", nullable = false)
    private String url;

    @ManyToOne(fetch = FetchType.EAGER)
    private Performer performer;

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    @Column(name = "created_on", updatable = false, nullable = false)
    private LocalDateTime createdOn;
}
