package codezilla.handynestproject.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class PerformerNotFoundException extends RuntimeException {

    public PerformerNotFoundException() {
    }

    public PerformerNotFoundException(String message) {
        super(message);
    }

}
