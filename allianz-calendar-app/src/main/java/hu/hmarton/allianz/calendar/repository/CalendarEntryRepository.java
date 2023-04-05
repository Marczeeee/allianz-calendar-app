package hu.hmarton.allianz.calendar.repository;

import hu.hmarton.allianz.calendar.model.CalendarEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managind {@link CalendarEntry} entities withing a database.
 */
@Repository
public interface CalendarEntryRepository extends JpaRepository<CalendarEntry, Long> {
}
