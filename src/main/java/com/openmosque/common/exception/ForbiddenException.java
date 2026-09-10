package com.openmosque.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an authenticated user attempts an operation they do not have sufficient role permissions for
 * (e.g. A regular USER attempting to approve a mosque submission or edit another mosque's prayer timings).
 * 
 * WHY THIS IS PRESENT:
 * Results in an HTTP 403 FORBIDDEN response.
 */
public class ForbiddenException extends ApiException {

    /**
     * Constructs a ForbiddenException with an authorization failure message.
     * 
     * @param message Description of the permission restriction.
     */
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}
