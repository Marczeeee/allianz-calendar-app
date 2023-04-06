package hu.hmarton.allianz.calendar.controller;

import hu.hmarton.allianz.calendar.dto.OpenSlotDTO;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
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
        checkReservationIsWithinCurrentWeek(calendarEntry);

        return calendarEntryRepository.save(calendarEntry);
    }

    @GetMapping(value = "/reservations/weekly")
    public List<CalendarEntry> listWeeklySchedule() {
        return List.of();
    }

    @GetMapping(value = "/reservations/freehours")
    public List<OpenSlotDTO> listWeeklyOpenSlots() {
        return List.of();
    }

    @GetMapping(value = "/reservations/bydate")
    public CalendarEntry getReservationByDate(final String date) {
        return null;
    }

    private void checkReservationIsWithinCurrentWeek(final CalendarEntry calendarEntry) {
        final LocalDateTime startDate = calendarEntry.getStartDate();
        final LocalDateTime endDate = calendarEntry.getEndDate();
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date must be before end date");
        }

        final LocalDateTime zdt = LocalDateTime.now();
        final LocalDateTime firstDayOfWeek = zdt.with(ChronoField.DAY_OF_WEEK, 1);
        final LocalDateTime lastDayOfWeek = zdt.with(ChronoField.DAY_OF_WEEK, 7);

        if (startDate.isBefore(firstDayOfWeek)) {
            throw new ValidationException("Start date is before current week");
        } else if (startDate.isAfter(lastDayOfWeek)) {
            throw new ValidationException("Start date is after current week");
        }

        if (endDate.isBefore(firstDayOfWeek)) {
            throw new ValidationException("End date is before current week");
        } else if (endDate.isAfter(lastDayOfWeek)) {
            throw new ValidationException("End date is after current week");
        }
    }
}
