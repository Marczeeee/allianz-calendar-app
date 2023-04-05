package hu.hmarton.allianz.calendar.controller.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.hmarton.allianz.calendar.AllianzCalendarApp;
import hu.hmarton.allianz.calendar.model.CalendarEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Date;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AllianzCalendarApp.class)
@AutoConfigureMockMvc
public class ReservationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void createNewReservationSuccessful() throws Exception {
        final CalendarEntry newCalendarEntry = new CalendarEntry();
        newCalendarEntry.setBookingPersonName("abcdef");
        newCalendarEntry.setStartDate(new Date());
        newCalendarEntry.setEndDate(new Date());

        final ObjectMapper objectMapper = new ObjectMapper();
        final String jsonContent =
                objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(newCalendarEntry);

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
