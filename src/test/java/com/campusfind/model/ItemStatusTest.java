package com.campusfind.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ItemStatus Enum Tests")
class ItemStatusTest {

    @ParameterizedTest
    @ValueSource(strings = {"AVAILABLE", "available", "Available", "Claim Pending", "CLAIM_PENDING", "RETURNED", "returned"})
    @DisplayName("fromString handles valid enum names and display strings case-insensitively")
    void testValidStatusParsing(String input) {
        ItemStatus status = ItemStatus.fromString(input);
        assertNotNull(status);
    }

    @Test
    @DisplayName("fromString throws IllegalArgumentException on invalid status")
    void testInvalidStatusParsing() {
        assertThrows(IllegalArgumentException.class, () -> ItemStatus.fromString("UNKNOWN_STATUS"));
        assertThrows(IllegalArgumentException.class, () -> ItemStatus.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> ItemStatus.fromString(null));
    }
}
