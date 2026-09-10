package com.openmosque.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an unauthenticated client tries to access a protected endpoint,
 * or when an authentication token is expired, missing, or malformed.
 * 
 * WHY THIS IS PRESENT:
 * Results in an HTTP 401 UNAUTHORIZED response.
 */
public class UnauthorizedException extends ApiException {

    /**
     * Constructs an UnauthorizedException with an authentication failure message.
     * 
     * @param message Description of the authentication error.
     */
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
