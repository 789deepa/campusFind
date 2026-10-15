package com.campusfind.util;

import com.campusfind.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DateUtil Utility Tests")
class DateUtilTest {

    @Test
    @DisplayName("parseDate correctly parses valid YYYY-MM-DD format")
    void testParseValidDate() {
        LocalDate date = DateUtil.parseDate("2026-10-15");
        assertEquals(2026, date.getYear());
        assertEquals(10, date.getMonthValue());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    @DisplayName("parseDate throws ValidationException on invalid format or empty input")
    void testParseInvalidDate() {
        assertThrows(ValidationException.class, () -> DateUtil.parseDate("15-10-2026"));
        assertThrows(ValidationException.class, () -> DateUtil.parseDate("invalid-date"));
        assertThrows(ValidationException.class, () -> DateUtil.parseDate(""));
        assertThrows(ValidationException.class, () -> DateUtil.parseDate(null));
    }

    @Test
    @DisplayName("formatDate and formatDateTime format properly or return N/A on null")
    void testFormatDateMethods() {
        LocalDate date = LocalDate.of(2026, 10, 15);
        assertEquals("2026-10-15", DateUtil.formatDate(date));
        assertEquals("N/A", DateUtil.formatDate(null));

        LocalDateTime dt = LocalDateTime.of(2026, 10, 15, 14, 30, 45);
        assertEquals("2026-10-15 14:30:45", DateUtil.formatDateTime(dt));
        assertEquals("N/A", DateUtil.formatDateTime(null));
    }
}
