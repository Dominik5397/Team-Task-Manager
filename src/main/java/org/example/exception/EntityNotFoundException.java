/**
 * Exception thrown when an entity cannot be found in the database.
 */
package org.example.exception;

public class EntityNotFoundException extends RuntimeException {
    
    private final String entityType;
    private final Object entityId;

    public EntityNotFoundException(String entityType, Object entityId) {
        super(String.format("%s not found with id: %s", entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public EntityNotFoundException(String entityType, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", entityType, fieldName, fieldValue));
        this.entityType = entityType;
        this.entityId = fieldValue;
    }

    public EntityNotFoundException(String message) {
        super(message);
        this.entityType = "Unknown";
        this.entityId = null;
    }

    public String getEntityType() {
        return entityType;
    }

    public Object getEntityId() {
        return entityId;
    }
} 