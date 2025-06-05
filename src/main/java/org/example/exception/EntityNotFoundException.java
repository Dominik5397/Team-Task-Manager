package org.example.exception;

/**
 * Wyjątek rzucany gdy nie można znaleźć encji w bazie danych.
 */
public class EntityNotFoundException extends RuntimeException {
    
    private final String entityName;
    private final Object entityId;

    public EntityNotFoundException(String entityName, Object entityId) {
        super(String.format("%s not found with id: %s", entityName, entityId));
        this.entityName = entityName;
        this.entityId = entityId;
    }

    public EntityNotFoundException(String entityName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", entityName, fieldName, fieldValue));
        this.entityName = entityName;
        this.entityId = fieldValue;
    }

    public EntityNotFoundException(String message) {
        super(message);
        this.entityName = "Entity";
        this.entityId = "unknown";
    }

    public String getEntityName() {
        return entityName;
    }

    public Object getEntityId() {
        return entityId;
    }
} 