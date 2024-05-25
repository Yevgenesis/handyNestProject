package codezilla.handynestproject.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    private int code;
    private String message;

    public ErrorResponse(String message) {
        super();
        this.message = message;
    }
}
