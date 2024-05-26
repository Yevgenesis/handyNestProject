package codezilla.handynestproject.dto.attachment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDto {
    private Long id;
    private Long performerId;
    private String fileName;
    private String type;
    private String url;
}
