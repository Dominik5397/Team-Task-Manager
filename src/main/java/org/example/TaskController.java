package org.example;

import org.example.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task", id));
    }

    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
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