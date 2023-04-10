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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        logger.info("Listing reservations for current week ({} - {})", mondayOfWeek.toLocalDate(), fridayOfWeek.toLocalDate());

        return calendarEntryRepository.findByStartDateBetweenOrderByStartDateAsc(mondayOfWeek, fridayOfWeek);
    }

    /** Minutes number indicating half of an hour. */
    private static final int HALF_OF_HOUR_IN_MINUTES = 30;

    /**
     * Returns the open slots of the current day.
     * @return List of open slots
     */
    @GetMapping(value = "/reservations/freehours/day")
    public List<OpenSlotDTO> listDailyOpenSlots() {
        final LocalDateTime now = LocalDateTime.now();
        logger.info("Listing all open time slots for current day ({})", now.toLocalDate());
        return listOpenSlotsForDay(now);
    }

    @GetMapping(value = "/reservations/freehours/week")
    public List<OpenSlotDTO> listWeeklyOpenSlots() {
        logger.info("Listing all open time slots for current week");
        final LocalDateTime now = LocalDateTime.now();
        int currentDayValue = now.getDayOfWeek().getValue();
        final List<OpenSlotDTO> openSlots = listOpenSlotsForDay(now);
        currentDayValue++;
        LocalDateTime currentDayStart = now.truncatedTo(ChronoUnit.DAYS);
        while (currentDayValue <= DayOfWeek.FRIDAY.getValue()) {
            currentDayStart = currentDayStart.plusDays(1);
            openSlots.addAll(listOpenSlotsForDay(currentDayStart));
            currentDayValue++;
        }

        return openSlots;
    }

    /** Pattern of date and time used to query person name did the reservation. */
    private static final String DATE_TIME_FORMAT = "yy.MM.dd HH:mm";

    /**
     * Returns the name of the person who did the reservation at the specified date and time. Returns an error
     * message if no reservation is available at the specified date and time.
     * @param dateString Date and time string
     * @return Name of the person who did the reservation, or an error message
     */
    @GetMapping(value = "/reservations/personname/bydate")
    public String getReservationPersonNameByDate(@RequestParam(name = "dateString") final String dateString) {
        logger.info("Get person's name who made the reservation by date: {}", dateString);
        final LocalDateTime dateTime = LocalDateTime.from(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).parse(dateString));
        final Optional<CalendarEntry> optionalCalendarEntry = calendarEntryRepository.getByDate(dateTime);
        return optionalCalendarEntry.isPresent() ? optionalCalendarEntry.get().getBookingPersonName()
                : "No reservation is available at the specified date and time.";
    }

    /**
     * Checks if a reservation is within the allowed range within the week determined by its starting date.
     * @param calendarEntry New calendar entry object
     */
    private void checkReservationIsWithinWeek(final CalendarEntry calendarEntry) {
        logger.debug("Checking if reservation ({}) is within the allowed range the week", calendarEntry);
        final LocalDateTime startDate = calendarEntry.getStartDate().truncatedTo(ChronoUnit.SECONDS);
        final LocalDateTime endDate = calendarEntry.getEndDate().truncatedTo(ChronoUnit.SECONDS);
        if (startDate.isAfter(endDate)) {
            logger.error("Reservation ({}) start date is before its end date", calendarEntry);
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_END_DATE_BEFORE_START_DATE);
        }
        if (startDate.isBefore(LocalDateTime.now())) {
            logger.error("Reservation ({}) start date is in the past", calendarEntry);
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_START_DATE_MUST_BE_IN_FUTURE);
        }

        final LocalDateTime lastDayOfWeek =
                LocalTime.MAX.atDate(startDate.with(ChronoField.DAY_OF_WEEK, DayOfWeek.FRIDAY.getValue()).toLocalDate());

        if (startDate.isAfter(lastDayOfWeek)) {
            logger.error("Reservation ({}) start date is not on a weekday!", calendarEntry);
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
            logger.error("Reservation ({}) start date is before allowed ({}:00) time within a weekday!", calendarEntry,
                    FIRST_HOUR_OF_WEEKDAY_ALLOWED);
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_MUST_START_AFTER_9AM);
        }
        if (calendarEntry.getEndDate().getHour() > LAST_HOUR_OF_WEEKDAY_ALLOWED) {
            logger.error("Reservation ({}) end date is after allowed ({}:00) time within a weekday!", calendarEntry,
                    LAST_HOUR_OF_WEEKDAY_ALLOWED);
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
            logger.error("Reservation ({}) length ({} min) should be at least {} minutes!", calendarEntry,
                    reservationLengthInMinutes, RESERVATION_SLOT_SIZE);
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_LENGTH_AT_LEAST_30MIN);
        }
        if (reservationLengthInMinutes / RESERVATION_SLOT_SIZE > MAX_TIME_SLOTS_PER_RESERVATION) {
            logger.error("Reservation ({}) length ({} min) is too long!", calendarEntry, reservationLengthInMinutes);
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_LENGTH_MAX_3HOURS);
        }
        if (reservationLengthInMinutes % RESERVATION_SLOT_SIZE != 0) {
            logger.error("Reservation ({}) length ({} min) should be dividable by {} minutes!", calendarEntry,
                    reservationLengthInMinutes, RESERVATION_SLOT_SIZE);
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_30MIN_SLOTS_ONLY);
        }
        if (startDate.getMinute() % MIN_OF_TIME_ALLOWED != 0) {
            logger.error("Reservation ({}) start date should be 00 or 30 minutes!", calendarEntry);
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
            logger.error("Reservation ({}) overlaps with {} existing reversion(s)!", calendarEntry, overlappingEntriesCount);
            throw new ValidationException(ValidationErrorMessages.VALIDATION_ERROR_DATES_OVERLAPPING_WITH_EXISTING_RESERVATION);
        }
    }

    /**
     * Finds all open slots in the calendar for a given day.
     * @param day Day to be checked for open slots
     * @return List of open slots within the given day
     */
    private List<OpenSlotDTO> listOpenSlotsForDay(final LocalDateTime day) {
        if (day.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
            throw new ValidationException("Today is not weekday, reservation is not available!");
        }

        LocalDateTime dailyStartDate = day.truncatedTo(ChronoUnit.MINUTES);
        if (dailyStartDate.getHour() < FIRST_HOUR_OF_WEEKDAY_ALLOWED) {
            logger.debug("Modifying the hour value of the opening date ({}) to be the first allowed value ({}) for a weekday",
                    dailyStartDate, FIRST_HOUR_OF_WEEKDAY_ALLOWED);
            dailyStartDate = dailyStartDate.withHour(FIRST_HOUR_OF_WEEKDAY_ALLOWED);
        }
        if (dailyStartDate.getMinute() > 0 && dailyStartDate.getMinute() <= HALF_OF_HOUR_IN_MINUTES) {
            logger.debug("Modifying the minutes value of the opening date ({}) to be the half of an hour ({})",
                    dailyStartDate, HALF_OF_HOUR_IN_MINUTES);
            dailyStartDate = dailyStartDate.withMinute(HALF_OF_HOUR_IN_MINUTES);
        } else if (dailyStartDate.getMinute() > HALF_OF_HOUR_IN_MINUTES) {
            logger.debug("Modifying the minutes value of the opening date ({}) to be the at the start of the next hour",
                    dailyStartDate);
            dailyStartDate = dailyStartDate.withHour(dailyStartDate.getHour() + 1).withMinute(0);
        }

        final LocalDateTime dailyEndDate = dailyStartDate.withHour(LAST_HOUR_OF_WEEKDAY_ALLOWED).withMinute(0);
        logger.debug("Setting daily ending date to {}", dailyEndDate);

        final List<CalendarEntry> calendarEntriesToday =
                calendarEntryRepository.findByStartDateBetweenOrderByStartDateAsc(dailyStartDate, dailyEndDate);
        logger.debug("Fetched {} calendar entries betweek daily opening ({} and ending ({}) dates",
                calendarEntriesToday.size(), dailyStartDate, dailyEndDate);
        final Map<LocalDateTime, CalendarEntry> calendarEntryMap = calendarEntriesToday.stream()
                .collect(Collectors.toMap(CalendarEntry::getStartDate, calendarEntry -> calendarEntry));

        LocalDateTime lastOpenSlotDate = null;
        LocalDateTime currentCheckedDate = dailyStartDate;
        final List<OpenSlotDTO> openSlotsOfDay = new ArrayList<>();
        while (!currentCheckedDate.isAfter(dailyEndDate)) {
            if (calendarEntryMap.containsKey(currentCheckedDate)) {
                if (lastOpenSlotDate != null) {
                    final OpenSlotDTO openSlotDTO = new OpenSlotDTO();
                    openSlotDTO.setSlotStartDate(lastOpenSlotDate);
                    openSlotDTO.setSlotEndDate(currentCheckedDate);
                    openSlotsOfDay.add(openSlotDTO);
                    logger.debug("Found a new open slot: {}", openSlotDTO);
                    lastOpenSlotDate = null;
                }
                final CalendarEntry calendarEntry = calendarEntryMap.get(currentCheckedDate);
                currentCheckedDate = calendarEntry.getEndDate();
            } else {
                if (lastOpenSlotDate != null) {
                    final OpenSlotDTO openSlotDTO = new OpenSlotDTO();
                    openSlotDTO.setSlotStartDate(lastOpenSlotDate);
                    openSlotDTO.setSlotEndDate(currentCheckedDate);
                    openSlotsOfDay.add(openSlotDTO);
                    logger.debug("Found a new open slot: {}", openSlotDTO);
                }
                lastOpenSlotDate = currentCheckedDate;
                currentCheckedDate = currentCheckedDate.plusMinutes(RESERVATION_SLOT_SIZE);
            }
        }

        return openSlotsOfDay;
    }
}
