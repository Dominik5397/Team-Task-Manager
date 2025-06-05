package org.example.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO dla kompletnych danych dashboardu używane w analytics API.
 */
public class DashboardDto {
    
    private TaskSummaryDto taskSummary;
    private UserStatsDto userStats;
    private List<Map<String, Object>> recentActivity;
    private Map<String, Object> progressTracking;
    private Map<String, Object> taskDistribution;
    private Map<String, Double> performanceMetrics;
    private LocalDateTime generatedAt;

    public DashboardDto() {
        this.generatedAt = LocalDateTime.now();
    }

    // Gettery i settery
    public TaskSummaryDto getTaskSummary() { return taskSummary; }
    public void setTaskSummary(TaskSummaryDto taskSummary) { this.taskSummary = taskSummary; }

    public UserStatsDto getUserStats() { return userStats; }
    public void setUserStats(UserStatsDto userStats) { this.userStats = userStats; }

    public List<Map<String, Object>> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<Map<String, Object>> recentActivity) { this.recentActivity = recentActivity; }

    public Map<String, Object> getProgressTracking() { return progressTracking; }
    public void setProgressTracking(Map<String, Object> progressTracking) { this.progressTracking = progressTracking; }

    public Map<String, Object> getTaskDistribution() { return taskDistribution; }
    public void setTaskDistribution(Map<String, Object> taskDistribution) { this.taskDistribution = taskDistribution; }

    public Map<String, Double> getPerformanceMetrics() { return performanceMetrics; }
    public void setPerformanceMetrics(Map<String, Double> performanceMetrics) { this.performanceMetrics = performanceMetrics; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
} 