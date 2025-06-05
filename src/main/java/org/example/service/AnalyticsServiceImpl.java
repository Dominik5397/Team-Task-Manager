package org.example.service;

import org.example.*;
import org.example.dto.TaskSummaryDto;
import org.example.dto.UserStatsDto;
import org.example.dto.DashboardDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementacja serwisu analitycznego z wydajnymi zapytaniami agregacyjnymi.
 */
@Service
public class AnalyticsServiceImpl implements AnalyticsService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ChangeLogEntryRepository changeLogRepository;
    
    @Override
    public TaskSummaryDto getTaskSummary() {
        TaskSummaryDto summary = new TaskSummaryDto();
        
        // Podstawowe liczby
        long totalTasks = taskRepository.count();
        Long completedTasks = taskRepository.countCompletedTasks();
        Long activeTasks = taskRepository.countActiveTasks();
        Long overdueTasks = taskRepository.countOverdueTasks(LocalDate.now());
        Long unassignedTasks = taskRepository.countUnassignedTasks();
        
        summary.setTotalTasks((int) totalTasks);
        summary.setCompletedTasks(completedTasks.intValue());
        summary.setActiveTasks(activeTasks.intValue());
        summary.setOverdueTasks(overdueTasks.intValue());
        summary.setUnassignedTasks(unassignedTasks.intValue());
        
        // Wskaźnik ukończenia
        double completionRate = totalTasks > 0 ? 
            (completedTasks.doubleValue() / totalTasks) * 100 : 0;
        summary.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);
        
        // Dystrybucja według statusu
        Map<String, Integer> tasksByStatus = new HashMap<>();
        List<Object[]> statusCounts = taskRepository.countTasksByStatus();
        for (Object[] row : statusCounts) {
            TaskStatus status = (TaskStatus) row[0];
            Long count = (Long) row[1];
            tasksByStatus.put(status.getDisplayName(), count.intValue());
        }
        summary.setTasksByStatus(tasksByStatus);
        
        // Dystrybucja według priorytetu
        Map<String, Integer> tasksByPriority = new HashMap<>();
        List<Object[]> priorityCounts = taskRepository.countTasksByPriority();
        for (Object[] row : priorityCounts) {
            TaskPriority priority = (TaskPriority) row[0];
            Long count = (Long) row[1];
            tasksByPriority.put(priority.getDisplayName(), count.intValue());
        }
        summary.setTasksByPriority(tasksByPriority);
        
        return summary;
    }
    
    @Override
    public UserStatsDto getUserStats() {
        UserStatsDto stats = new UserStatsDto();
        
        // Podstawowe liczby użytkowników
        long totalUsers = userRepository.count();
        List<User> usersWithTasks = userRepository.findUsersWithTasks();
        List<User> usersWithoutTasks = userRepository.findUsersWithoutTasks();
        
        stats.setTotalUsers((int) totalUsers);
        stats.setActiveUsers(usersWithTasks.size());
        stats.setInactiveUsers(usersWithoutTasks.size());
        
        // Dystrybucja zadań według użytkowników
        List<Object[]> userTaskCounts = taskRepository.countTasksByUser();
        Map<String, Integer> usersByTaskCount = new HashMap<>();
        double totalTasksSum = 0;
        
        for (Object[] row : userTaskCounts) {
            String username = (String) row[0];
            Long count = (Long) row[1];
            usersByTaskCount.put(username, count.intValue());
            totalTasksSum += count;
        }
        stats.setUsersByTaskCount(usersByTaskCount);
        
        // Średnia zadań na użytkownika
        double averageTasksPerUser = totalUsers > 0 ? totalTasksSum / totalUsers : 0;
        stats.setAverageTasksPerUser(Math.round(averageTasksPerUser * 100.0) / 100.0);
        
        // Najlepsi wykonawcy
        List<Object[]> topPerformers = taskRepository.findTopPerformersByCompletedTasks();
        Map<String, Object> topPerformersMap = new HashMap<>();
        List<Map<String, Object>> performersList = new ArrayList<>();
        
        for (int i = 0; i < Math.min(5, topPerformers.size()); i++) {
            Object[] row = topPerformers.get(i);
            Map<String, Object> performer = new HashMap<>();
            performer.put("username", (String) row[0]);
            performer.put("completedTasks", ((Long) row[1]).intValue());
            performer.put("rank", i + 1);
            performersList.add(performer);
        }
        topPerformersMap.put("top5", performersList);
        stats.setTopPerformers(topPerformersMap);
        
        return stats;
    }
    
    @Override
    public DashboardDto getDashboardData() {
        DashboardDto dashboard = new DashboardDto();
        
        // Główne sekcje
        dashboard.setTaskSummary(getTaskSummary());
        dashboard.setUserStats(getUserStats());
        dashboard.setRecentActivity(getRecentActivity(10));
        dashboard.setProgressTracking(getProgressTracking(
            LocalDate.now().minusDays(30), LocalDate.now()));
        dashboard.setTaskDistribution(getTaskDistribution());
        dashboard.setPerformanceMetrics(getPerformanceMetrics());
        
        return dashboard;
    }
    
    @Override
    public Map<String, Object> getTaskDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        
        // Dystrybucja według statusu i priorytetu
        List<Object[]> statusPriorityCounts = taskRepository.getTaskDistributionByStatusAndPriority();
        Map<String, Integer> statusPriorityMap = new HashMap<>();
        
        for (Object[] row : statusPriorityCounts) {
            String combination = (String) row[0];
            Long count = (Long) row[1];
            statusPriorityMap.put(combination, count.intValue());
        }
        distribution.put("statusPriority", statusPriorityMap);
        
        // Dodatkowe statystyki
        distribution.put("assignmentRate", calculateAssignmentRate());
        distribution.put("priorityDistribution", getPriorityDistribution());
        distribution.put("overdueRate", calculateOverdueRate());
        
        return distribution;
    }
    
    @Override
    public Map<String, Object> getProgressTracking(LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> progress = new HashMap<>();
        
        // Zadania zmodyfikowane w okresie
        List<Task> modifiedTasks = taskRepository.findTasksModifiedBetween(fromDate, toDate);
        progress.put("modifiedTasksCount", modifiedTasks.size());
        
        // Statystyki zmian w okresie
        List<ChangeLogEntry> recentChanges = changeLogRepository.findByChangedAtBetweenOrderByChangedAtDesc(fromDate.atStartOfDay(), toDate.atTime(23, 59, 59));
        
        Map<String, Integer> changesByType = recentChanges.stream()
            .collect(Collectors.groupingBy(
                change -> change.getOperationType().getDisplayName(),
                Collectors.summingInt(change -> 1)
            ));
        progress.put("changesByType", changesByType);
        
        // Trend zakończeń zadań
        Map<String, Object> completionTrend = calculateCompletionTrend(fromDate, toDate);
        progress.put("completionTrend", completionTrend);
        
        progress.put("fromDate", fromDate);
        progress.put("toDate", toDate);
        
        return progress;
    }
    
    @Override
    public Map<String, Double> getPerformanceMetrics() {
        Map<String, Double> metrics = new HashMap<>();
        
        // Wskaźnik ukończeń
        long totalTasks = taskRepository.count();
        Long completedTasks = taskRepository.countCompletedTasks();
        double completionRate = totalTasks > 0 ? 
            (completedTasks.doubleValue() / totalTasks) * 100 : 0;
        metrics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        
        // Wskaźnik przypisania
        metrics.put("assignmentRate", calculateAssignmentRate());
        
        // Wskaźnik przeterminowania
        metrics.put("overdueRate", calculateOverdueRate());
        
        // Średni czas realizacji (symulowany - w rzeczywistej aplikacji byłby wyliczany na podstawie dat)
        metrics.put("averageCompletionTime", 5.2); // dni
        
        // Produktywność zespołu
        metrics.put("teamProductivity", calculateTeamProductivity());
        
        return metrics;
    }
    
    @Override
    public List<Map<String, Object>> getRecentActivity(int limit) {
        List<ChangeLogEntry> recentChanges = changeLogRepository.findTopNByOrderByChangedAtDesc(limit);
        
        return recentChanges.stream()
            .map(change -> {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", change.getId());
                activity.put("taskId", change.getTask().getId());
                activity.put("taskTitle", change.getTask().getTitle());
                activity.put("operationType", change.getOperationType().getDisplayName());
                activity.put("description", change.getDescription());
                activity.put("changedAt", change.getChangedAt());
                
                if (change.getChangedBy() != null) {
                    activity.put("changedBy", change.getChangedBy().getUsername());
                }
                
                if (change.hasValueChange()) {
                    activity.put("fieldName", change.getFieldName());
                    activity.put("oldValue", change.getOldValue());
                    activity.put("newValue", change.getNewValue());
                }
                
                return activity;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getTrendData(LocalDate fromDate, LocalDate toDate) {
        // Implementacja trendu - można rozszerzyć o szczegółowe analizy
        Map<String, Object> trend = new HashMap<>();
        trend.put("fromDate", fromDate);
        trend.put("toDate", toDate);
        trend.put("period", "custom");
        
        // Dodaj więcej logiki trendu według potrzeb
        return trend;
    }
    
    @Override
    public Map<String, Object> getCompletionForecast() {
        Map<String, Object> forecast = new HashMap<>();
        
        // Prosta prognoza na podstawie obecnego tempa
        Long activeTasks = taskRepository.countActiveTasks();
        double averageCompletionRate = 0.15; // 15% zadań dziennie (przykład)
        
        double estimatedDays = activeTasks > 0 ? activeTasks / averageCompletionRate : 0;
        LocalDate estimatedCompletion = LocalDate.now().plusDays((long) estimatedDays);
        
        forecast.put("activeTasks", activeTasks.intValue());
        forecast.put("estimatedCompletionDate", estimatedCompletion);
        forecast.put("estimatedDays", Math.round(estimatedDays));
        forecast.put("confidenceLevel", "Medium");
        
        return forecast;
    }
    
    // Metody pomocnicze
    
    private double calculateAssignmentRate() {
        long totalTasks = taskRepository.count();
        Long unassignedTasks = taskRepository.countUnassignedTasks();
        
        if (totalTasks == 0) return 0;
        
        double assignedTasks = totalTasks - unassignedTasks;
        double rate = (assignedTasks / totalTasks) * 100;
        return Math.round(rate * 100.0) / 100.0;
    }
    
    private Map<String, Double> getPriorityDistribution() {
        Map<String, Double> distribution = new HashMap<>();
        long totalTasks = taskRepository.count();
        
        if (totalTasks == 0) return distribution;
        
        List<Object[]> priorityCounts = taskRepository.countTasksByPriority();
        for (Object[] row : priorityCounts) {
            TaskPriority priority = (TaskPriority) row[0];
            Long count = (Long) row[1];
            double percentage = (count.doubleValue() / totalTasks) * 100;
            distribution.put(priority.getDisplayName(), Math.round(percentage * 100.0) / 100.0);
        }
        
        return distribution;
    }
    
    private double calculateOverdueRate() {
        long totalTasks = taskRepository.count();
        Long overdueTasks = taskRepository.countOverdueTasks(LocalDate.now());
        
        if (totalTasks == 0) return 0;
        
        double rate = (overdueTasks.doubleValue() / totalTasks) * 100;
        return Math.round(rate * 100.0) / 100.0;
    }
    
    private double calculateTeamProductivity() {
        // Uproszczona metryka produktywności
        Long completedTasks = taskRepository.countCompletedTasks();
        long totalUsers = userRepository.count();
        
        if (totalUsers == 0) return 0;
        
        double productivity = completedTasks.doubleValue() / totalUsers;
        return Math.round(productivity * 100.0) / 100.0;
    }
    
    private Map<String, Object> calculateCompletionTrend(LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> trend = new HashMap<>();
        
        // Można rozszerzyć o bardziej szczegółową analizę trendu
        List<ChangeLogEntry> completions = changeLogRepository.findByOperationTypeAndChangedAtBetweenOrderByChangedAtDesc(
            ChangeLogOperationType.STATUS_CHANGE, fromDate.atStartOfDay(), toDate.atTime(23, 59, 59)
        );
        
        long completionsCount = completions.stream()
            .filter(change -> "Done".equals(change.getNewValue()))
            .count();
        
        trend.put("completionsInPeriod", completionsCount);
        trend.put("averagePerDay", completionsCount / (double) (toDate.toEpochDay() - fromDate.toEpochDay() + 1));
        
        return trend;
    }
} 