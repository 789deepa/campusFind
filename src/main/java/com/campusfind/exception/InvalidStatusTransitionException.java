package com.campusfind.exception;

/**
 * Thrown when an illegal state change is attempted (e.g., claiming an already returned item,
 * or returning an item without an approved claim).
 */
public class InvalidStatusTransitionException extends CampusFindException {
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
