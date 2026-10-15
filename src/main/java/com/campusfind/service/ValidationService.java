package com.campusfind.service;

import com.campusfind.exception.ValidationException;
import com.campusfind.model.Claim;
import com.campusfind.model.FoundItem;

import java.time.LocalDate;

/**
 * Service responsible for business input validation.
 * Centralizes validation logic to keep DAOs and UI classes clean.
 */
public class ValidationService {

    /**
     * Validates an item to be reported found.
     *
     * @param item the item to validate
     * @throws ValidationException if any business rule is violated
     */
    public void validateFoundItem(FoundItem item) {
        if (item == null) {
            throw new ValidationException("Found item cannot be null.");
        }
        validateNonEmpty(item.getItemName(), "Item name", 3, 100);
        validateNonEmpty(item.getCategory(), "Category", 2, 50);
        validateNonEmpty(item.getDescription(), "Public description", 5, 500);
        validateNonEmpty(item.getLocationFound(), "Location found", 2, 100);
        validateNonEmpty(item.getReportedBy(), "Reported by (finder name)", 2, 100);
        validateNonEmpty(item.getPrivateVerificationClue(), "Private verification clue", 3, 255);

        if (item.getDateFound() == null) {
            throw new ValidationException("Date found must be specified.");
        }
        if (item.getDateFound().isAfter(LocalDate.now())) {
            throw new ValidationException("Date found cannot be in the future (Entered: " + item.getDateFound() + ").");
        }
    }

    /**
     * Validates a student's lost-item report.
     *
     * @param item the lost item to validate
     * @throws ValidationException if any validation rule is violated
     */
    public void validateLostItem(com.campusfind.model.LostItem item) {
        if (item == null) {
            throw new ValidationException("Lost item cannot be null.");
        }
        validateNonEmpty(item.getItemName(), "Item name", 3, 100);
        validateNonEmpty(item.getCategory(), "Category", 2, 50);
        validateNonEmpty(item.getDescription(), "Description", 5, 500);
        validateNonEmpty(item.getLocationLost(), "Location lost / last seen", 2, 100);
        validateNonEmpty(item.getStudentName(), "Student name", 2, 100);
        validateNonEmpty(item.getStudentContact(), "Student contact details", 5, 100);
        validateNonEmpty(item.getIdentifyingDetails(), "Identifying details / secret marks", 3, 255);

        if (item.getDateLost() == null) {
            throw new ValidationException("Date lost must be specified.");
        }
        if (item.getDateLost().isAfter(LocalDate.now())) {
            throw new ValidationException("Date lost cannot be in the future (Entered: " + item.getDateLost() + ").");
        }
    }

    /**
     * Validates a newly submitted claim.
     *
     * @param claim the claim to validate
     * @throws ValidationException if invalid
     */
    public void validateClaim(Claim claim) {
        if (claim == null) {
            throw new ValidationException("Claim cannot be null.");
        }
        if (claim.getItemId() == null || claim.getItemId() <= 0) {
            throw new ValidationException("A valid positive item ID is required to file a claim.");
        }
        validateNonEmpty(claim.getClaimantName(), "Claimant name", 2, 100);
        validateNonEmpty(claim.getClaimantContact(), "Claimant contact details", 5, 100);
        validateNonEmpty(claim.getClaimDescription(), "Claim description", 5, 1000);
        validateNonEmpty(claim.getVerificationResponse(), "Private verification answer", 2, 255);
    }

    private void validateNonEmpty(String value, String fieldName, int minLength, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
        String trimmed = value.trim();
        if (trimmed.length() < minLength) {
            throw new ValidationException(fieldName + " must be at least " + minLength + " characters long.");
        }
        if (trimmed.length() > maxLength) {
            throw new ValidationException(fieldName + " cannot exceed " + maxLength + " characters.");
        }
    }
}
