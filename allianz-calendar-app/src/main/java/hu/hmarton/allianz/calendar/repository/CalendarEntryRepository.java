package hu.hmarton.allianz.calendar.repository;

import hu.hmarton.allianz.calendar.model.CalendarEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link CalendarEntry} entities withing a database.
 */
@Repository
public interface CalendarEntryRepository extends JpaRepository<CalendarEntry, Long> {
    /**
     * Returns all {@link CalendarEntry} records which has its start date value between the specified dates.
     * @param openingDate Opening date value
     * @param closingDate Closing date value
     * @return List of {@link CalendarEntry} records withing the date range specified
     */
    List<CalendarEntry> findByStartDateBetweenOrderByStartDateAsc(LocalDateTime openingDate, LocalDateTime closingDate);

    /**
     * Returns the number of {@link CalendarEntry} records which would overlap with a reservation with the specified
     * starting and ending date.
     * @param startDate Start date of a reservation
     * @param endDate End date of a reservation
     * @return Number of overlapping {@link CalendarEntry} records
     */
    @Query(value = "SELECT count(*) FROM CalendarEntry WHERE (startDate<=?1 AND endDate>?1) OR "
            + "(startDate<?2 AND endDate>=?2) OR (startDate<=?1 AND endDate>=?2)")
    long countOverlapping(LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "SELECT ce FROM CalendarEntry ce WHERE startDate<=?1 AND endDate>=?1")
    Optional<CalendarEntry> getByDate(LocalDateTime date);
}
