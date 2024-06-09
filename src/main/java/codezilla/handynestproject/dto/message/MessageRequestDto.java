package codezilla.handynestproject.dto.message;

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
public class MessageRequestDto {

    @Schema(description = "chat ID")
    @NotNull(message = "chat can't be empty")
    private Long chatId;
    @Schema(description = "Sender ID")
    @NotNull(message = "Sender can't be empty")
    private Long senderId;
    @Schema(description = "Message text")
    @NotBlank(message = "Text can't be empty")
    private String text;
}
