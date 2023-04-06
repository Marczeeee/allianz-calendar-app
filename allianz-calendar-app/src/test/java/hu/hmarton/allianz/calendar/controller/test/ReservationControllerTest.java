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

/**
 * Testing reservations for several scenarios. Be aware that each test case uses the same database instance, so
 * every successful reservation will remain in the database when the next test case runs!
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AllianzCalendarApp.class)
@AutoConfigureMockMvc
public class ReservationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void createNewReservation_Success() throws Exception {
        //Next Monday from 9:00-10:00
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), createValidStartDateAtNextMonday(), Duration.of(1, ChronoUnit.HOURS)));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void createNewReservationWithMissingPersonName_Error() throws Exception {
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createRandomNewCalendarEntry(false,
                        true, true));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void createNewReservationWithMissingStartDate_Error() throws Exception {
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createRandomNewCalendarEntry(true,
                        false, true));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void createNewReservationWithMissingEndDate_Error() throws Exception {
        final String jsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createRandomNewCalendarEntry(true,
                        true, false));

        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void createNewReservationToLastWeek_Error() throws Exception {
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
    public void createNewReservationTooShort_Error() throws Exception {
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
    public void createNewReservationTooLong_Error() throws Exception {
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
    public void createNewReservationStartingAt15Minutes_Error() throws Exception {
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
    public void createNewReservationOnWeekend_Error() throws Exception {
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
    public void createNewReservationStartBefore9AM_Error() throws Exception {
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
    public void createNewReservationEndAfter5PM_Error() throws Exception {
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

    @Test
    public void createOverlappingReservationsByStartDateOverlapping_Error() throws Exception {
        //Next Monday from 10:00-12:00
        final LocalDateTime firstReservationStartDate =
                createValidStartDateAtNextMonday().withHour(10).withMinute(0);
        final String firstJsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), firstReservationStartDate,
                        Duration.of(2, ChronoUnit.HOURS)));
        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(firstJsonContent))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        final LocalDateTime secondReservationStartDate = firstReservationStartDate.withMinute(30);
        final String secondJsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), secondReservationStartDate,
                        Duration.of(2, ChronoUnit.HOURS)));
        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(secondJsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string(ValidationErrorMessages.VALIDATION_ERROR_DATES_OVERLAPPING_WITH_EXISTING_RESERVATION));
    }

    @Test
    public void createOverlappingReservationsByEndDateOverlapping_Error() throws Exception {
        //Next Monday from 14:00-15:00
        final LocalDateTime firstReservationStartDate =
                createValidStartDateAtNextMonday().withHour(14).withMinute(0);
        final String firstJsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), firstReservationStartDate,
                        Duration.of(1, ChronoUnit.HOURS)));
        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(firstJsonContent))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        final LocalDateTime secondReservationStartDate = firstReservationStartDate.withHour(13).withMinute(30);
        final String secondJsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), secondReservationStartDate,
                        Duration.of(1, ChronoUnit.HOURS)));
        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(secondJsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string(ValidationErrorMessages.VALIDATION_ERROR_DATES_OVERLAPPING_WITH_EXISTING_RESERVATION));
    }

    @Test
    public void createOverlappingReservationsBySameDates_Error() throws Exception {
        //Next Monday from 15:00-16:00
        final LocalDateTime firstReservationStartDate =
                createValidStartDateAtNextMonday().withHour(15).withMinute(0);
        final String firstJsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), firstReservationStartDate,
                        Duration.of(1, ChronoUnit.HOURS)));
        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(firstJsonContent))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        final String secondJsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), firstReservationStartDate,
                        Duration.of(1, ChronoUnit.HOURS)));
        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(secondJsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string(ValidationErrorMessages.VALIDATION_ERROR_DATES_OVERLAPPING_WITH_EXISTING_RESERVATION));
    }

    @Test
    public void createOverlappingReservationsByWholeWithin_Error() throws Exception {
        //Next Tuesday from 9:00-12:00
        final LocalDateTime firstReservationStartDate =
                createValidStartDateAtNextMonday().plusDays(1).withHour(9).withMinute(0);
        final String firstJsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), firstReservationStartDate,
                        Duration.of(3, ChronoUnit.HOURS)));
        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(firstJsonContent))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        final LocalDateTime secondReservationStartDate = firstReservationStartDate.withHour(10).withMinute(0);
        final String secondJsonContent =
                createJsonObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(createNewCalendarEntry(
                        RandomStringUtils.randomAlphabetic(8, 16), secondReservationStartDate,
                        Duration.of(1, ChronoUnit.HOURS)));
        mvc.perform(MockMvcRequestBuilders.post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON).content(secondJsonContent))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string(ValidationErrorMessages.VALIDATION_ERROR_DATES_OVERLAPPING_WITH_EXISTING_RESERVATION));
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
                now.plus((7 - dayOfWeekValue) + 1, ChronoUnit.DAYS).withHour(9).withMinute(0).withSecond(0);
        return nextMonday;
    }
}
