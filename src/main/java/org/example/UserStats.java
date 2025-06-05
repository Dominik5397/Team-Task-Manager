package org.example;

/**
 * Klasa reprezentująca statystyki użytkownika.
 * Zawiera informacje o zadaniach przypisanych do użytkownika.
 */
public class UserStats {
    
    private Long userId;
    private String username;
    private int totalTasks;
    private int completedTasks;
    private int inProgressTasks;
    private int todoTasks;
    private int highPriorityTasks;
    private int mediumPriorityTasks;
    private int lowPriorityTasks;
    private int overdueTasks;
    
    // Konstruktor domyślny
    public UserStats() {}
    
    // Konstruktor z parametrami
    public UserStats(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }
    
    // Gettery i settery
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
    
    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
    
    public int getInProgressTasks() { return inProgressTasks; }
    public void setInProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; }
    
    public int getTodoTasks() { return todoTasks; }
    public void setTodoTasks(int todoTasks) { this.todoTasks = todoTasks; }
    
    public int getHighPriorityTasks() { return highPriorityTasks; }
    public void setHighPriorityTasks(int highPriorityTasks) { this.highPriorityTasks = highPriorityTasks; }
    
    public int getMediumPriorityTasks() { return mediumPriorityTasks; }
    public void setMediumPriorityTasks(int mediumPriorityTasks) { this.mediumPriorityTasks = mediumPriorityTasks; }
    
    public int getLowPriorityTasks() { return lowPriorityTasks; }
    public void setLowPriorityTasks(int lowPriorityTasks) { this.lowPriorityTasks = lowPriorityTasks; }
    
    public int getOverdueTasks() { return overdueTasks; }
    public void setOverdueTasks(int overdueTasks) { this.overdueTasks = overdueTasks; }
    
    // Metody pomocnicze
    public double getCompletionRate() {
        if (totalTasks == 0) return 0.0;
        return (double) completedTasks / totalTasks * 100.0;
    }
    
    public int getActiveTasks() {
        return inProgressTasks + todoTasks;
    }
    
    public boolean hasOverdueTasks() {
        return overdueTasks > 0;
    }
    
    @Override
    public String toString() {
        return "UserStats{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", totalTasks=" + totalTasks +
                ", completedTasks=" + completedTasks +
                ", completionRate=" + String.format("%.1f%%", getCompletionRate()) +
                '}';
    }
} 