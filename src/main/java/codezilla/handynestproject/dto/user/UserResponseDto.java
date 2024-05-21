package codezilla.handynestproject.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

// ToDo убрать конфиденциальные поля (оставил для тестов)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private Long taskCount;
    private Double positiveFeedbackPercent;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH.mm.ss")
    private Timestamp created_on;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH.mm.ss")
    private Timestamp updated_on;

}

