package com.openmosque.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when client requests contain invalid, malformed, or illegal business arguments.
 * 
 * WHY THIS IS PRESENT:
 * Signals a 400 Bad Request to the client (e.g. invalid geographic coordinates, invalid prayer calculation method).
 */
public class BadRequestException extends ApiException {

    /**
     * Constructs a BadRequestException with a descriptive error message.
     * 
     * @param message Human-readable explanation of why the request is invalid.
     */
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }
}
