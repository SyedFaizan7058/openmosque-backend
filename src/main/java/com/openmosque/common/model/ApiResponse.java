package com.openmosque.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Standard Universal JSON API Response Wrapper.
 * 
 * WHY THIS IS PRESENT:
 * Guarantees that every REST endpoint across the entire OpenMosque backend
 * returns a predictable JSON structure for React Web and React Native mobile clients:
 * {
 *   "success": true,
 *   "message": "...",
 *   "data": { ... },
 *   "error": null,
 *   "timestamp": "2026-08-27T10:00:00Z"
 * }
 * 
 * @param <T> The payload data type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 'true' if the HTTP request completed successfully, 'false' if an error occurred.
     */
    private boolean success;

    /**
     * Human-readable status or informational message.
     */
    private String message;

    /**
     * The response payload object (Entity, DTO, List, Page, etc.). Null on errors.
     */
    private T data;

    /**
     * Structured error details if 'success' is false. Null on successful responses.
     */
    private ErrorDetail error;

    /**
     * UTC Timestamp of the response generation.
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Static factory for a successful response with payload.
     * 
     * @param data The payload object
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Static factory for a successful response with payload and custom message.
     * 
     * @param data The payload object
     * @param message Informational message
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Static factory for an error response.
     * 
     * @param code Machine-readable error code (e.g. 'VALIDATION_FAILED')
     * @param message Human-readable error description
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(new ErrorDetail(code, message, null))
                .build();
    }

    /**
     * Static factory for an error response with detailed field-level error mapping.
     * 
     * @param code Machine-readable error code
     * @param message Error description
     * @param fieldErrors Map of input field names to validation error messages
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String code, String message, Map<String, String> fieldErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(new ErrorDetail(code, message, fieldErrors))
                .build();
    }

    /**
     * Nested structure for error payloads.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {
        private String code;
        private String message;
        private Map<String, String> details;
    }
}
