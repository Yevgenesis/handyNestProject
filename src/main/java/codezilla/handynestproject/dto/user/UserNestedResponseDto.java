package codezilla.handynestproject.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserNestedResponseDto {

    private Long id;

    private String firstName;

    private String lastName;

}

