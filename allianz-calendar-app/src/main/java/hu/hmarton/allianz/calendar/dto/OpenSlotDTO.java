package hu.hmarton.allianz.calendar.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.StringJoiner;

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

    @Override
    public String toString() {
        return new StringJoiner(", ", OpenSlotDTO.class.getSimpleName() + "[", "]").add("slotStartDate="
                + slotStartDate).add("slotEndDate=" + slotEndDate).toString();
    }
}
