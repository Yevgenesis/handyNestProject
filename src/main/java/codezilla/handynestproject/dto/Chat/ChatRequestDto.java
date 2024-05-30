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

    @NotNull(message = "User can't be empty")
    private Long userId;

    @NotNull(message = "Performer can't be empty")
    private Long performerId;

    @NotNull(message = "Task can't be empty")
    private Long taskId;

}
