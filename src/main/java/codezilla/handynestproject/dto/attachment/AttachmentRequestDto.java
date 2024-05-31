package codezilla.handynestproject.dto.attachment;

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
    @NotNull
    private Long performerId;
    @NotBlank
    private String fileName;
    @NotBlank
    private String type;
    @NotBlank
    private String url;
}
