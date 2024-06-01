package codezilla.handynestproject.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Data
@Builder
@Table(name = "attachment")
@AllArgsConstructor
@NoArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(name = "Attachment id",example = "1", required = true)
    private Long id;
    @Schema(name = "File name",example = "my file", required = true)
    @Column(name = "file_name", nullable = false)
    private String fileName;
    //TODO write right type
    @Schema(name = "Attachment type",example = "type", required = true)
    @Column(name = "type", nullable = false)
    private String type;
    @Schema(name = "Attachment url",example = "http://...", required = true)
    @Column(name = "url", nullable = false)
    private String url;


    @ManyToOne(fetch = FetchType.EAGER)
    private Performer performer;

}
