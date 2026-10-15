package com.campusfind.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LostItemStatus Enum Tests")
class LostItemStatusTest {

    @ParameterizedTest
    @ValueSource(strings = {"OPEN", "open", "Open", "MATCHED", "matched", "Matched", "RESOLVED", "resolved", "Resolved"})
    @DisplayName("fromString handles valid enum names and display strings case-insensitively")
    void testValidStatusParsing(String input) {
        LostItemStatus status = LostItemStatus.fromString(input);
        assertNotNull(status);
    }

    @Test
    @DisplayName("fromString throws IllegalArgumentException on invalid status")
    void testInvalidStatusParsing() {
        assertThrows(IllegalArgumentException.class, () -> LostItemStatus.fromString("UNKNOWN_STATUS"));
        assertThrows(IllegalArgumentException.class, () -> LostItemStatus.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> LostItemStatus.fromString(null));
    }
}
