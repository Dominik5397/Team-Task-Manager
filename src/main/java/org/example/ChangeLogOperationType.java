package org.example;

/**
 * Enum definiujący typy operacji w historii zmian zadań.
 */
public enum ChangeLogOperationType {
    
    /**
     * Utworzenie nowego zadania
     */
    CREATE("Create", "Task was created"),
    
    /**
     * Aktualizacja zadania
     */
    UPDATE("Update", "Task field was updated"),
    
    /**
     * Usunięcie zadania
     */
    DELETE("Delete", "Task was deleted"),
    
    /**
     * Przypisanie zadania do użytkownika
     */
    ASSIGN("Assign", "Task was assigned to user"),
    
    /**
     * Usunięcie przypisania zadania
     */
    UNASSIGN("Unassign", "Task assignment was removed"),
    
    /**
     * Zmiana statusu zadania
     */
    STATUS_CHANGE("Status Change", "Task status was changed"),
    
    /**
     * Zmiana priorytetu zadania
     */
    PRIORITY_CHANGE("Priority Change", "Task priority was changed"),
    
    /**
     * Zmiana terminu wykonania
     */
    DUE_DATE_CHANGE("Due Date Change", "Task due date was changed"),
    
    /**
     * Zmiana tytułu zadania
     */
    TITLE_CHANGE("Title Change", "Task title was changed"),
    
    /**
     * Zmiana opisu zadania
     */
    DESCRIPTION_CHANGE("Description Change", "Task description was changed");

    private final String displayName;
    private final String defaultDescription;

    ChangeLogOperationType(String displayName, String defaultDescription) {
        this.displayName = displayName;
        this.defaultDescription = defaultDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    /**
     * Sprawdza czy operacja dotyczy zmiany wartości pola
     */
    public boolean isFieldUpdate() {
        return this == UPDATE || 
               this == STATUS_CHANGE || 
               this == PRIORITY_CHANGE || 
               this == DUE_DATE_CHANGE || 
               this == TITLE_CHANGE || 
               this == DESCRIPTION_CHANGE;
    }

    /**
     * Sprawdza czy operacja dotyczy przypisania użytkownika
     */
    public boolean isAssignmentOperation() {
        return this == ASSIGN || this == UNASSIGN;
    }

    /**
     * Sprawdza czy operacja dotyczy cyklu życia zadania
     */
    public boolean isLifecycleOperation() {
        return this == CREATE || this == DELETE;
    }

    public static ChangeLogOperationType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return UPDATE;
        }
        
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UPDATE;
        }
    }
} 