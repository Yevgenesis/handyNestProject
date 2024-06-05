package codezilla.handynestproject.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException() {
    }

    public MessageNotFoundException(String message) {
        super(message);
    }
}
