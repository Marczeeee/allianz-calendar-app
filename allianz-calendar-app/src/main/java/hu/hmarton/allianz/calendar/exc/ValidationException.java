package hu.hmarton.allianz.calendar.exc;

/**
 * Exception thrown when a validation error occurs.
 */
public class ValidationException extends RuntimeException {
    /**
     * Constructor.
     * @param message Validation error message
     */
    public ValidationException(final String message) {
        super(message);
    }
}
