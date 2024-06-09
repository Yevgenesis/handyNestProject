package codezilla.handynestproject.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequestDto {
    @Schema(description = "User ID")
    @NotNull(message = "User can't be empty")
    private Long userId;
    @Schema(description = "Performer ID")
    @NotNull(message = "Performer can't be empty")
    private Long performerId;
    @Schema(description = "Task ID")
    @NotNull(message = "Task can't be empty")
    private Long taskId;
}
