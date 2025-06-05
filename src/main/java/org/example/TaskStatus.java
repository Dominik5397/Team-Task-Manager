package org.example;

/**
 * Enum reprezentujący możliwe statusy zadań.
 * Zapewnia bezpieczeństwo typów i spójność danych.
 */
public enum TaskStatus {
    TODO("To Do"),
    IN_PROGRESS("In Progress"), 
    DONE("Done");
    
    private final String displayName;
    
    TaskStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Konwertuje string na enum, obsługując różne formaty wejściowe
     */
    public static TaskStatus fromString(String value) {
        if (value == null) return null;
        
        // Obsługa różnych formatów wejściowych
        switch (value.toUpperCase().replace(" ", "_")) {
            case "TODO":
            case "TO_DO":
                return TODO;
            case "IN_PROGRESS":
            case "INPROGRESS": 
                return IN_PROGRESS;
            case "DONE":
                return DONE;
            default:
                // Próba dopasowania przez displayName
                for (TaskStatus status : TaskStatus.values()) {
                    if (status.displayName.equalsIgnoreCase(value)) {
                        return status;
                    }
                }
                throw new IllegalArgumentException("Unknown task status: " + value);
        }
    }
    
    @Override
    public String toString() {
        return displayName;
    }
} 