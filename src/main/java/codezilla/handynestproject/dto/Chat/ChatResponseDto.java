package codezilla.handynestproject.dto.Chat;

import codezilla.handynestproject.model.entity.Message;
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

    private Long id;

    private Long userId;

    private Long performerId;

    private Long taskId;

    private List<Message> messages;

    private Timestamp createdOn;

    private Timestamp updatedOn;

    private boolean isDeleted;

}
