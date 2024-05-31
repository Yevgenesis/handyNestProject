package codezilla.handynestproject.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class FeedbackErrorException extends RuntimeException {

    public FeedbackErrorException() {
        super();
    }

    public FeedbackErrorException(String message) {
        super(message);
    }
}
