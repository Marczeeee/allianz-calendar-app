package hu.hmarton.allianz.calendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application main class.
 */
@SpringBootApplication
public class AllianzCalendarApp {
    /**
     * Application main entry point.
     * @param args Application arguments
     * @throws Exception If any exception occurs during initialization of the application
     */
    public static void main(final String[] args) throws Exception {
        SpringApplication.run(AllianzCalendarApp.class, args);
    }
}
