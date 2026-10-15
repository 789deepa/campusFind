package com.campusfind.exception;

/**
 * Thrown when an underlying JDBC SQLException occurs.
 * Wraps technical SQL errors into an application exception without exposing internal details to UI users.
 */
public class DatabaseOperationException extends CampusFindException {
    public DatabaseOperationException(String message) {
        super(message);
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
