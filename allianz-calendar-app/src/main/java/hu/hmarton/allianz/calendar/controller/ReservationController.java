package hu.hmarton.allianz.calendar.controller;

import hu.hmarton.allianz.calendar.dto.OpenSlotDTO;
import hu.hmarton.allianz.calendar.exc.ValidationErrorMessages;
import hu.hmarton.allianz.calendar.exc.ValidationException;
import hu.hmarton.allianz.calendar.model.CalendarEntry;
import hu.hmarton.allianz.calendar.repository.CalendarEntryRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * REST controller class for managing reservations.
 */
@RestController
public class ReservationController {
    /** {@link Logger} instance. */
    private final Logger logger = LoggerFactory.getLogger(ReservationController.class);
    /** {@link CalendarEntryRepository} bean. */
    @Autowired
    private CalendarEntryRepository calendarEntryRepository;

    /**
     * Creates a new reservation based on the data given by the caller.
     * @param calendarEntry Calendar entry to be created
     * @return New CalendarEntry entity created
     */
    @PostMapping(value = "/reservation")
    public CalendarEntry createNewReservation(@Valid @RequestBody final CalendarEntry calendarEntry) {
        logger.info("Creating new reservation as {}", calendarEntry);
        logger.debug("Modify the dates in the reservation object to use 00 seconds always.");
        calendarEntry.setStartDate(calendarEntry.getStartDate() != null
                ? calendarEntry.getStartDate().truncatedTo(ChronoUnit.SECONDS) : null);
        calendarEntry.setEndDate(calendarEntry.getEndDate() != null
                ? calendarEntry.getEndDate().truncatedTo(ChronoUnit.SECONDS) : null);

        checkReservationIsWithinWeek(calendarEntry);
        checkReservationTimeWithinDay(calendarEntry);
        checkReservationLength(calendarEntry);
        checkReservationOverlapping(calendarEntry);

        return calendarEntryRepository.save(calendarEntry);
    }

    /**
     * Lists all reservation of the current week.
     * @return List of reservation saved for current week
     */
    @GetMapping(value = "/reservations/weekly")
    public List<CalendarEntry> listWeeklySchedule() {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime mondayOfWeek = LocalTime.MIN.atDate(now.with(ChronoField.DAY_OF_WEEK,
                DayOfWeek.MONDAY.getValue()).toLocalDate());
        final LocalDateTime fridayOfWeek =
                LocalTime.MAX.atDate(now.with(ChronoField.DAY_OF_WEEK, DayOfWeek.FRIDAY.getValue()).toLocalDate());

        return calendarEntryRepository.findByStartDateBetweenOrderByStartDateAsc(mondayOfWeek, fridayOfWeek);
    }

    @GetMapping(value = "/reservations/freehours")
    public List<OpenSlotDTO> listWeeklyOpenSlots() {

        return List.of();
    }

    @GetMapping(value = "/reservations/bydate")
    public CalendarEntry getReservationByDate(final String date) {
        return null;
    }

    /**
     * Checks if a reservation is within the allowed range within the week determined by its starting date.
     * @param calendarEntry New calendar entry object
     */
    private void checkReservationIsWithinWeek(final CalendarEntry calendarEntry) {
        final LocalDateTime startDate = calendarEntry.getStartDate().truncatedTo(ChronoUnit.SECONDS);
        final LocalDateTime endDate = calendarEntry.getEndDate().truncatedTo(ChronoUnit.SECONDS);
        if (startDate.isAfter(endDate)) {
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_END_DATE_BEFORE_START_DATE);
        }
        if (startDate.isBefore(LocalDateTime.now())) {
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_START_DATE_MUST_BE_IN_FUTURE);
        }

        final LocalDateTime lastDayOfWeek =
                LocalTime.MAX.atDate(startDate.with(ChronoField.DAY_OF_WEEK, DayOfWeek.FRIDAY.getValue()).toLocalDate());

        if (startDate.isAfter(lastDayOfWeek)) {
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_MUST_BE_ON_WEEKDAY);
        }
    }

    /** Contains the number of the first hour can be booked on a weekday. */
    private static final int FIRST_HOUR_OF_WEEKDAY_ALLOWED = 9;
    /** Contains the number of the last hour can be used to end a reservation on a weekday. */
    private static final int LAST_HOUR_OF_WEEKDAY_ALLOWED = 17;

    /**
     * Checks is a reservation is withing the allowed time frame in a day.
     * @param calendarEntry New calendar entry object
     */
    private void checkReservationTimeWithinDay(final CalendarEntry calendarEntry) {
        if (calendarEntry.getStartDate().getHour() < FIRST_HOUR_OF_WEEKDAY_ALLOWED) {
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_MUST_START_AFTER_9AM);
        }
        if (calendarEntry.getEndDate().getHour() > LAST_HOUR_OF_WEEKDAY_ALLOWED) {
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_MUST_END_BEFORE_5PM);
        }
    }

    /** Shortest reservation length in minutes. */
    private static final int RESERVATION_SLOT_SIZE = 30;
    /** Maximal number of time slots to be booked in one reservation. */
    private static final int MAX_TIME_SLOTS_PER_RESERVATION = 6;
    /** Number helping to determine if reservation starts at a proper time (hh:00 or hh:30). */
    private static final int MIN_OF_TIME_ALLOWED = 30;

    /**
     * Checks if the length of the reservation is within allowed bounds.
     * @param calendarEntry New calendar entry object
     */
    private void checkReservationLength(final CalendarEntry calendarEntry) {
        final LocalDateTime startDate = calendarEntry.getStartDate();
        final LocalDateTime endDate = calendarEntry.getEndDate();
        final Duration reservationDuration = Duration.between(startDate, endDate);
        final long reservationLengthInMinutes = reservationDuration.toMinutes();
        if (reservationLengthInMinutes / RESERVATION_SLOT_SIZE <= 0) {
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_LENGTH_AT_LEAST_30MIN);
        }
        if (reservationLengthInMinutes / RESERVATION_SLOT_SIZE > MAX_TIME_SLOTS_PER_RESERVATION) {
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_LENGTH_MAX_3HOURS);
        }
        if (reservationLengthInMinutes % RESERVATION_SLOT_SIZE != 0) {
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_30MIN_SLOTS_ONLY);
        }
        if (startDate.getMinute() % MIN_OF_TIME_ALLOWED != 0) {
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_START_AT_00MIN_OR_30MIN_ONLY);
        }
    }

    /**
     * Checks if a new reservation would overlap with any existing reservation(s).
     * @param calendarEntry New calendar entry object
     */
    private void checkReservationOverlapping(final CalendarEntry calendarEntry) {
        final long overlappingEntriesCount = calendarEntryRepository.countOverlapping(calendarEntry.getStartDate(),
                calendarEntry.getEndDate());
        if (overlappingEntriesCount > 0) {
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_DATES_OVERLAPPING_WITH_EXISTING_RESERVATION);
        }
    }
}
