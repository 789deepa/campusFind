package com.campusfind.exception;

/**
 * Thrown when a student attempts to file multiple pending claims for the exact same item.
 */
public class DuplicateClaimException extends CampusFindException {
    public DuplicateClaimException(String message) {
        super(message);
    }
}
