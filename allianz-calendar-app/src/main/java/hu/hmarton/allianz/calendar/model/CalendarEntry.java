package hu.hmarton.allianz.calendar.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.StringJoiner;

/**
 * Calendar entry created by a person.
 */
@Entity
public class CalendarEntry {
    /** Unique identifier. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    /** Name of the person created the reservation. */
    @NotBlank(message = "Name of the person is mandatory")
    private String bookingPersonName;
    /** Start date of the reservation. */
    @NotNull(message = "Reservation start date is mandatory")
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime startDate;
    /** End date of the reservation. */
    @NotNull(message = "Reservation end date is mandatory")
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime endDate;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getBookingPersonName() {
        return bookingPersonName;
    }

    public void setBookingPersonName(final String bookingPersonName) {
        this.bookingPersonName = bookingPersonName;
    }

    public LocalDateTime getStartDate() {
        return startDate != null ? LocalDateTime.from(startDate) : null;
    }

    public void setStartDate(final LocalDateTime startDate) {
        this.startDate = startDate != null ? LocalDateTime.from(startDate) : null;
    }

    public LocalDateTime getEndDate() {
        return endDate != null ? LocalDateTime.from(endDate) : null;
    }

    public void setEndDate(final LocalDateTime endDate) {
        this.endDate = endDate != null ? LocalDateTime.from(endDate) : null;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CalendarEntry.class.getSimpleName() + "[", "]")
                .add("id=" + id).add("bookingPersonName='" + bookingPersonName + "'").add("startDate=" + startDate)
                .add("endDate=" + endDate).toString();
    }
}
