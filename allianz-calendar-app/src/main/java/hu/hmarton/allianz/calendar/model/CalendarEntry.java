package hu.hmarton.allianz.calendar.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

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
    private Date startDate;
    /** End date of the reservation. */
    @NotNull(message = "Reservation end date is mandatory")
    private Date endDate;

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

    public Date getStartDate() {
        return startDate != null ? new Date(startDate.getTime()) : null;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate != null ? new Date(startDate.getTime()) : null;
    }

    public Date getEndDate() {
        return endDate != null ? new Date(endDate.getTime()) : null;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate != null ? new Date(endDate.getTime()) : null;;
    }
}
