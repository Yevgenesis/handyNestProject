package codezilla.handynestproject.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class WorkingTimeNotFoundException extends RuntimeException {

    public WorkingTimeNotFoundException() {
    }

    public WorkingTimeNotFoundException(String message) {
        super(message);
    }
}
