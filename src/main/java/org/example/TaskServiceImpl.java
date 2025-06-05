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
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
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
     * Dodaje wpis do loga zmian zadania
     */
    private void addChangeLogEntry(Task task, String action, String description) {
        try {
            List<Map<String, Object>> log = getCurrentChangeLog(task);
            
            Map<String, Object> logEntry = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "action", action,
                "description", description,
                "title", task.getTitle() != null ? task.getTitle() : ""
            );
            
            log.add(logEntry);
            task.setChangeLog(objectMapper.writeValueAsString(log));
        } catch (Exception e) {
            // W przypadku błędu serializacji, ustawiamy podstawowy log
            task.setChangeLog("[{\"timestamp\":\"" + LocalDateTime.now().toString() + 
                "\",\"action\":\"" + action + "\",\"error\":\"Log serialization failed\"}]");
        }
    }
    
    /**
     * Dodaje wpisy do loga na podstawie zmian między starą a nową wersją zadania
     */
    private void addUpdateChangeLog(Task newTask, Task oldTask) {
        StringBuilder changes = new StringBuilder("Updated: ");
        boolean hasChanges = false;
        
        // Sprawdzenie zmian w tytule
        if (!newTask.getTitle().equals(oldTask.getTitle())) {
            changes.append("title from '").append(oldTask.getTitle())
                   .append("' to '").append(newTask.getTitle()).append("'; ");
            hasChanges = true;
        }
        
        // Sprawdzenie zmian w opisie
        if (!java.util.Objects.equals(newTask.getDescription(), oldTask.getDescription())) {
            changes.append("description; ");
            hasChanges = true;
        }
        
        // Sprawdzenie zmian w statusie
        if (!java.util.Objects.equals(newTask.getStatus(), oldTask.getStatus())) {
            changes.append("status from '")
                   .append(oldTask.getStatus() != null ? oldTask.getStatus().getDisplayName() : "none")
                   .append("' to '").append(newTask.getStatus().getDisplayName()).append("'; ");
            hasChanges = true;
        }
        
        // Sprawdzenie zmian w priorytecie
        if (!java.util.Objects.equals(newTask.getPriority(), oldTask.getPriority())) {
            changes.append("priority from '")
                   .append(oldTask.getPriority() != null ? oldTask.getPriority().getDisplayName() : "none")
                   .append("' to '").append(newTask.getPriority().getDisplayName()).append("'; ");
            hasChanges = true;
        }
        
        // Sprawdzenie zmian w dacie wykonania
        if (!java.util.Objects.equals(newTask.getDueDate(), oldTask.getDueDate())) {
            changes.append("due date; ");
            hasChanges = true;
        }
        
        if (hasChanges) {
            addChangeLogEntry(newTask, "updated", changes.toString());
        }
    }
    
    /**
     * Pobiera obecny log zmian lub tworzy nowy
     */
    private List<Map<String, Object>> getCurrentChangeLog(Task task) {
        if (task.getChangeLog() == null || task.getChangeLog().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(task.getChangeLog(), new TypeReference<>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
} 