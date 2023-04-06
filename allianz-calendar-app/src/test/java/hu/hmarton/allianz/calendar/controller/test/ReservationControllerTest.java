package hu.hmarton.allianz.calendar.controller.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hu.hmarton.allianz.calendar.AllianzCalendarApp;
import hu.hmarton.allianz.calendar.exc.ValidationErrorMessages;
import hu.hmarton.allianz.calendar.model.CalendarEntry;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), createValidStartDateAtNextMonday(), Duration.of(1, ChronoUnit.HOURS)));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void createNewReservationWithMissingPersonName() throws Exception {
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createRandomNewCalendarEntry(false,
                        true, true));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void createNewReservationWithMissingStartDate() throws Exception {
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createRandomNewCalendarEntry(true,
                        false, true));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void createNewReservationWithMissingEndDate() throws Exception {
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createRandomNewCalendarEntry(true,
                        true, false));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void createNewReservationToLastWeek() throws Exception {
        final LocalDateTime startDateAtLastWeek = LocalDateTime.now().minus(8, ChronoUnit.DAYS);
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), startDateAtLastWeek, Duration.of(30, ChronoUnit.MINUTES)));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string(ValidationErrorMessages.VALIDATION_ERROR_START_DATE_MUST_BE_IN_FUTURE));
    }

    @Test
    public void createNewReservationTooShort() throws Exception {
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), createValidStartDateAtNextMonday(), Duration.of(1,
                                ChronoUnit.MINUTES)));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_LENGTH_AT_LEAST_30MIN));
    }

    @Test
    public void createNewReservationTooLong() throws Exception {
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), createValidStartDateAtNextMonday(), Duration.of(4,
                                ChronoUnit.HOURS)));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_LENGTH_MAX_3HOURS));
    }

    @Test
    public void createNewReservationStartingAt15Minutes() throws Exception {
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), createValidStartDateAtNextMonday().withMinute(15),
                        Duration.of(2, ChronoUnit.HOURS)));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string(ValidationErrorMessages.VALIDATION_ERROR_START_AT_00MIN_OR_30MIN_ONLY));
    }

    @Test
    public void createNewReservationOnWeekend() throws Exception {
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), createValidStartDateAtNextMonday().plus(5, ChronoUnit.DAYS),
                        Duration.of(2, ChronoUnit.HOURS)));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_MUST_BE_ON_WEEKDAY));
    }

    @Test
    public void createNewReservationStartBefore9AM() throws Exception {
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), createValidStartDateAtNextMonday().withHour(5),
                        Duration.of(2, ChronoUnit.HOURS)));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_MUST_START_AFTER_9AM));
    }

    @Test
    public void createNewReservationEndAfter5PM() throws Exception {
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), createValidStartDateAtNextMonday().withHour(16),
                        Duration.of(2, ChronoUnit.HOURS)));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string(ValidationErrorMessages.VALIDATION_ERROR_RESERVATION_MUST_END_BEFORE_5PM));
    }



    private CalendarEntry createRandomNewCalendarEntry(final boolean withPersonName, final boolean withStartDate,
                                                       final boolean withEndDate) {
        final CalendarEntry newCalendarEntry = new CalendarEntry();
        newCalendarEntry.setBookingPersonName(withPersonName ? RandomStringUtils.randomAlphabetic(8, 16) : null);
        newCalendarEntry.setStartDate(withStartDate ? LocalDateTime.now() : null);
        newCalendarEntry.setEndDate(withEndDate ? LocalDateTime.now() : null);
        return newCalendarEntry;
    }

    private CalendarEntry createNewCalendarEntry(final String personName, final LocalDateTime startDate,
                                                 final Duration duration) {
        final CalendarEntry newCalendarEntry = new CalendarEntry();
        newCalendarEntry.setBookingPersonName(personName);
        newCalendarEntry.setStartDate(startDate);
        if (duration != null) {
            final LocalDateTime endDate = startDate.plus(duration);
            newCalendarEntry.setEndDate(endDate);
        }
        return newCalendarEntry;
    }

    private ObjectMapper createJsonObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    private LocalDateTime createValidStartDateAtNextMonday() {
        final LocalDateTime now = LocalDateTime.now();
        final DayOfWeek dayOfWeek = now.getDayOfWeek();
        final int dayOfWeekValue = dayOfWeek.getValue();
        final LocalDateTime nextMonday =
                now.plus((7 - dayOfWeekValue) + 1, ChronoUnit.DAYS).withHour(10).withMinute(0).withSecond(0);
        return nextMonday;
    }
}
