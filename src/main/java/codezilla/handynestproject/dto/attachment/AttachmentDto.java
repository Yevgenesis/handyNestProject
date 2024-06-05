package codezilla.handynestproject.dto.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDto {
    @Schema(description = "Attachment ID")
    private Long id;
    @Schema(description = "Performer ID")
    private Long performerId;
    @Schema(description = "Attachment file name", example = "document.pdf")
    private String fileName;
    @Schema(description = "Attachment type", example = "application/pdf")
    private String type;
    @Schema(description = "Attachment URL")
    private String url;
}

