package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/validation-test")
public class ValidationTestController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @PostMapping("/test-user")
    public ResponseEntity<Map<String, String>> testUserValidation(@Valid @RequestBody User user) {
        // Ten endpoint służy do testowania walidacji użytkowników
        return ResponseEntity.ok(Map.of(
            "message", "User validation passed successfully!",
            "username", user.getUsername(),
            "email", user.getEmail()
        ));
    }
    
    @PostMapping("/test-task")
    public ResponseEntity<Map<String, String>> testTaskValidation(@Valid @RequestBody Task task) {
        // Ten endpoint służy do testowania walidacji zadań
        return ResponseEntity.ok(Map.of(
            "message", "Task validation passed successfully!",
            "title", task.getTitle(),
            "status", task.getStatus().getDisplayName(),
            "priority", task.getPriority().getDisplayName()
        ));
    }
    
    @GetMapping("/enum-values")
    public ResponseEntity<Map<String, Object>> getEnumValues() {
        return ResponseEntity.ok(Map.of(
            "statuses", Map.of(
                "TODO", TaskStatus.TODO.getDisplayName(),
                "IN_PROGRESS", TaskStatus.IN_PROGRESS.getDisplayName(),
                "DONE", TaskStatus.DONE.getDisplayName()
            ),
            "priorities", Map.of(
                "LOW", TaskPriority.LOW.getDisplayName(),
                "MEDIUM", TaskPriority.MEDIUM.getDisplayName(),
                "HIGH", TaskPriority.HIGH.getDisplayName()
            )
        ));
    }
} 