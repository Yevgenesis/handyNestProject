package codezilla.handynestproject.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("User not found");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
