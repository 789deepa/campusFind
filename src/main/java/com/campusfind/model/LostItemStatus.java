package com.campusfind.model;

/**
 * Represents the lifecycle status of a lost-item report filed by a student.
 * 
 * <p>Lifecycle:
 * <ul>
 *   <li>{@link #OPEN}: Student filed a lost report; no approved claim/match has closed it yet.</li>
 *   <li>{@link #MATCHED}: An associated claim has been approved by staff, pending physical handover.</li>
 *   <li>{@link #RESOLVED}: The item was verified and returned to the student; report is finalized.</li>
 * </ul>
 * </p>
 */
public enum LostItemStatus {
    OPEN("Open"),
    MATCHED("Matched"),
    RESOLVED("Resolved");

    private final String displayName;

    LostItemStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converts a database status string into its matching enum constant.
     */
    public static LostItemStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Lost item status cannot be null or empty");
        }
        for (LostItemStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim()) ||
                status.displayName.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown lost item status: '" + value + "'. Expected OPEN, MATCHED, or RESOLVED.");
    }
}
