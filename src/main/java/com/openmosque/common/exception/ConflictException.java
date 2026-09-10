package com.openmosque.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an operation violates a unique constraint or conflicts with existing state
 * (e.g. User email already exists, Mosque slug conflict).
 * 
 * WHY THIS IS PRESENT:
 * Results in an HTTP 409 CONFLICT response.
 */
public class ConflictException extends ApiException {

    /**
     * Constructs a ConflictException with a conflict description message.
     * 
     * @param message Description of the conflicting resource.
     */
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS");
    }
}
