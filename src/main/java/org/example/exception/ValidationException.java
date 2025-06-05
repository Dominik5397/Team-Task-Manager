package org.example.exception;

import java.util.Map;
import java.util.HashMap;

/**
 * Wyjątek rzucany gdy dane nie przechodzą walidacji biznesowej.
 */
public class ValidationException extends RuntimeException {
    
    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = new HashMap<>();
    }

    public ValidationException(String field, String message) {
        super(String.format("Validation failed for field '%s': %s", field, message));
        this.fieldErrors = new HashMap<>();
        this.fieldErrors.put(field, message);
    }

    public ValidationException(Map<String, String> fieldErrors) {
        super("Validation failed for multiple fields");
        this.fieldErrors = new HashMap<>(fieldErrors);
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = new HashMap<>(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        return new HashMap<>(fieldErrors);
    }

    public void addFieldError(String field, String message) {
        this.fieldErrors.put(field, message);
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
} 