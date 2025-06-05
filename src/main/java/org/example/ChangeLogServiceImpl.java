package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Implementacja serwisu do zarządzania szczegółową historią zmian zadań.
 * Automatycznie wykrywa zmiany w polach i tworzy odpowiednie wpisy w historii.
 */
@Service
public class ChangeLogServiceImpl implements ChangeLogService {
    
    @Autowired
    private ChangeLogEntryRepository changeLogRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ChangeLogEntry logChange(Task task, String fieldName, String oldValue, String newValue, 
                                  ChangeLogOperationType operationType) {
        return logChange(task, fieldName, oldValue, newValue, operationType, (String) null);
    }

    @Override
    public ChangeLogEntry logChange(Task task, String fieldName, String oldValue, String newValue, 
                                  ChangeLogOperationType operationType, String description) {
        ChangeLogEntry entry = new ChangeLogEntry(task, fieldName, oldValue, newValue, operationType, description);
        return changeLogRepository.save(entry);
    }

    @Override
    public ChangeLogEntry logChange(Task task, String fieldName, String oldValue, String newValue, 
                                  ChangeLogOperationType operationType, User changedBy) {
        ChangeLogEntry entry = new ChangeLogEntry(task, fieldName, oldValue, newValue, operationType);
        entry.setChangedBy(changedBy);
        return changeLogRepository.save(entry);
    }

    @Override
    public List<ChangeLogEntry> logTaskChanges(Task oldTask, Task newTask, User changedBy) {
        List<ChangeLogEntry> changes = new ArrayList<>();
        
        if (oldTask == null) {
            // Nowe zadanie
            ChangeLogEntry createEntry = new ChangeLogEntry(newTask, "task", null, "created", 
                                                           ChangeLogOperationType.CREATE);
            createEntry.setChangedBy(changedBy);
            createEntry.setDescription("Task created: " + newTask.getTitle());
            changes.add(changeLogRepository.save(createEntry));
            return changes;
        }

        // Porównanie pól i tworzenie wpisów dla zmienionych wartości
        changes.addAll(compareAndLogFieldChanges(oldTask, newTask, changedBy));
        
        return changes;
    }

    private List<ChangeLogEntry> compareAndLogFieldChanges(Task oldTask, Task newTask, User changedBy) {
        List<ChangeLogEntry> changes = new ArrayList<>();

        // Sprawdzenie zmiany tytułu
        if (!Objects.equals(oldTask.getTitle(), newTask.getTitle())) {
            changes.add(createFieldChangeEntry(newTask, "title", oldTask.getTitle(), 
                                             newTask.getTitle(), ChangeLogOperationType.TITLE_CHANGE, changedBy));
        }

        // Sprawdzenie zmiany opisu
        if (!Objects.equals(oldTask.getDescription(), newTask.getDescription())) {
            changes.add(createFieldChangeEntry(newTask, "description", oldTask.getDescription(), 
                                             newTask.getDescription(), ChangeLogOperationType.DESCRIPTION_CHANGE, changedBy));
        }

        // Sprawdzenie zmiany statusu
        if (!Objects.equals(oldTask.getStatus(), newTask.getStatus())) {
            String oldStatus = oldTask.getStatus() != null ? oldTask.getStatus().getDisplayName() : null;
            String newStatus = newTask.getStatus() != null ? newTask.getStatus().getDisplayName() : null;
            changes.add(createFieldChangeEntry(newTask, "status", oldStatus, newStatus, 
                                             ChangeLogOperationType.STATUS_CHANGE, changedBy));
        }

        // Sprawdzenie zmiany priorytetu
        if (!Objects.equals(oldTask.getPriority(), newTask.getPriority())) {
            String oldPriority = oldTask.getPriority() != null ? oldTask.getPriority().getDisplayName() : null;
            String newPriority = newTask.getPriority() != null ? newTask.getPriority().getDisplayName() : null;
            changes.add(createFieldChangeEntry(newTask, "priority", oldPriority, newPriority, 
                                             ChangeLogOperationType.PRIORITY_CHANGE, changedBy));
        }

        // Sprawdzenie zmiany terminu wykonania
        if (!Objects.equals(oldTask.getDueDate(), newTask.getDueDate())) {
            String oldDate = oldTask.getDueDate() != null ? oldTask.getDueDate().toString() : null;
            String newDate = newTask.getDueDate() != null ? newTask.getDueDate().toString() : null;
            changes.add(createFieldChangeEntry(newTask, "dueDate", oldDate, newDate, 
                                             ChangeLogOperationType.DUE_DATE_CHANGE, changedBy));
        }

        // Sprawdzenie zmiany przypisania
        if (!Objects.equals(getAssignedToId(oldTask), getAssignedToId(newTask))) {
            String oldAssignee = getAssignedToName(oldTask);
            String newAssignee = getAssignedToName(newTask);
            
            ChangeLogOperationType operationType = newTask.getAssignedTo() != null ? 
                ChangeLogOperationType.ASSIGN : ChangeLogOperationType.UNASSIGN;
            
            changes.add(createFieldChangeEntry(newTask, "assignedTo", oldAssignee, newAssignee, 
                                             operationType, changedBy));
        }

        return changes;
    }

    private ChangeLogEntry createFieldChangeEntry(Task task, String fieldName, String oldValue, String newValue, 
                                                ChangeLogOperationType operationType, User changedBy) {
        ChangeLogEntry entry = new ChangeLogEntry(task, fieldName, oldValue, newValue, operationType);
        entry.setChangedBy(changedBy);
        return changeLogRepository.save(entry);
    }

    private Long getAssignedToId(Task task) {
        return task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;
    }

    private String getAssignedToName(Task task) {
        return task.getAssignedTo() != null ? task.getAssignedTo().getUsername() : "unassigned";
    }

    @Override
    public List<ChangeLogEntry> getTaskHistory(Long taskId) {
        return changeLogRepository.findByTaskIdOrderByChangedAtDesc(taskId);
    }

    @Override
    public List<ChangeLogEntry> getTaskHistory(Long taskId, LocalDateTime startDate, LocalDateTime endDate) {
        return changeLogRepository.findByTaskIdAndChangedAtBetweenOrderByChangedAtDesc(taskId, startDate, endDate);
    }

    @Override
    public List<ChangeLogEntry> getRecentTaskChanges(Long taskId, int limit) {
        return changeLogRepository.findTopNByTaskIdOrderByChangedAtDesc(taskId, limit);
    }

    @Override
    public List<ChangeLogEntry> getChangesByOperationType(ChangeLogOperationType operationType) {
        return changeLogRepository.findByOperationTypeOrderByChangedAtDesc(operationType);
    }

    @Override
    public List<ChangeLogEntry> getChangesByUser(Long userId) {
        return changeLogRepository.findByChangedByIdOrderByChangedAtDesc(userId);
    }

    @Override
    public List<ChangeLogEntry> getRecentSystemChanges(int limit) {
        return changeLogRepository.findRecentChanges(limit);
    }

    @Override
    public ChangeLogStats getTaskChangeStats(Long taskId) {
        List<ChangeLogEntry> entries = getTaskHistory(taskId);
        return calculateStats(taskId, "task", entries);
    }

    @Override
    public ChangeLogStats getUserChangeStats(Long userId) {
        List<ChangeLogEntry> entries = getChangesByUser(userId);
        return calculateStats(userId, "user", entries);
    }

    private ChangeLogStats calculateStats(Long entityId, String entityType, List<ChangeLogEntry> entries) {
        ChangeLogStats stats = new ChangeLogStats(entityId, entityType);
        
        if (entries.isEmpty()) {
            return stats;
        }

        stats.setTotalChanges(entries.size());
        stats.setFirstChange(entries.get(entries.size() - 1).getChangedAt());
        stats.setLastChange(entries.get(0).getChangedAt());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusHours(24);
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDateTime monthAgo = now.minusDays(30);

        for (ChangeLogEntry entry : entries) {
            stats.addChangeByType(entry.getOperationType());
            stats.addChangeByField(entry.getFieldName());

            LocalDateTime changedAt = entry.getChangedAt();
            if (changedAt.isAfter(yesterday)) {
                stats.setChangesLast24Hours(stats.getChangesLast24Hours() + 1);
            }
            if (changedAt.isAfter(weekAgo)) {
                stats.setChangesLast7Days(stats.getChangesLast7Days() + 1);
            }
            if (changedAt.isAfter(monthAgo)) {
                stats.setChangesLast30Days(stats.getChangesLast30Days() + 1);
            }
        }

        // Obliczenie średniej zmian dziennie
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(stats.getFirstChange(), stats.getLastChange());
        if (daysBetween > 0) {
            stats.setAverageChangesPerDay((double) stats.getTotalChanges() / daysBetween);
        }

        return stats;
    }

    @Override
    public int cleanOldChangeLog(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<ChangeLogEntry> oldEntries = changeLogRepository.findByChangedAtBetweenOrderByChangedAtDesc(
            LocalDateTime.MIN, cutoffDate);
        
        changeLogRepository.deleteAll(oldEntries);
        return oldEntries.size();
    }

    @Override
    public String exportTaskHistoryToJson(Long taskId) {
        try {
            List<ChangeLogEntry> history = getTaskHistory(taskId);
            return objectMapper.writeValueAsString(history);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export task history to JSON", e);
        }
    }
} 