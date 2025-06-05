package org.example.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO dla podsumowania zadań używane w analytics API.
 */
public class TaskSummaryDto {
    
    private int totalTasks;
    private Map<String, Integer> tasksByStatus;
    private Map<String, Integer> tasksByPriority;
    private int completedTasks;
    private int activeTasks;
    private int overdueTasks;
    private int unassignedTasks;
    private double completionRate;
    private LocalDateTime generatedAt;

    public TaskSummaryDto() {
        this.generatedAt = LocalDateTime.now();
    }

    // Gettery i settery
    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public Map<String, Integer> getTasksByStatus() { return tasksByStatus; }
    public void setTasksByStatus(Map<String, Integer> tasksByStatus) { this.tasksByStatus = tasksByStatus; }

    public Map<String, Integer> getTasksByPriority() { return tasksByPriority; }
    public void setTasksByPriority(Map<String, Integer> tasksByPriority) { this.tasksByPriority = tasksByPriority; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public int getActiveTasks() { return activeTasks; }
    public void setActiveTasks(int activeTasks) { this.activeTasks = activeTasks; }

    public int getOverdueTasks() { return overdueTasks; }
    public void setOverdueTasks(int overdueTasks) { this.overdueTasks = overdueTasks; }

    public int getUnassignedTasks() { return unassignedTasks; }
    public void setUnassignedTasks(int unassignedTasks) { this.unassignedTasks = unassignedTasks; }

    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
} 