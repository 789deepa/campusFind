package com.campusfind.model;

/**
 * Standard campus item categories to ensure clean grouping and filtering.
 */
public enum ItemCategory {
    ELECTRONICS("Electronics"),
    DOCUMENTS("Documents"),
    ACCESSORIES("Accessories"),
    CLOTHING("Clothing"),
    BOOKS("Books"),
    KEYS("Keys"),
    OTHERS("Others");

    private final String displayName;

    ItemCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolves an input string into a standard category or falls back to OTHERS.
     */
    public static ItemCategory fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return OTHERS;
        }
        for (ItemCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(text.trim()) ||
                cat.displayName.equalsIgnoreCase(text.trim())) {
                return cat;
            }
        }
        return OTHERS;
    }
}
