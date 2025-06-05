package org.example;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Klasa przechowująca statystyki historii zmian.
 */
public class ChangeLogStats {
    
    private Long entityId; // ID zadania lub użytkownika
    private String entityType; // "task" lub "user"
    private int totalChanges;
    private LocalDateTime firstChange;
    private LocalDateTime lastChange;
    private Map<ChangeLogOperationType, Integer> changesByType;
    private Map<String, Integer> changesByField;
    private int changesLast24Hours;
    private int changesLast7Days;
    private int changesLast30Days;
    private double averageChangesPerDay;
    
    public ChangeLogStats() {
        this.changesByType = new HashMap<>();
        this.changesByField = new HashMap<>();
    }
    
    public ChangeLogStats(Long entityId, String entityType) {
        this();
        this.entityId = entityId;
        this.entityType = entityType;
    }

    // Gettery i settery
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public int getTotalChanges() { return totalChanges; }
    public void setTotalChanges(int totalChanges) { this.totalChanges = totalChanges; }

    public LocalDateTime getFirstChange() { return firstChange; }
    public void setFirstChange(LocalDateTime firstChange) { this.firstChange = firstChange; }

    public LocalDateTime getLastChange() { return lastChange; }
    public void setLastChange(LocalDateTime lastChange) { this.lastChange = lastChange; }

    public Map<ChangeLogOperationType, Integer> getChangesByType() { return changesByType; }
    public void setChangesByType(Map<ChangeLogOperationType, Integer> changesByType) { this.changesByType = changesByType; }

    public Map<String, Integer> getChangesByField() { return changesByField; }
    public void setChangesByField(Map<String, Integer> changesByField) { this.changesByField = changesByField; }

    public int getChangesLast24Hours() { return changesLast24Hours; }
    public void setChangesLast24Hours(int changesLast24Hours) { this.changesLast24Hours = changesLast24Hours; }

    public int getChangesLast7Days() { return changesLast7Days; }
    public void setChangesLast7Days(int changesLast7Days) { this.changesLast7Days = changesLast7Days; }

    public int getChangesLast30Days() { return changesLast30Days; }
    public void setChangesLast30Days(int changesLast30Days) { this.changesLast30Days = changesLast30Days; }

    public double getAverageChangesPerDay() { return averageChangesPerDay; }
    public void setAverageChangesPerDay(double averageChangesPerDay) { this.averageChangesPerDay = averageChangesPerDay; }

    // Metody pomocnicze
    public void addChangeByType(ChangeLogOperationType operationType) {
        changesByType.merge(operationType, 1, Integer::sum);
    }

    public void addChangeByField(String fieldName) {
        if (fieldName != null && !fieldName.isEmpty()) {
            changesByField.merge(fieldName, 1, Integer::sum);
        }
    }

    public ChangeLogOperationType getMostFrequentOperationType() {
        return changesByType.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public String getMostChangedField() {
        return changesByField.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public boolean hasRecentActivity() {
        return changesLast24Hours > 0;
    }

    public boolean isActiveEntity() {
        return changesLast7Days > 0;
    }

    @Override
    public String toString() {
        return String.format("ChangeLogStats{entityId=%d, entityType='%s', totalChanges=%d, last24h=%d}", 
                           entityId, entityType, totalChanges, changesLast24Hours);
    }
} 