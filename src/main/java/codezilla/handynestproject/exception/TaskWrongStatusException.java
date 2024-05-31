package codezilla.handynestproject.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class TaskWrongStatusException extends RuntimeException {

    public TaskWrongStatusException() {
    }

    public TaskWrongStatusException(String message) {
        super(message);
    }
}
