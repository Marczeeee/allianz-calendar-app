package hu.hmarton.allianz.calendar.dto;

import java.io.Serializable;
import java.util.Date;

/** Data class representing an open slot in the calendar. */
public class OpenSlotDTO implements Serializable {
    /** Start of the open slot. */
    private Date slotStartDate;
    /** End of the open slot. */
    private Date slotEndDate;

    public Date getSlotStartDate() {
        return slotStartDate != null ? new Date(slotStartDate.getTime()) : null;
    }

    public void setSlotStartDate(final Date slotStartDate) {
        this.slotStartDate = slotStartDate != null ? new Date(slotStartDate.getTime()) : null;
    }

    public Date getSlotEndDate() {
        return slotEndDate != null ? new Date(slotEndDate.getTime()) : null;
    }

    public void setSlotEndDate(final Date slotEndDate) {
        this.slotEndDate = slotEndDate != null ? new Date(slotEndDate.getTime()) : null;
    }
}
