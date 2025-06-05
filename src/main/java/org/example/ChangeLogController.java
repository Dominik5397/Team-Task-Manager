package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Kontroler REST do zarządzania historią zmian zadań.
 * Udostępnia endpoints do przeglądania, filtrowania i analizowania zmian.
 */
@RestController
@RequestMapping("/api/changelog")
public class ChangeLogController {
    
    @Autowired
    private ChangeLogService changeLogService;

    /**
     * Pobiera pełną historię zmian dla konkretnego zadania
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<ChangeLogEntry>> getTaskHistory(@PathVariable Long taskId) {
        List<ChangeLogEntry> history = changeLogService.getTaskHistory(taskId);
        return ResponseEntity.ok(history);
    }

    /**
     * Pobiera historię zmian dla zadania w określonym zakresie dat
     */
    @GetMapping("/task/{taskId}/daterange")
    public ResponseEntity<List<ChangeLogEntry>> getTaskHistoryByDateRange(
            @PathVariable Long taskId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<ChangeLogEntry> history = changeLogService.getTaskHistory(taskId, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    /**
     * Pobiera ostatnie N zmian dla zadania
     */
    @GetMapping("/task/{taskId}/recent")
    public ResponseEntity<List<ChangeLogEntry>> getRecentTaskChanges(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<ChangeLogEntry> changes = changeLogService.getRecentTaskChanges(taskId, limit);
        return ResponseEntity.ok(changes);
    }

    /**
     * Pobiera zmiany według typu operacji
     */
    @GetMapping("/operation/{operationType}")
    public ResponseEntity<List<ChangeLogEntry>> getChangesByOperationType(
            @PathVariable ChangeLogOperationType operationType) {
        
        List<ChangeLogEntry> changes = changeLogService.getChangesByOperationType(operationType);
        return ResponseEntity.ok(changes);
    }

    /**
     * Pobiera zmiany wykonane przez konkretnego użytkownika
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChangeLogEntry>> getChangesByUser(@PathVariable Long userId) {
        List<ChangeLogEntry> changes = changeLogService.getChangesByUser(userId);
        return ResponseEntity.ok(changes);
    }

    /**
     * Pobiera ostatnie zmiany w całym systemie
     */
    @GetMapping("/recent")
    public ResponseEntity<List<ChangeLogEntry>> getRecentSystemChanges(
            @RequestParam(defaultValue = "20") int limit) {
        
        List<ChangeLogEntry> changes = changeLogService.getRecentSystemChanges(limit);
        return ResponseEntity.ok(changes);
    }

    /**
     * Pobiera statystyki zmian dla zadania
     */
    @GetMapping("/stats/task/{taskId}")
    public ResponseEntity<ChangeLogStats> getTaskChangeStats(@PathVariable Long taskId) {
        ChangeLogStats stats = changeLogService.getTaskChangeStats(taskId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Pobiera statystyki zmian dla użytkownika
     */
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<ChangeLogStats> getUserChangeStats(@PathVariable Long userId) {
        ChangeLogStats stats = changeLogService.getUserChangeStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Eksportuje historię zmian zadania do JSON
     */
    @GetMapping("/task/{taskId}/export")
    public ResponseEntity<Map<String, Object>> exportTaskHistory(@PathVariable Long taskId) {
        try {
            String jsonHistory = changeLogService.exportTaskHistoryToJson(taskId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("taskId", taskId);
            response.put("exportedAt", LocalDateTime.now());
            response.put("format", "json");
            response.put("data", jsonHistory);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Export failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("taskId", taskId);
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Dashboard z podstawowymi statystykami systemu change log
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getChangeLogDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Ostatnie zmiany w systemie
        List<ChangeLogEntry> recentChanges = changeLogService.getRecentSystemChanges(10);
        dashboard.put("recentChanges", recentChanges);
        dashboard.put("recentChangesCount", recentChanges.size());
        
        // Statystyki według typu operacji
        Map<String, Integer> operationStats = new HashMap<>();
        for (ChangeLogOperationType type : ChangeLogOperationType.values()) {
            List<ChangeLogEntry> typeChanges = changeLogService.getChangesByOperationType(type);
            operationStats.put(type.getDisplayName(), typeChanges.size());
        }
        dashboard.put("operationStats", operationStats);
        
        // Informacje o systemie
        dashboard.put("timestamp", LocalDateTime.now());
        dashboard.put("version", "2.0");
        dashboard.put("description", "Advanced Change Log Management System");
        
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Czyści starą historię zmian
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanOldChangeLog(
            @RequestParam(defaultValue = "90") int daysOld) {
        
        int deletedCount = changeLogService.cleanOldChangeLog(daysOld);
        
        Map<String, Object> response = new HashMap<>();
        response.put("operation", "cleanup");
        response.put("deletedEntries", deletedCount);
        response.put("olderThanDays", daysOld);
        response.put("timestamp", LocalDateTime.now());
        response.put("message", String.format("Deleted %d old change log entries", deletedCount));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Pobiera dostępne typy operacji
     */
    @GetMapping("/operation-types")
    public ResponseEntity<Map<String, Object>> getOperationTypes() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, String> operationTypes = new HashMap<>();
        for (ChangeLogOperationType type : ChangeLogOperationType.values()) {
            operationTypes.put(type.name(), type.getDisplayName());
        }
        
        response.put("operationTypes", operationTypes);
        response.put("count", operationTypes.size());
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Wyszukuje wpisy w historii zmian
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchChangeLog(
            @RequestParam(required = false) String fieldName,
            @RequestParam(required = false) ChangeLogOperationType operationType,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long taskId,
            @RequestParam(defaultValue = "50") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        List<ChangeLogEntry> results = null;
        
        if (taskId != null && operationType != null) {
            // Wyszukiwanie według zadania i typu operacji
            results = changeLogService.getTaskHistory(taskId).stream()
                    .filter(entry -> entry.getOperationType() == operationType)
                    .limit(limit)
                    .toList();
        } else if (taskId != null) {
            // Wyszukiwanie według zadania
            results = changeLogService.getRecentTaskChanges(taskId, limit);
        } else if (userId != null) {
            // Wyszukiwanie według użytkownika
            results = changeLogService.getChangesByUser(userId).stream()
                    .limit(limit)
                    .toList();
        } else if (operationType != null) {
            // Wyszukiwanie według typu operacji
            results = changeLogService.getChangesByOperationType(operationType).stream()
                    .limit(limit)
                    .toList();
        } else {
            // Domyślnie ostatnie zmiany
            results = changeLogService.getRecentSystemChanges(limit);
        }
        
        response.put("results", results);
        response.put("count", results != null ? results.size() : 0);
        response.put("filters", Map.of(
            "fieldName", fieldName,
            "operationType", operationType,
            "userId", userId,
            "taskId", taskId,
            "limit", limit
        ));
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
} 