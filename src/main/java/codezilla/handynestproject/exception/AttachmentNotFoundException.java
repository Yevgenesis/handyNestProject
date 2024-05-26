package codezilla.handynestproject.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class AttachmentNotFoundException extends RuntimeException {

    public AttachmentNotFoundException() {
        super("Attachment not found");
    }

    public AttachmentNotFoundException(String message) {
        super(message);
    }
}
