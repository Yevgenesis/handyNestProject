package codezilla.handynestproject.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("messages", errors);
        return ResponseEntity.of(Optional.of(errorResponse)); // ToDo исправить Exception
    }

    @ExceptionHandler(WrongConfirmationPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleWrongConfirmationPasswordException(WrongConfirmationPasswordException ex, WebRequest request) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public @ResponseBody ErrorResponse UserAlreadyExistException(UserAlreadyExistsException ex, WebRequest request) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResponse handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(WorkingTimeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResponse handleWorkingTimeNotFoundException(WorkingTimeNotFoundException ex, WebRequest request) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResponse handleCategoryNotFoundException(CategoryNotFoundException ex) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResponse handleTaskNotFoundException(TaskNotFoundException ex) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(PerformerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResponse handlePerformerNotFoundException(PerformerNotFoundException ex) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    /**
     * Method für @Nullable
     */
//    @Override
//    protected ResponseEntity<Object> handleArgumentNotValid
//    (MethodArgumentNotValidException ex, HttpHeaders headers,
//     HttpStatus status, WebRequest request) {
//        return new ResponseEntity<>(
//                new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
//                        getMessageConcat(ex)), HttpStatus.BAD_REQUEST
//        );
//    }

//    private String getMessageConcat(MethodArgumentNotValidException ex) {
//        return ex.getBindingResult()
//                .getAllErrors().stream()
//                .map(DefaultMessageSourceResolvable::getDefaultMessage)
//                .filter(Objects::nonNull)
//                .toList().toString();
//    }


}
