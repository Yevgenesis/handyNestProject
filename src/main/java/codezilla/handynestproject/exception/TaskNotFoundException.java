package codezilla.handynestproject.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException() {
    }

    public TaskNotFoundException(String message) {
        super(message);
    }
}
