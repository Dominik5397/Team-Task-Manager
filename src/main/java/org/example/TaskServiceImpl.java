package org.example;

import org.example.exception.EntityNotFoundException;
import org.example.exception.BusinessLogicException;
import org.example.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementacja serwisu obsługującego logikę biznesową dla zadań.
 * Zawiera operacje CRUD oraz zaawansowane funkcjonalności biznesowe.
 * Zaktualizowany o nowy system szczegółowego śledzenia zmian i obsługę wyjątków.
 */
@Service
public class TaskServiceImpl implements TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Nowy serwis do zarządzania szczegółową historią zmian
     */
    @Autowired
    private ChangeLogService changeLogService;
    
    /**
     * Wstrzyknięty, skonfigurowany ObjectMapper do obsługi JSON.
     * Zarządzany centralnie przez JsonConfig.
     */
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    @Override
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }
    
    @Override
    public Task createTask(Task task) {
        // Walidacja biznesowa przed utworzeniem
        validateTaskForCreation(task);
        
        // Zapis zadania
        Task savedTask = taskRepository.save(task);
        
        // Dodanie loga tworzenia (legacy)
        addChangeLogEntry(savedTask, "created", "Task created: " + savedTask.getTitle());
        
        // Dodanie szczegółowego loga tworzenia (nowy system)
        changeLogService.logChange(savedTask, "task", null, "created", 
                                 ChangeLogOperationType.CREATE, "Task created: " + savedTask.getTitle());
        
        return taskRepository.save(savedTask);
    }
    
    @Override
    public Task updateTask(Long id, Task task) {
        Optional<Task> existingTaskOpt = taskRepository.findById(id);
        if (existingTaskOpt.isEmpty()) {
            throw new EntityNotFoundException("Task", id);
        }
        
        Task existingTask = existingTaskOpt.get();
        
        // Walidacja biznesowa przed aktualizacją
        validateTaskForUpdate(task, existingTask);
        
        task.setId(id);
        
        // Zachowanie istniejącego loga (legacy)
        if (existingTask.getChangeLog() != null) {
            task.setChangeLog(existingTask.getChangeLog());
        }
        
        // Sprawdzenie zmian i dodanie odpowiednich wpisów do loga (legacy)
        addUpdateChangeLog(task, existingTask);
        
        // Automatyczne wykrycie i logowanie zmian w nowym systemie
        changeLogService.logTaskChanges(existingTask, task, null);
        
        return taskRepository.save(task);
    }
    
    @Override
    public void deleteTask(Long id) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new EntityNotFoundException("Task", id);
        }
        
        Task task = taskOpt.get();
        
        // Walidacja biznesowa przed usunięciem
        validateTaskForDeletion(task);
        
        // Dodanie loga usunięcia w nowym systemie
        changeLogService.logChange(task, "task", task.getTitle(), "deleted", 
                                 ChangeLogOperationType.DELETE, "Task deleted: " + task.getTitle());
        
        taskRepository.deleteById(id);
    }
    
    @Override
    public List<Task> getTasksByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User", userId);
        }
        return taskRepository.findByAssignedTo_Id(userId);
    }
    
    @Override
    public List<Task> getTasksByStatus(TaskStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Task status cannot be null");
        }
        return taskRepository.findByStatus(status);
    }
    
    @Override
    public List<Task> getTasksByPriority(TaskPriority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Task priority cannot be null");
        }
        return taskRepository.findByPriority(priority);
    }
    
    @Override
    public Task assignTaskToUser(Long taskId, Long userId) {
        Task task = getTaskById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task", taskId));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User", userId));
        
        // Walidacja biznesowa
        validateTaskAssignment(task, user);
        
        String oldAssignment = task.getAssignedTo() != null ? 
            task.getAssignedTo().getUsername() : "unassigned";
        
        // Utworzenie kopii zadania do porównania
        Task oldTask = createTaskCopy(task);
        
        task.setAssignedTo(user);
        
        // Legacy change log
        addChangeLogEntry(task, "assigned", 
            "Task assigned from '" + oldAssignment + "' to '" + user.getUsername() + "'");
        
        // Nowy system change log
        changeLogService.logChange(task, "assignedTo", oldAssignment, user.getUsername(), 
                                 ChangeLogOperationType.ASSIGN, user);
        
        return taskRepository.save(task);
    }
    
    @Override
    public Task unassignTask(Long taskId) {
        Task task = getTaskById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task", taskId));
        
        if (task.getAssignedTo() == null) {
            throw new BusinessLogicException("TASK_NOT_ASSIGNED", "Task is not assigned to any user");
        }
        
        String oldAssignment = task.getAssignedTo().getUsername();
        
        task.setAssignedTo(null);
        
        // Legacy change log
        addChangeLogEntry(task, "unassigned", 
            "Task unassigned from '" + oldAssignment + "'");
        
        // Nowy system change log
        changeLogService.logChange(task, "assignedTo", oldAssignment, "unassigned", 
                                 ChangeLogOperationType.UNASSIGN);
        
        return taskRepository.save(task);
    }
    
    @Override
    public Task changeTaskStatus(Long taskId, TaskStatus newStatus) {
        Task task = getTaskById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task", taskId));
        
        if (newStatus == null) {
            throw new IllegalArgumentException("Task status cannot be null");
        }
        
        TaskStatus oldStatus = task.getStatus();
        
        // Walidacja przejść stanów
        validateStatusTransition(oldStatus, newStatus);
        
        task.setStatus(newStatus);
        
        String oldStatusName = oldStatus != null ? oldStatus.getDisplayName() : "none";
        String newStatusName = newStatus.getDisplayName();
        
        // Legacy change log
        addChangeLogEntry(task, "status_changed", 
            "Status changed from '" + oldStatusName + "' to '" + newStatusName + "'");
        
        // Nowy system change log
        changeLogService.logChange(task, "status", oldStatusName, newStatusName, 
                                 ChangeLogOperationType.STATUS_CHANGE);
        
        return taskRepository.save(task);
    }
    
    @Override
    public Task changeTaskPriority(Long taskId, TaskPriority newPriority) {
        Task task = getTaskById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task", taskId));
        
        if (newPriority == null) {
            throw new IllegalArgumentException("Task priority cannot be null");
        }
        
        TaskPriority oldPriority = task.getPriority();
        task.setPriority(newPriority);
        
        String oldPriorityName = oldPriority != null ? oldPriority.getDisplayName() : "none";
        String newPriorityName = newPriority.getDisplayName();
        
        // Legacy change log
        addChangeLogEntry(task, "priority_changed", 
            "Priority changed from '" + oldPriorityName + "' to '" + newPriorityName + "'");
        
        // Nowy system change log
        changeLogService.logChange(task, "priority", oldPriorityName, newPriorityName, 
                                 ChangeLogOperationType.PRIORITY_CHANGE);
        
        return taskRepository.save(task);
    }

    // Metody walidacji biznesowej
    
    private void validateTaskForCreation(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new ValidationException("title", "Task title is required");
        }
        
        if (task.getTitle().length() < 3) {
            throw new ValidationException("title", "Task title must be at least 3 characters long");
        }
        
        if (task.getStatus() == null) {
            throw new ValidationException("status", "Task status is required");
        }
        
        if (task.getPriority() == null) {
            throw new ValidationException("priority", "Task priority is required");
        }
    }
    
    private void validateTaskForUpdate(Task newTask, Task existingTask) {
        validateTaskForCreation(newTask);
        
        // Sprawdzenie czy zadanie nie jest w stanie który uniemożliwia edycję
        if (existingTask.getStatus() == TaskStatus.DONE) {
            throw new BusinessLogicException("TASK_COMPLETED", 
                "Cannot modify completed task. Change status first.");
        }
    }
    
    private void validateTaskForDeletion(Task task) {
        // Sprawdzenie czy zadanie nie jest w trakcie realizacji
        if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            throw new BusinessLogicException("TASK_IN_PROGRESS", 
                "Cannot delete task that is in progress. Complete or cancel the task first.");
        }
    }
    
    private void validateTaskAssignment(Task task, User user) {
        // Sprawdzenie czy zadanie nie jest już zakończone
        if (task.getStatus() == TaskStatus.DONE) {
            throw new BusinessLogicException("TASK_COMPLETED", 
                "Cannot assign completed task to user");
        }
        
        // Sprawdzenie czy użytkownik nie ma już za dużo zadań (przykład reguły biznesowej)
        List<Task> userTasks = taskRepository.findByAssignedTo_Id(user.getId());
        long activeTasks = userTasks.stream()
            .filter(t -> t.getStatus() != TaskStatus.DONE)
            .count();
            
        if (activeTasks >= 10) {
            throw new BusinessLogicException("USER_OVERLOADED", 
                String.format("User %s already has %d active tasks. Maximum is 10.", 
                            user.getUsername(), activeTasks));
        }
    }
    
    private void validateStatusTransition(TaskStatus oldStatus, TaskStatus newStatus) {
        // Przykładowe reguły przejść stanów
        if (oldStatus == TaskStatus.DONE && newStatus != TaskStatus.DONE) {
            throw new BusinessLogicException("INVALID_STATUS_TRANSITION", 
                "Cannot change status of completed task. Task must remain completed.");
        }
        
        // Można dodać więcej reguł przejść...
    }

    /**
     * Tworzy kopię zadania do porównania (shallow copy wystarczący dla naszych potrzeb)
     */
    private Task createTaskCopy(Task original) {
        Task copy = new Task();
        copy.setId(original.getId());
        copy.setTitle(original.getTitle());
        copy.setDescription(original.getDescription());
        copy.setStatus(original.getStatus());
        copy.setPriority(original.getPriority());
        copy.setDueDate(original.getDueDate());
        copy.setAssignedTo(original.getAssignedTo());
        copy.setChangeLog(original.getChangeLog());
        return copy;
    }
    
    /**
     * Dodaje wpis do loga zmian zadania (legacy system).
     * Używa wstrzykniętego ObjectMapper z globalną konfiguracją.
     * @deprecated Używaj changeLogService.logChange() w nowym kodzie
     */
    @Deprecated
    private void addChangeLogEntry(Task task, String action, String description) {
        try {
            List<Map<String, Object>> log = getCurrentChangeLog(task);
            
            Map<String, Object> logEntry = Map.of(
                "timestamp", LocalDateTime.now(),
                "action", action,
                "description", description,
                "title", task.getTitle() != null ? task.getTitle() : ""
            );
            
            log.add(logEntry);
            task.setChangeLog(objectMapper.writeValueAsString(log));
        } catch (Exception e) {
            // W przypadku błędu serializacji, ustawiamy podstawowy log z informacją o błędzie
            String errorLog = String.format(
                "[{\"timestamp\":\"%s\",\"action\":\"%s\",\"description\":\"%s\",\"error\":\"JSON serialization failed: %s\"}]",
                LocalDateTime.now().toString(), action, description, e.getMessage()
            );
            task.setChangeLog(errorLog);
        }
    }
    
    /**
     * Dodaje wpisy do loga na podstawie zmian między starą a nową wersją zadania (legacy system).
     * Wykorzystuje szczegółowe porównanie pól i czytelne opisy zmian.
     * @deprecated Używaj changeLogService.logTaskChanges() w nowym kodzie
     */
    @Deprecated
    private void addUpdateChangeLog(Task newTask, Task oldTask) {
        List<String> changes = new ArrayList<>();
        
        // Sprawdzenie zmian w tytule
        if (!newTask.getTitle().equals(oldTask.getTitle())) {
            changes.add(String.format("title from '%s' to '%s'", 
                oldTask.getTitle(), newTask.getTitle()));
        }
        
        // Sprawdzenie zmian w opisie
        if (!java.util.Objects.equals(newTask.getDescription(), oldTask.getDescription())) {
            String oldDesc = oldTask.getDescription() != null ? 
                (oldTask.getDescription().length() > 50 ? 
                    oldTask.getDescription().substring(0, 50) + "..." : oldTask.getDescription()) : "empty";
            String newDesc = newTask.getDescription() != null ? 
                (newTask.getDescription().length() > 50 ? 
                    newTask.getDescription().substring(0, 50) + "..." : newTask.getDescription()) : "empty";
            changes.add(String.format("description from '%s' to '%s'", oldDesc, newDesc));
        }
        
        // Sprawdzenie zmian w statusie
        if (!java.util.Objects.equals(newTask.getStatus(), oldTask.getStatus())) {
            changes.add(String.format("status from '%s' to '%s'",
                oldTask.getStatus() != null ? oldTask.getStatus().getDisplayName() : "none",
                newTask.getStatus().getDisplayName()));
        }
        
        // Sprawdzenie zmian w priorytecie
        if (!java.util.Objects.equals(newTask.getPriority(), oldTask.getPriority())) {
            changes.add(String.format("priority from '%s' to '%s'",
                oldTask.getPriority() != null ? oldTask.getPriority().getDisplayName() : "none",
                newTask.getPriority().getDisplayName()));
        }
        
        // Sprawdzenie zmian w dacie wykonania
        if (!java.util.Objects.equals(newTask.getDueDate(), oldTask.getDueDate())) {
            changes.add(String.format("due date from '%s' to '%s'",
                oldTask.getDueDate() != null ? oldTask.getDueDate().toString() : "none",
                newTask.getDueDate() != null ? newTask.getDueDate().toString() : "none"));
        }
        
        // Sprawdzenie zmian w przypisaniu użytkownika
        if (!java.util.Objects.equals(
                newTask.getAssignedTo() != null ? newTask.getAssignedTo().getId() : null,
                oldTask.getAssignedTo() != null ? oldTask.getAssignedTo().getId() : null)) {
            changes.add(String.format("assignment from '%s' to '%s'",
                oldTask.getAssignedTo() != null ? oldTask.getAssignedTo().getUsername() : "unassigned",
                newTask.getAssignedTo() != null ? newTask.getAssignedTo().getUsername() : "unassigned"));
        }
        
        if (!changes.isEmpty()) {
            String description = "Updated: " + String.join("; ", changes);
            addChangeLogEntry(newTask, "updated", description);
        }
    }
    
    /**
     * Pobiera obecny log zmian lub tworzy nowy (legacy system).
     * Używa wstrzykniętego ObjectMapper z obsługą błędów.
     * @deprecated Używaj changeLogService.getTaskHistory() w nowym kodzie
     */
    @Deprecated
    private List<Map<String, Object>> getCurrentChangeLog(Task task) {
        if (task.getChangeLog() == null || task.getChangeLog().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(task.getChangeLog(), new TypeReference<>() {});
        } catch (Exception e) {
            // W przypadku błędu parsowania, zwracamy nową listę i logujemy błąd
            return new ArrayList<>();
        }
    }

    // Nowe metody dla zaawansowanego zarządzania historią zmian
    
    /**
     * Pobiera szczegółową historię zmian dla zadania
     */
    public List<ChangeLogEntry> getTaskChangeHistory(Long taskId) {
        return changeLogService.getTaskHistory(taskId);
    }
    
    /**
     * Pobiera statystyki zmian dla zadania
     */
    public ChangeLogStats getTaskChangeStats(Long taskId) {
        return changeLogService.getTaskChangeStats(taskId);
    }
    
    /**
     * Eksportuje historię zmian zadania do JSON
     */
    public String exportTaskChangeHistory(Long taskId) {
        return changeLogService.exportTaskHistoryToJson(taskId);
    }
} 