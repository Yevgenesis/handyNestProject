package codezilla.handynestproject.dto.Chat;

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

    @NotNull(message = "Sender can't be empty")
    private Long senderId;

    @NotNull(message = "Receiver can't be empty")
    private Long receiverId;

    @NotNull(message = "Task can't be empty")
    private Long taskId;

    @NotNull(message = "Text can't be empty")
    private String text;
}
