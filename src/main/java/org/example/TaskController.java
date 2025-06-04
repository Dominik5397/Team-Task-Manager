package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    @Autowired
    private TaskRepository taskRepository;

    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Task createTask(@RequestBody Task task) {
        List<Map<String, Object>> log = new ArrayList<>();
        log.add(Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "action", "created",
            "title", task.getTitle()
        ));
        try {
            ObjectMapper mapper = new ObjectMapper();
            task.setChangeLog(mapper.writeValueAsString(log));
        } catch (Exception ignored) {}
        return taskRepository.save(task);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        Task old = taskRepository.findById(id).orElse(null);
        if (old != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> log = old.getChangeLog() != null ?
                    mapper.readValue(old.getChangeLog(), new TypeReference<>() {}) : new ArrayList<>();
                log.add(Map.of(
                    "timestamp", LocalDateTime.now().toString(),
                    "action", "updated",
                    "title", task.getTitle()
                ));
                task.setChangeLog(mapper.writeValueAsString(log));
            } catch (Exception ignored) {}
        }
        return taskRepository.save(task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskRepository.deleteById(id);
    }
} 