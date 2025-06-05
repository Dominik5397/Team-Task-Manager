package org.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * Standardowa odpowiedź błędu zwracana przez API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    
    private int status;
    private String error;
    private String message;
    private String path;
    private String requestId;
    private String errorCode;
    
    // Dodatkowe informacje dla różnych typów błędów
    private Map<String, String> fieldErrors;
    private List<String> details;
    private Object debugInfo;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message);
        this.path = path;
    }

    // Gettery i settery
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public Map<String, String> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(Map<String, String> fieldErrors) { this.fieldErrors = fieldErrors; }

    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }

    public Object getDebugInfo() { return debugInfo; }
    public void setDebugInfo(Object debugInfo) { this.debugInfo = debugInfo; }

    // Builder pattern dla wygody
    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    public static class ErrorResponseBuilder {
        private ErrorResponse errorResponse = new ErrorResponse();

        public ErrorResponseBuilder status(int status) {
            errorResponse.setStatus(status);
            return this;
        }

        public ErrorResponseBuilder error(String error) {
            errorResponse.setError(error);
            return this;
        }

        public ErrorResponseBuilder message(String message) {
            errorResponse.setMessage(message);
            return this;
        }

        public ErrorResponseBuilder path(String path) {
            errorResponse.setPath(path);
            return this;
        }

        public ErrorResponseBuilder requestId(String requestId) {
            errorResponse.setRequestId(requestId);
            return this;
        }

        public ErrorResponseBuilder errorCode(String errorCode) {
            errorResponse.setErrorCode(errorCode);
            return this;
        }

        public ErrorResponseBuilder fieldErrors(Map<String, String> fieldErrors) {
            errorResponse.setFieldErrors(fieldErrors);
            return this;
        }

        public ErrorResponseBuilder details(List<String> details) {
            errorResponse.setDetails(details);
            return this;
        }

        public ErrorResponseBuilder debugInfo(Object debugInfo) {
            errorResponse.setDebugInfo(debugInfo);
            return this;
        }

        public ErrorResponse build() {
            return errorResponse;
        }
    }
} 