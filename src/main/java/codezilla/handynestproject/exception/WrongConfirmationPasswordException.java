package codezilla.handynestproject.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class WrongConfirmationPasswordException extends RuntimeException {

    public WrongConfirmationPasswordException() {
        super("Password and ConfirmationPassword do not match");
    }

    public WrongConfirmationPasswordException(String message) {
        super(message);
    }
}
