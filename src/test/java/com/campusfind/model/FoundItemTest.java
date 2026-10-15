package com.campusfind.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FoundItem Entity Tests")
class FoundItemTest {

    @Test
    @DisplayName("toString() MUST NOT leak private verification clue")
    void testToStringRedaction() {
        String secretClue = "SUPER_SECRET_ENGRAVED_INITIALS_991";
        FoundItem item = new FoundItem("Scientific Calculator", "Electronics", "Casio FX calculator",
                "Library Hall", LocalDate.now(), "Prof. Kumar", secretClue);

        String stringOutput = item.toString();

        assertFalse(stringOutput.contains(secretClue),
                "Privacy violation: toString() revealed the private verification clue!");
        assertTrue(stringOutput.contains("[REDACTED]"),
                "toString() should clearly denote the private verification clue is [REDACTED].");
    }

    @Test
    @DisplayName("Authorized getter retrieves the exact private clue")
    void testAuthorizedGetter() {
        String secretClue = "Blue dragon sticker on inner backplate";
        FoundItem item = new FoundItem("MacBook Air", "Electronics", "Midnight blue laptop",
                "Cafe 2", LocalDate.now(), "Security", secretClue);

        assertEquals(secretClue, item.getPrivateVerificationClue());
    }

    @Test
    @DisplayName("Newly created item defaults to AVAILABLE status")
    void testDefaultStatus() {
        FoundItem item = new FoundItem();
        assertEquals(ItemStatus.AVAILABLE, item.getStatus());
        assertNotNull(item.getDateReported());
    }

    @Test
    @DisplayName("Entities with identical IDs are considered equal")
    void testEqualsAndHashCode() {
        FoundItem item1 = new FoundItem(10, "Keys", "Keys", "Ring of keys", "Gate",
                LocalDate.now(), LocalDateTime.now(), "Guard", "Red tag", ItemStatus.AVAILABLE);
        FoundItem item2 = new FoundItem(10, "Keys", "Keys", "Ring of keys", "Gate",
                LocalDate.now(), LocalDateTime.now(), "Guard", "Red tag", ItemStatus.AVAILABLE);
        FoundItem item3 = new FoundItem(20, "Keys", "Keys", "Ring of keys", "Gate",
                LocalDate.now(), LocalDateTime.now(), "Guard", "Red tag", ItemStatus.AVAILABLE);

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
        assertNotEquals(item1, item3);
    }
}
