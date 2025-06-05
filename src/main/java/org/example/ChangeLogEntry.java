package org.example;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/**
 * Encja reprezentująca wpis w historii zmian zadania.
 * Przechowuje szczegółowe informacje o każdej zmianie: jakie pole, poprzednia wartość, nowa wartość.
 */
@Entity
@Table(name = "change_log_entry")
public class ChangeLogEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Zadanie, którego dotyczy zmiana
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({"changeLogEntries", "assignedTo"})
    private Task task;
    
    /**
     * Nazwa pola, które zostało zmienione
     */
    @Column(name = "field_name", nullable = false, length = 50)
    private String fieldName;
    
    /**
     * Poprzednia wartość pola (przed zmianą)
     */
    @Column(name = "old_value", length = 1000)
    private String oldValue;
    
    /**
     * Nowa wartość pola (po zmianie)
     */
    @Column(name = "new_value", length = 1000)
    private String newValue;
    
    /**
     * Typ operacji (CREATE, UPDATE, DELETE, ASSIGN, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private ChangeLogOperationType operationType;
    
    /**
     * Użytkownik, który wykonał zmianę (opcjonalne)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    @JsonIgnoreProperties("tasks")
    private User changedBy;
    
    /**
     * Data i czas zmiany
     */
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
    
    /**
     * Dodatkowy opis zmiany (opcjonalny)
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * Adres IP użytkownika (dla audytu)
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    /**
     * User Agent przeglądarki (dla audytu)
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // Konstruktory
    public ChangeLogEntry() {
        this.changedAt = LocalDateTime.now();
    }

    public ChangeLogEntry(Task task, String fieldName, String oldValue, String newValue, 
                         ChangeLogOperationType operationType) {
        this();
        this.task = task;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.operationType = operationType;
    }

    public ChangeLogEntry(Task task, String fieldName, String oldValue, String newValue, 
                         ChangeLogOperationType operationType, String description) {
        this(task, fieldName, oldValue, newValue, operationType);
        this.description = description;
    }

    // Gettery i settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public ChangeLogOperationType getOperationType() { return operationType; }
    public void setOperationType(ChangeLogOperationType operationType) { this.operationType = operationType; }

    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    // Metody pomocnicze
    public boolean isFieldChange() {
        return operationType == ChangeLogOperationType.UPDATE && 
               fieldName != null && !fieldName.isEmpty();
    }

    public boolean hasValueChange() {
        return !java.util.Objects.equals(oldValue, newValue);
    }

    public String getChangeDescription() {
        if (description != null && !description.isEmpty()) {
            return description;
        }
        
        return switch (operationType) {
            case CREATE -> "Task created";
            case UPDATE -> hasValueChange() ? 
                String.format("Field '%s' changed from '%s' to '%s'", fieldName, oldValue, newValue) :
                String.format("Field '%s' updated", fieldName);
            case DELETE -> "Task deleted";
            case ASSIGN -> String.format("Task assigned to: %s", newValue);
            case UNASSIGN -> String.format("Task unassigned from: %s", oldValue);
            case STATUS_CHANGE -> String.format("Status changed from '%s' to '%s'", oldValue, newValue);
            case PRIORITY_CHANGE -> String.format("Priority changed from '%s' to '%s'", oldValue, newValue);
            case DUE_DATE_CHANGE -> String.format("Due date changed from '%s' to '%s'", oldValue, newValue);
            case TITLE_CHANGE -> String.format("Title changed from '%s' to '%s'", oldValue, newValue);
            case DESCRIPTION_CHANGE -> String.format("Description changed from '%s' to '%s'", oldValue, newValue);
            default -> String.format("Field '%s' changed from '%s' to '%s'", fieldName, oldValue, newValue);
        };
    }

    @Override
    public String toString() {
        return String.format("ChangeLogEntry{id=%d, task=%d, field='%s', operation=%s, changedAt=%s}", 
                           id, task != null ? task.getId() : null, fieldName, operationType, changedAt);
    }
} 