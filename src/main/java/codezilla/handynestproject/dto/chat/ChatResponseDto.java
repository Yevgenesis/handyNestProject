package codezilla.handynestproject.dto.chat;

import codezilla.handynestproject.model.entity.Message;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @Schema(description = "chat ID")
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    private Timestamp createdOn;
    @Schema(description = "Timestamp of last chat update")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    private Timestamp updatedOn;
    @Schema(description = "Flag indicating if chat is deleted")
    private boolean isDeleted;
}
