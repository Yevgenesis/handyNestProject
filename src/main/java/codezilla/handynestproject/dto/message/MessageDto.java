package codezilla.handynestproject.dto.message;

import lombok.Data;

@Data
public class MessageDto {
    private String sender;
    private String receiver;
    private String content;
    // геттеры и сеттеры
}
