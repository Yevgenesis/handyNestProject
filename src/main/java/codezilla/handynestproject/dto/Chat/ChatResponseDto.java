package codezilla.handynestproject.dto.Chat;

import codezilla.handynestproject.model.entity.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseDto {

    @Schema(description = "Chat ID")
    private Long id;
    @Schema(description = "User ID")
    private Long userId;
    @Schema(description = "Performer ID")
    private Long performerId;
    @Schema(description = "Task ID")
    private Long taskId;
    @Schema(description = "List of messages in the chat")
    private List<Message> messages;
    @Schema(description = "Timestamp of chat creation")
    private Timestamp createdOn;
    @Schema(description = "Timestamp of last chat update")
    private Timestamp updatedOn;
    @Schema(description = "Flag indicating if chat is deleted")
    private boolean isDeleted;
}
