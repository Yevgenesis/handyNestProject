package codezilla.handynestproject.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDto {

    @Schema(description = "Message ID")
    private Long id;
    @Schema(description = "Chat ID")
    private Long chatId;
    @Schema(description = "Sender ID")
    private Long senderId;
    @Schema(description = "Message text")
    private String text;
    @Schema(description = "Timestamp of message creation")
    private Timestamp createdOn;
    @Schema(description = "Timestamp of last message update")
    private Timestamp updatedOn;
    @Schema(description = "Flag indicating if message is read")
    private boolean isRead;
}
