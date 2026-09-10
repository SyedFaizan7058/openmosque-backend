package com.openmosque.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base unchecked exception for all OpenMosque business and API domain exceptions.
 * 
 * WHY THIS IS PRESENT:
 * Provides a unified contract holding an HTTP status and machine-readable error code.
 * Subclasses (e.g. ResourceNotFoundException, BadRequestException) extend this class
 * so that the GlobalExceptionHandler can process them uniformly without boilerplate.
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    /**
     * Constructs an ApiException with a custom message, HTTP status, and error code.
     * 
     * @param message Human-readable error description
     * @param status HTTP response status code
     * @param errorCode Machine-readable error code string (e.g. 'USER_NOT_FOUND')
     */
    public ApiException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    /**
     * Constructs an ApiException defaulting the error code to the HTTP status name.
     */
    public ApiException(String message, HttpStatus status) {
        this(message, status, status.name());
    }
}
