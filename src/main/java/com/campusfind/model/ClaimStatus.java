package com.campusfind.model;

/**
 * Represents the evaluation status of a claim submitted for a found item.
 */
public enum ClaimStatus {
    /**
     * Claim is newly submitted and waiting for administrator / staff review.
     */
    PENDING("Pending"),

    /**
     * Claimant's verification response matched the private verification clue.
     * Ready for item handover / return.
     */
    APPROVED("Approved"),

    /**
     * Claimant's response did not match the private clue, or the claim was fraudulent / invalid.
     */
    REJECTED("Rejected");

    private final String displayName;

    ClaimStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converts a database status string into its matching enum constant.
     *
     * @param value raw status string
     * @return corresponding ClaimStatus
     * @throws IllegalArgumentException if invalid
     */
    public static ClaimStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Claim status cannot be null or empty");
        }
        for (ClaimStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim()) ||
                status.displayName.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown claim status: '" + value + "'. Expected PENDING, APPROVED, or REJECTED.");
    }
}
