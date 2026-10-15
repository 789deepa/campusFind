package com.campusfind.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LostItem Entity Tests")
class LostItemTest {

    @Test
    @DisplayName("toString() MUST NOT leak confidential identifying details")
    void testToStringRedaction() {
        String secretDetails = "Custom sticker with phone number 9876543210 and silver scrape on corner";
        LostItem item = new LostItem("Blue Backpack", "Bags", "Nike backpack lost in library",
                "Central Library 2nd Floor", LocalDate.now(), "Rohan Gupta", "rohan@campus.edu", secretDetails);

        String stringOutput = item.toString();

        assertFalse(stringOutput.contains(secretDetails),
                "Privacy violation: toString() revealed confidential identifying details!");
        assertTrue(stringOutput.contains("[REDACTED]"),
                "toString() should clearly denote identifying details are [REDACTED].");
    }

    @Test
    @DisplayName("Authorized getter retrieves confidential identifying details")
    void testAuthorizedGetter() {
        String secretDetails = "Serial number ending in -9942";
        LostItem item = new LostItem("Dell Laptop", "Electronics", "Black XPS 13",
                "Lab 4", LocalDate.now(), "Sneha", "sneha@campus.edu", secretDetails);

        assertEquals(secretDetails, item.getIdentifyingDetails());
    }

    @Test
    @DisplayName("Newly created lost item defaults to OPEN status with non-null dateReported")
    void testDefaultStatus() {
        LostItem item = new LostItem();
        assertEquals(LostItemStatus.OPEN, item.getStatus());
        assertNotNull(item.getDateReported());
    }

    @Test
    @DisplayName("Entities with identical IDs are considered equal")
    void testEqualsAndHashCode() {
        LostItem item1 = new LostItem(10, "Keys", "Accessories", "Car keys", "Parking Lot",
                LocalDate.now(), LocalDateTime.now(), "Amit", "amit@campus.edu", "Silver keychain", LostItemStatus.OPEN);
        LostItem item2 = new LostItem(10, "Keys", "Accessories", "Car keys", "Parking Lot",
                LocalDate.now(), LocalDateTime.now(), "Amit", "amit@campus.edu", "Silver keychain", LostItemStatus.OPEN);
        LostItem item3 = new LostItem(20, "Keys", "Accessories", "Car keys", "Parking Lot",
                LocalDate.now(), LocalDateTime.now(), "Amit", "amit@campus.edu", "Silver keychain", LostItemStatus.OPEN);

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
        assertNotEquals(item1, item3);
    }
}
