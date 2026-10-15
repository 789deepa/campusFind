package com.campusfind.exception;

/**
 * Thrown when user-provided input fails business or sanity validation rules
 * (e.g. empty required fields, future dates, invalid contact format).
 */
public class ValidationException extends CampusFindException {
    public ValidationException(String message) {
        super(message);
    }
}
