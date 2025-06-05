package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Task createTask(@Valid @RequestBody Task task) {
        return taskService.createTask(task);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @Valid @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // ===== Nowe endpointy biznesowe =====
    
    @GetMapping("/user/{userId}")
    public List<Task> getTasksByUser(@PathVariable Long userId) {
        return taskService.getTasksByUser(userId);
    }
    
    @GetMapping("/status/{status}")
    public List<Task> getTasksByStatus(@PathVariable TaskStatus status) {
        return taskService.getTasksByStatus(status);
    }
    
    @GetMapping("/priority/{priority}")
    public List<Task> getTasksByPriority(@PathVariable TaskPriority priority) {
        return taskService.getTasksByPriority(priority);
    }
    
    @PutMapping("/{taskId}/assign/{userId}")
    public Task assignTaskToUser(@PathVariable Long taskId, @PathVariable Long userId) {
        return taskService.assignTaskToUser(taskId, userId);
    }
    
    @PutMapping("/{taskId}/unassign")
    public Task unassignTask(@PathVariable Long taskId) {
        return taskService.unassignTask(taskId);
    }
    
    @PutMapping("/{taskId}/status")
    public Task changeTaskStatus(@PathVariable Long taskId, @RequestParam TaskStatus status) {
        return taskService.changeTaskStatus(taskId, status);
    }
    
    @PutMapping("/{taskId}/priority")
    public Task changeTaskPriority(@PathVariable Long taskId, @RequestParam TaskPriority priority) {
        return taskService.changeTaskPriority(taskId, priority);
    }
} 