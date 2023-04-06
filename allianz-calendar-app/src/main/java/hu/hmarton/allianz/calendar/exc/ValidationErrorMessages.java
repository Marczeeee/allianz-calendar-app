package hu.hmarton.allianz.calendar.exc;

/**
 * Class containing static references to all validation exception error messages.
 */
public final class ValidationErrorMessages {
    /** Hidden constructor. */
    private ValidationErrorMessages() {};

    public static final String VALIDATION_ERROR_END_DATE_BEFORE_START_DATE = "Start date must be before end date!";

    public static final String VALIDATION_ERROR_START_DATE_MUST_BE_IN_FUTURE = "Start date must be in the future!";

    public static final String VALIDATION_ERROR_RESERVATION_MUST_BE_ON_WEEKDAY = "Reservation must be on a weekday!";

    public static final String VALIDATION_ERROR_RESERVATION_MUST_START_AFTER_9AM = "Reservation must start after 9:00!";

    public static final String VALIDATION_ERROR_RESERVATION_MUST_END_BEFORE_5PM = "Reservation must end before 17:00!";

    public static final String VALIDATION_ERROR_RESERVATION_LENGTH_AT_LEAST_30MIN = "Reservation length should be at least 30 minutes!";

    public static final String VALIDATION_ERROR_RESERVATION_LENGTH_MAX_3HOURS = "Reservation can't be longer than 3 hours!";

    public static final String VALIDATION_ERROR_RESERVATION_30MIN_SLOTS_ONLY = "Reservation should use 30 minutes long slots!";

    public static final String VALIDATION_ERROR_START_AT_00MIN_OR_30MIN_ONLY = "Reservation must start at 00 or 30 minutes!";

    public static final String VALIDATION_ERROR_DATES_OVERLAPPING_WITH_EXISTING_RESERVATION = "Reservation dates "
            + "overlapping with existing reservation(s)!";
}
