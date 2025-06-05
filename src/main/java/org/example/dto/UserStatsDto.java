package org.example.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO dla statystyk użytkowników używane w analytics API.
 */
public class UserStatsDto {
    
    private int totalUsers;
    private int activeUsers; // users with tasks
    private int inactiveUsers; // users without tasks
    private Map<String, Integer> usersByTaskCount;
    private Map<String, Object> topPerformers;
    private double averageTasksPerUser;
    private LocalDateTime generatedAt;

    public UserStatsDto() {
        this.generatedAt = LocalDateTime.now();
    }

    // Gettery i settery
    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }

    public int getActiveUsers() { return activeUsers; }
    public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }

    public int getInactiveUsers() { return inactiveUsers; }
    public void setInactiveUsers(int inactiveUsers) { this.inactiveUsers = inactiveUsers; }

    public Map<String, Integer> getUsersByTaskCount() { return usersByTaskCount; }
    public void setUsersByTaskCount(Map<String, Integer> usersByTaskCount) { this.usersByTaskCount = usersByTaskCount; }

    public Map<String, Object> getTopPerformers() { return topPerformers; }
    public void setTopPerformers(Map<String, Object> topPerformers) { this.topPerformers = topPerformers; }

    public double getAverageTasksPerUser() { return averageTasksPerUser; }
    public void setAverageTasksPerUser(double averageTasksPerUser) { this.averageTasksPerUser = averageTasksPerUser; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
} 