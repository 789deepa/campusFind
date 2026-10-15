package com.campusfind.exception;

/**
 * Thrown when an operation references a lost-item report ID that does not exist.
 */
public class LostItemNotFoundException extends CampusFindException {
    private final Integer lostItemId;

    public LostItemNotFoundException(Integer lostItemId) {
        super("Lost-item report with ID " + lostItemId + " was not found.");
        this.lostItemId = lostItemId;
    }

    public Integer getLostItemId() {
        return lostItemId;
    }
}
