package com.saiket.task2;

/**
 * Custom Exception thrown when an error occurs during API communication or response processing.
 */
public class ApiException extends Exception {

    private final int statusCode;

    public ApiException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public ApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /**
     * Gets the HTTP status code associated with this error, or -1 if no HTTP code applies.
     */
    public int getStatusCode() {
        return statusCode;
    }
}
