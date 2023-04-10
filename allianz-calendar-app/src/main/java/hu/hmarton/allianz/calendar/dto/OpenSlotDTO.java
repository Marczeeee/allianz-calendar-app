package hu.hmarton.allianz.calendar.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Data class representing an open slot in the calendar. */
public class OpenSlotDTO implements Serializable {
    /** Start of the open slot. */
    private LocalDateTime slotStartDate;
    /** End of the open slot. */
    private LocalDateTime slotEndDate;

    public LocalDateTime getSlotStartDate() {
        return slotStartDate != null ? LocalDateTime.from(slotStartDate) : null;
    }

    public void setSlotStartDate(final LocalDateTime slotStartDate) {
        this.slotStartDate = slotStartDate != null ? LocalDateTime.from(slotStartDate) : null;
    }

    public LocalDateTime getSlotEndDate() {
        return slotEndDate != null ? LocalDateTime.from(slotEndDate) : null;
    }

    public void setSlotEndDate(final LocalDateTime slotEndDate) {
        this.slotEndDate = slotEndDate != null ? LocalDateTime.from(slotEndDate) : null;
    }
}
