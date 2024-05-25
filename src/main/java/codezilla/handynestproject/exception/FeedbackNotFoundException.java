package codezilla.handynestproject.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class FeedbackNotFoundException extends RuntimeException {

    public FeedbackNotFoundException() {
        super("Feedback not found");
    }

    public FeedbackNotFoundException(String message) {
        super(message);
    }
}
