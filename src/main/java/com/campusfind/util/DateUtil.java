package com.campusfind.util;

import com.campusfind.exception.ValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class providing safe and consistent Date/Time formatting and parsing.
 * Uses Java 8+ java.time API (immutable, thread-safe).
 */
public final class DateUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateUtil() {
        // Prevent instantiation of utility class
    }

    /**
     * Parses a date string in 'yyyy-MM-dd' format into a {@link LocalDate}.
     *
     * @param dateString user input string
     * @return parsed LocalDate
     * @throws ValidationException if format is invalid
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            throw new ValidationException("Date cannot be empty. Please use format YYYY-MM-DD.");
        }
        try {
            return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format: '" + dateString + "'. Please enter date in YYYY-MM-DD format (e.g., 2026-10-15).");
        }
    }

    /**
     * Formats a {@link LocalDate} to 'yyyy-MM-dd'.
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formats a {@link LocalDateTime} to 'yyyy-MM-dd HH:mm:ss'.
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DATE_TIME_FORMATTER);
    }
}
