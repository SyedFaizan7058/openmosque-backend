package com.openmosque.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested entity cannot be found in the database.
 * 
 * WHY THIS IS PRESENT:
 * Used across repositories and services (e.g. Mosque not found, User not found, Event not found)
 * to automatically trigger a 404 HTTP response with clear entity and field details.
 * 
 * EXAMPLE USAGE:
 * throw new ResourceNotFoundException("Mosque", "id", mosqueId);
 */
public class ResourceNotFoundException extends ApiException {

    /**
     * Constructs a ResourceNotFoundException with formatted entity name and search criteria.
     * 
     * @param resourceName The entity name (e.g., "Mosque", "User")
     * @param fieldName The lookup field (e.g., "id", "slug", "email")
     * @param fieldValue The value that failed the lookup
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue), 
              HttpStatus.NOT_FOUND, 
              "RESOURCE_NOT_FOUND");
    }

    /**
     * Constructs a ResourceNotFoundException with a custom message.
     */
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}
