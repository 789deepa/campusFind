package com.campusfind.exception;

/**
 * Base custom unchecked exception for the CampusFind application.
 * All domain-specific business exceptions inherit from this class.
 */
public class CampusFindException extends RuntimeException {
    public CampusFindException(String message) {
        super(message);
    }

    public CampusFindException(String message, Throwable cause) {
        super(message, cause);
    }
}
