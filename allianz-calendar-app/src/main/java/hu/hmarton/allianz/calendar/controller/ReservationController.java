package hu.hmarton.allianz.calendar.controller;

import hu.hmarton.allianz.calendar.dto.OpenSlotDTO;
import hu.hmarton.allianz.calendar.model.CalendarEntry;
import hu.hmarton.allianz.calendar.repository.CalendarEntryRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;

/**
 * REST controller class for managing reservations.
 */
@RestController
public class ReservationController {
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
        final ZonedDateTime zdt = ZonedDateTime.now();
        final ZonedDateTime firstDayOfWeek = zdt.with(ChronoField.DAY_OF_WEEK, 1);
        final ZonedDateTime lastDayOfWeek = zdt.with(ChronoField.DAY_OF_WEEK, 7);

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
}
