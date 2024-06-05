package codezilla.handynestproject.dto.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentRequestDto {
    @Schema(description = "Performer ID")
    @NotNull
    private Long performerId;
    @Schema(description = "Attachment file name", example = "document.pdf")
    @NotBlank
    private String fileName;
    @Schema(description = "Attachment type", example = "application/pdf")
    @NotBlank
    private String type;
    @Schema(description = "Attachment URL")
    @NotBlank
    private String url;
}

