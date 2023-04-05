package hu.hmarton.allianz.calendar.controller.advice;

import hu.hmarton.allianz.calendar.exc.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller advice class for handling validation exceptions.
 */
@ControllerAdvice
public class ExceptionControllerAdvice {
    /**
     * Handles {@link ValidationException} objects thrown by REST interfaces.
     * @param validationException Exception object
     * @return Error message displayed for the caller
     */
    @ResponseBody
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(final ValidationException validationException) {
        return validationException.getMessage();
    }

    /**
     * Handles validation exceptions thrown by bean validation using entity-level annotations.
     * @param exception Exception object
     * @return Error message displayed for the caller
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleBeanValidationExceptions(MethodArgumentNotValidException exception) {
        final Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
