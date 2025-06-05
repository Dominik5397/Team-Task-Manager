package org.example;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface serwisu do zarządzania szczegółową historią zmian zadań.
 */
public interface ChangeLogService {
    
    /**
     * Zapisuje nowy wpis w historii zmian
     */
    ChangeLogEntry logChange(Task task, String fieldName, String oldValue, String newValue, 
                           ChangeLogOperationType operationType);
    
    /**
     * Zapisuje nowy wpis w historii zmian z dodatkowym opisem
     */
    ChangeLogEntry logChange(Task task, String fieldName, String oldValue, String newValue, 
                           ChangeLogOperationType operationType, String description);
    
    /**
     * Zapisuje nowy wpis w historii zmian z informacją o użytkowniku
     */
    ChangeLogEntry logChange(Task task, String fieldName, String oldValue, String newValue, 
                           ChangeLogOperationType operationType, User changedBy);
    
    /**
     * Porównuje zadania i automatycznie tworzy wpisy historii dla zmienionych pól
     */
    List<ChangeLogEntry> logTaskChanges(Task oldTask, Task newTask, User changedBy);
    
    /**
     * Pobiera pełną historię zmian dla zadania
     */
    List<ChangeLogEntry> getTaskHistory(Long taskId);
    
    /**
     * Pobiera historię zmian dla zadania w określonym zakresie dat
     */
    List<ChangeLogEntry> getTaskHistory(Long taskId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Pobiera ostatnie N zmian dla zadania
     */
    List<ChangeLogEntry> getRecentTaskChanges(Long taskId, int limit);
    
    /**
     * Pobiera historię zmian według typu operacji
     */
    List<ChangeLogEntry> getChangesByOperationType(ChangeLogOperationType operationType);
    
    /**
     * Pobiera zmiany wykonane przez konkretnego użytkownika
     */
    List<ChangeLogEntry> getChangesByUser(Long userId);
    
    /**
     * Pobiera ostatnie zmiany w systemie
     */
    List<ChangeLogEntry> getRecentSystemChanges(int limit);
    
    /**
     * Pobiera statystyki zmian dla zadania
     */
    ChangeLogStats getTaskChangeStats(Long taskId);
    
    /**
     * Pobiera statystyki zmian dla użytkownika
     */
    ChangeLogStats getUserChangeStats(Long userId);
    
    /**
     * Czyści starą historię zmian (starszą niż określona liczba dni)
     */
    int cleanOldChangeLog(int daysOld);
    
    /**
     * Eksportuje historię zmian dla zadania do JSON
     */
    String exportTaskHistoryToJson(Long taskId);
} 