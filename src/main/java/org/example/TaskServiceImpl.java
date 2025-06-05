package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.example.exception.EntityNotFoundException;
import org.example.exception.BusinessLogicException;
import org.example.exception.ValidationException;

/**
 * Implementation of TaskService with comprehensive business logic,
 * validation, and change logging using the new ChangeLogService system.
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
        // Set default status if not provided BEFORE validation
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        
        validateTaskForCreation(task);
        
        Task savedTask = taskRepository.save(task);
        
        // Log task creation in the new system
        changeLogService.logChange(savedTask, "task", null, "created", 
                                 ChangeLogOperationType.CREATE, "Task created: " + savedTask.getTitle());
        
        return savedTask;
    }
    
    @Override
    public Task updateTask(Long id, Task task) {
        Optional<Task> existingTaskOpt = getTaskById(id);
        if (existingTaskOpt.isEmpty()) {
            throw new EntityNotFoundException("Task", id);
        }
        
        Task existingTask = existingTaskOpt.get();
        
        validateTaskForUpdate(task, existingTask);
        
        task.setId(id);
        
        // Automatic change detection and logging in the new system
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
        
        // Business validation before deletion
        validateTaskForDeletion(task);
        
        // Log deletion in the new system
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
        
        // Business validation
        validateTaskAssignment(task, user);
        
        String oldAssignment = task.getAssignedTo() != null ? 
            task.getAssignedTo().getUsername() : "unassigned";
        
        task.setAssignedTo(user);
        
        // New system change log
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
        
        // New system change log
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
        
        // Validate status transitions
        validateStatusTransition(oldStatus, newStatus);
        
        task.setStatus(newStatus);
        
        String oldStatusName = oldStatus != null ? oldStatus.getDisplayName() : "none";
        String newStatusName = newStatus.getDisplayName();
        
        // New system change log
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
        
        // New system change log
        changeLogService.logChange(task, "priority", oldPriorityName, newPriorityName, 
                                 ChangeLogOperationType.PRIORITY_CHANGE);
        
        return taskRepository.save(task);
    }

    // Business validation methods
    
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
        
        // Status validation removed since we set default status before validation
        
        if (task.getPriority() == null) {
            throw new ValidationException("priority", "Task priority is required");
        }
    }
    
    private void validateTaskForUpdate(Task newTask, Task existingTask) {
        validateTaskForCreation(newTask);
        
        // Check if task is in a state that prevents editing
        if (existingTask.getStatus() == TaskStatus.DONE) {
            throw new BusinessLogicException("TASK_COMPLETED", 
                "Cannot modify completed task. Change status first.");
        }
    }
    
    private void validateTaskForDeletion(Task task) {
        // Check if task is not in progress
        if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            throw new BusinessLogicException("TASK_IN_PROGRESS", 
                "Cannot delete task that is in progress. Complete or cancel the task first.");
        }
    }
    
    private void validateTaskAssignment(Task task, User user) {
        // Check if task is not already completed
        if (task.getStatus() == TaskStatus.DONE) {
            throw new BusinessLogicException("TASK_COMPLETED", 
                "Cannot assign completed task to user");
        }
        
        // Check if user doesn't have too many tasks (business rule example)
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
        // Example status transition rules
        if (oldStatus == TaskStatus.DONE && newStatus != TaskStatus.DONE) {
            throw new BusinessLogicException("INVALID_STATUS_TRANSITION", 
                "Cannot change status of completed task. Task must remain completed.");
        }
        
        // More transition rules can be added...
    }

    // New methods for advanced change history management
    
    /**
     * Gets detailed change history for a task
     */
    public List<ChangeLogEntry> getTaskChangeHistory(Long taskId) {
        return changeLogService.getTaskHistory(taskId);
    }
    
    /**
     * Gets change statistics for a task
     */
    public ChangeLogStats getTaskChangeStats(Long taskId) {
        return changeLogService.getTaskChangeStats(taskId);
    }
    
    /**
     * Exports task change history to JSON
     */
    public String exportTaskChangeHistory(Long taskId) {
        return changeLogService.exportTaskHistoryToJson(taskId);
    }
} 