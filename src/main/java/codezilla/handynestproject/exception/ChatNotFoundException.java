package codezilla.handynestproject.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ChatNotFoundException extends RuntimeException {

    public ChatNotFoundException() {
    }

    public ChatNotFoundException(String message) {
        super(message);
    }
}
