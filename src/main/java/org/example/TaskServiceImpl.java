package org.example;

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
 */
@Service
public class TaskServiceImpl implements TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;
    
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
        // Dodanie loga tworzenia
        addChangeLogEntry(task, "created", "Task created: " + task.getTitle());
        return taskRepository.save(task);
    }
    
    @Override
    public Task updateTask(Long id, Task task) {
        Optional<Task> existingTaskOpt = taskRepository.findById(id);
        if (existingTaskOpt.isEmpty()) {
            throw new RuntimeException("Task not found with id: " + id);
        }
        
        Task existingTask = existingTaskOpt.get();
        task.setId(id);
        
        // Zachowanie istniejącego loga i dodanie nowego wpisu
        if (existingTask.getChangeLog() != null) {
            task.setChangeLog(existingTask.getChangeLog());
        }
        
        // Sprawdzenie zmian i dodanie odpowiednich wpisów do loga
        addUpdateChangeLog(task, existingTask);
        
        return taskRepository.save(task);
    }
    
    @Override
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }
    
    @Override
    public List<Task> getTasksByUser(Long userId) {
        return taskRepository.findByAssignedTo_Id(userId);
    }
    
    @Override
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }
    
    @Override
    public List<Task> getTasksByPriority(TaskPriority priority) {
        return taskRepository.findByPriority(priority);
    }
    
    @Override
    public Task assignTaskToUser(Long taskId, Long userId) {
        Task task = getTaskById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        String oldAssignment = task.getAssignedTo() != null ? 
            task.getAssignedTo().getUsername() : "unassigned";
        
        task.setAssignedTo(user);
        addChangeLogEntry(task, "assigned", 
            "Task assigned from '" + oldAssignment + "' to '" + user.getUsername() + "'");
        
        return taskRepository.save(task);
    }
    
    @Override
    public Task unassignTask(Long taskId) {
        Task task = getTaskById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        
        String oldAssignment = task.getAssignedTo() != null ? 
            task.getAssignedTo().getUsername() : "unassigned";
        
        task.setAssignedTo(null);
        addChangeLogEntry(task, "unassigned", 
            "Task unassigned from '" + oldAssignment + "'");
        
        return taskRepository.save(task);
    }
    
    @Override
    public Task changeTaskStatus(Long taskId, TaskStatus newStatus) {
        Task task = getTaskById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);
        
        addChangeLogEntry(task, "status_changed", 
            "Status changed from '" + (oldStatus != null ? oldStatus.getDisplayName() : "none") + 
            "' to '" + newStatus.getDisplayName() + "'");
        
        return taskRepository.save(task);
    }
    
    @Override
    public Task changeTaskPriority(Long taskId, TaskPriority newPriority) {
        Task task = getTaskById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        
        TaskPriority oldPriority = task.getPriority();
        task.setPriority(newPriority);
        
        addChangeLogEntry(task, "priority_changed", 
            "Priority changed from '" + (oldPriority != null ? oldPriority.getDisplayName() : "none") + 
            "' to '" + newPriority.getDisplayName() + "'");
        
        return taskRepository.save(task);
    }
    
    /**
     * Dodaje wpis do loga zmian zadania.
     * Używa wstrzykniętego ObjectMapper z globalną konfiguracją.
     */
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
     * Dodaje wpisy do loga na podstawie zmian między starą a nową wersją zadania.
     * Wykorzystuje szczegółowe porównanie pól i czytelne opisy zmian.
     */
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
     * Pobiera obecny log zmian lub tworzy nowy.
     * Używa wstrzykniętego ObjectMapper z obsługą błędów.
     */
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
} 