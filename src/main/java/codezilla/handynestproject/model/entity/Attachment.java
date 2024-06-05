package codezilla.handynestproject.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@Table(name = "attachment")
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entity representing a attachment")
public class Attachment {

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
}
