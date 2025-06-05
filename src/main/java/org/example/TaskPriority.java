package org.example;

/**
 * Enum reprezentujący możliwe priorytety zadań.
 * Zapewnia bezpieczeństwo typów i spójność danych.
 */
public enum TaskPriority {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3);
    
    private final String displayName;
    private final int level;
    
    TaskPriority(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLevel() {
        return level;
    }
    
    /**
     * Konwertuje string na enum, obsługując różne formaty wejściowe
     */
    public static TaskPriority fromString(String value) {
        if (value == null) return null;
        
        switch (value.toUpperCase()) {
            case "LOW":
                return LOW;
            case "MEDIUM":
                return MEDIUM;
            case "HIGH":
                return HIGH;
            default:
                // Próba dopasowania przez displayName
                for (TaskPriority priority : TaskPriority.values()) {
                    if (priority.displayName.equalsIgnoreCase(value)) {
                        return priority;
                    }
                }
                throw new IllegalArgumentException("Unknown task priority: " + value);
        }
    }
    
    /**
     * Porównuje priorytety - wyższy priorytet ma większą wartość
     */
    public boolean isHigherThan(TaskPriority other) {
        return this.level > other.level;
    }
    
    /**
     * Porównuje priorytety - niższy priorytet ma mniejszą wartość
     */
    public boolean isLowerThan(TaskPriority other) {
        return this.level < other.level;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
} 