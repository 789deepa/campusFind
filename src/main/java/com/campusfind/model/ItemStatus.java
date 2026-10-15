package com.campusfind.model;

/**
 * Represents the lifecycle status of a found item on campus.
 * 
 * <p>Why Enum instead of String?
 * Enums enforce type-safety at compile time, eliminating invalid status values,
 * typos, and the need for repetitive string comparisons throughout the application.
 * </p>
 */
public enum ItemStatus {
    /**
     * Item has been reported and securely logged in the repository.
     * Open for public viewing and new claims.
     */
    AVAILABLE("Available"),

    /**
     * One or more claims have been submitted by students and are under review.
     * Other claimants can still view the item, but reviewers are currently assessing claims.
     */
    CLAIM_PENDING("Claim Pending"),

    /**
     * Item has been successfully verified, claimed, and physically returned to its rightful owner.
     * Terminal state; cannot be claimed again.
     */
    RETURNED("Returned");

    private final String displayName;

    ItemStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converts a database status string into its matching enum constant.
     * Case-insensitive for defensive programming.
     *
     * @param value raw status string from DB
     * @return corresponding ItemStatus enum constant
     * @throws IllegalArgumentException if the status string is unrecognized
     */
    public static ItemStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Item status cannot be null or empty");
        }
        for (ItemStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim()) ||
                status.displayName.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown item status: '" + value + "'. Expected AVAILABLE, CLAIM_PENDING, or RETURNED.");
    }
}
