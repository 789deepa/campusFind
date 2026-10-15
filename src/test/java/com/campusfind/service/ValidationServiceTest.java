package com.campusfind.service;

import com.campusfind.exception.ValidationException;
import com.campusfind.model.Claim;
import com.campusfind.model.FoundItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationService Tests")
class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Test
    @DisplayName("Valid found item passes validation without throwing")
    void testValidItemPasses() {
        FoundItem item = new FoundItem("Scientific Calculator", "Electronics",
                "Casio fx-991EX left in exam hall", "Hall 101",
                LocalDate.now(), "Prof. Sharma", "Serial 10293 inside cover");

        assertDoesNotThrow(() -> validationService.validateFoundItem(item));
    }

    @Test
    @DisplayName("Validation fails when found item is null")
    void testNullItemFails() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validationService.validateFoundItem(null));
        assertTrue(ex.getMessage().contains("Found item cannot be null"));
    }

    @Test
    @DisplayName("Validation fails when item name is empty or too short")
    void testEmptyItemNameFails() {
        FoundItem item = new FoundItem("  ", "Electronics", "Description", "Location",
                LocalDate.now(), "Reporter", "Clue");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> validationService.validateFoundItem(item));
        assertTrue(ex.getMessage().contains("Item name cannot be empty"));
    }

    @Test
    @DisplayName("Validation fails when date found is in the future")
    void testFutureDateFails() {
        FoundItem item = new FoundItem("Dell Charger", "Electronics", "Black charger", "Library",
                LocalDate.now().plusDays(2), "Reporter", "Clue with tape");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> validationService.validateFoundItem(item));
        assertTrue(ex.getMessage().contains("cannot be in the future"));
    }

    @Test
    @DisplayName("Validation fails when private verification clue is missing")
    void testMissingPrivateClueFails() {
        FoundItem item = new FoundItem("Water Bottle", "Others", "Blue flask", "Cafeteria",
                LocalDate.now(), "Reporter", "   ");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> validationService.validateFoundItem(item));
        assertTrue(ex.getMessage().contains("Private verification clue"));
    }

    @Test
    @DisplayName("Valid claim passes validation")
    void testValidClaimPasses() {
        Claim claim = new Claim(1, "Rohan Deshmukh", "rohan@campus.edu",
                "Left it on the table during lunchtime", "Dented near the bottom base");

        assertDoesNotThrow(() -> validationService.validateClaim(claim));
    }

    @Test
    @DisplayName("Claim fails when item ID is missing or non-positive")
    void testInvalidClaimItemIdFails() {
        Claim zeroIdClaim = new Claim(0, "Rohan", "rohan@campus.edu", "Story", "Answer");
        Claim nullIdClaim = new Claim(null, "Rohan", "rohan@campus.edu", "Story", "Answer");

        assertThrows(ValidationException.class, () -> validationService.validateClaim(zeroIdClaim));
        assertThrows(ValidationException.class, () -> validationService.validateClaim(nullIdClaim));
    }

    @Test
    @DisplayName("Claim fails when claimant name or contact is empty")
    void testEmptyClaimantFieldsFail() {
        Claim claim = new Claim(1, "   ", "rohan@campus.edu", "Story", "Answer");
        assertThrows(ValidationException.class, () -> validationService.validateClaim(claim));

        Claim claim2 = new Claim(1, "Rohan", "  ", "Story", "Answer");
        assertThrows(ValidationException.class, () -> validationService.validateClaim(claim2));
    }

    @Test
    @DisplayName("Claim fails when private verification answer is missing")
    void testMissingVerificationAnswerFails() {
        Claim claim = new Claim(1, "Rohan", "rohan@campus.edu", "Story", "   ");
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validationService.validateClaim(claim));
        assertTrue(ex.getMessage().contains("Private verification answer"));
    }

    @Test
    @DisplayName("Valid lost item passes validation without throwing")
    void testValidLostItemPasses() {
        com.campusfind.model.LostItem item = new com.campusfind.model.LostItem("Blue Umbrella", "Accessories",
                "Navy blue folding umbrella left near canteen", "Food Court",
                LocalDate.now(), "Vikram Singh", "vikram@campus.edu", "Small scratch on curved handle");

        assertDoesNotThrow(() -> validationService.validateLostItem(item));
    }

    @Test
    @DisplayName("Validation fails when lost item is null")
    void testNullLostItemFails() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validationService.validateLostItem(null));
        assertTrue(ex.getMessage().contains("Lost item cannot be null"));
    }

    @Test
    @DisplayName("Validation fails when lost item date is in future")
    void testFutureLostDateFails() {
        com.campusfind.model.LostItem item = new com.campusfind.model.LostItem("Blue Umbrella", "Accessories",
                "Navy blue folding umbrella", "Food Court",
                LocalDate.now().plusDays(3), "Vikram", "vikram@campus.edu", "Small scratch");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> validationService.validateLostItem(item));
        assertTrue(ex.getMessage().contains("cannot be in the future"));
    }

    @Test
    @DisplayName("Validation fails when identifying details are missing for lost item")
    void testMissingIdentifyingDetailsFails() {
        com.campusfind.model.LostItem item = new com.campusfind.model.LostItem("Blue Umbrella", "Accessories",
                "Navy blue folding umbrella", "Food Court",
                LocalDate.now(), "Vikram", "vikram@campus.edu", "   ");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> validationService.validateLostItem(item));
        assertTrue(ex.getMessage().contains("Identifying details"));
    }
}
