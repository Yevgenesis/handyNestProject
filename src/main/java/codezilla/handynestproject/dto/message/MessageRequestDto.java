package codezilla.handynestproject.dto.message;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestDto {

    @NotNull(message = "Chat can't be empty")
    private Long chatId;

    @NotNull(message = "Sender can't be empty")
    private Long senderId;

    @NotNull(message = "Sender can't be empty")
    private Long receiverId;

    @NotNull(message = "Text can't be empty")
    private String text;
}
