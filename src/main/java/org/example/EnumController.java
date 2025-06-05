package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enums")
public class EnumController {

    @GetMapping("/task-statuses")
    public List<Map<String, Object>> getTaskStatuses() {
        return Arrays.stream(TaskStatus.values())
                .map(status -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", status.name());
                    map.put("displayName", status.getDisplayName());
                    map.put("label", status.getDisplayName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/task-priorities")
    public List<Map<String, Object>> getTaskPriorities() {
        return Arrays.stream(TaskPriority.values())
                .map(priority -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", priority.name());
                    map.put("displayName", priority.getDisplayName());
                    map.put("label", priority.getDisplayName());
                    map.put("level", priority.getLevel());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/all")
    public Map<String, Object> getAllEnums() {
        Map<String, Object> result = new HashMap<>();
        result.put("taskStatuses", getTaskStatuses());
        result.put("taskPriorities", getTaskPriorities());
        return result;
    }
} 