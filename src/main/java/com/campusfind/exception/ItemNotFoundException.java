package com.campusfind.exception;

/**
 * Thrown when an operation targets an item ID that does not exist in the database.
 */
public class ItemNotFoundException extends CampusFindException {
    private final Integer itemId;

    public ItemNotFoundException(Integer itemId) {
        super("Found item with ID " + itemId + " was not found.");
        this.itemId = itemId;
    }

    public Integer getItemId() {
        return itemId;
    }
}
