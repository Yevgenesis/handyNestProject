package codezilla.handynestproject.dto.message;

import jakarta.validation.constraints.NotNull;
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


    private Long id;

    private Long chatId;

    private Long senderId;

    private String text;

    private Timestamp createdOn;

    private Timestamp updatedOn;

    private boolean isRead;
}
