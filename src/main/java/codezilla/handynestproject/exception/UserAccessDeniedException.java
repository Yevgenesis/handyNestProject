package codezilla.handynestproject.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class UserAccessDeniedException extends RuntimeException {

    public UserAccessDeniedException() {
    }

    public UserAccessDeniedException(String message) {
        super(message);
    }
}
