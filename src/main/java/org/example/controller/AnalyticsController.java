package org.example.controller;

import org.example.dto.TaskSummaryDto;
import org.example.dto.UserStatsDto;
import org.example.dto.DashboardDto;
import org.example.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Kontroler dla API analityki - dostarcza zagregowane dane dla dashboardu.
 * Optymalizowany pod kątem wydajności z użyciem dedykowanych zapytań agregacyjnych.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Pobiera podstawowe podsumowanie zadań
     * GET /api/analytics/task-summary
     */
    @GetMapping("/task-summary")
    public TaskSummaryDto getTaskSummary() {
        return analyticsService.getTaskSummary();
    }

    /**
     * Pobiera statystyki użytkowników
     * GET /api/analytics/user-stats
     */
    @GetMapping("/user-stats")
    public UserStatsDto getUserStats() {
        return analyticsService.getUserStats();
    }

    /**
     * Pobiera kompletne dane dashboardu (łączy wszystkie analityki)
     * GET /api/analytics/dashboard
     */
    @GetMapping("/dashboard")
    public DashboardDto getDashboardData() {
        return analyticsService.getDashboardData();
    }

    /**
     * Pobiera dystrybucję zadań według różnych kryteriów
     * GET /api/analytics/task-distribution
     */
    @GetMapping("/task-distribution")
    public Map<String, Object> getTaskDistribution() {
        return analyticsService.getTaskDistribution();
    }

    /**
     * Śledzenie postępu w określonym okresie
     * GET /api/analytics/progress-tracking?fromDate=2024-01-01&toDate=2024-01-31
     */
    @GetMapping("/progress-tracking")
    public Map<String, Object> getProgressTracking(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return analyticsService.getProgressTracking(fromDate, toDate);
    }

    /**
     * Śledzenie postępu za ostatnie 30 dni (domyślnie)
     * GET /api/analytics/progress-tracking/recent
     */
    @GetMapping("/progress-tracking/recent")
    public Map<String, Object> getRecentProgressTracking() {
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(30);
        return analyticsService.getProgressTracking(fromDate, toDate);
    }

    /**
     * Metryki wydajności zespołu
     * GET /api/analytics/performance-metrics
     */
    @GetMapping("/performance-metrics")
    public Map<String, Double> getPerformanceMetrics() {
        return analyticsService.getPerformanceMetrics();
    }

    /**
     * Ostatnia aktywność w systemie
     * GET /api/analytics/recent-activity?limit=10
     */
    @GetMapping("/recent-activity")
    public List<Map<String, Object>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        return analyticsService.getRecentActivity(limit);
    }

    /**
     * Statystyki trendu dla wykresów
     * GET /api/analytics/trend-data?fromDate=2024-01-01&toDate=2024-01-31
     */
    @GetMapping("/trend-data")
    public Map<String, Object> getTrendData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return analyticsService.getTrendData(fromDate, toDate);
    }

    /**
     * Prognoza zakończenia zadań
     * GET /api/analytics/completion-forecast
     */
    @GetMapping("/completion-forecast")
    public Map<String, Object> getCompletionForecast() {
        return analyticsService.getCompletionForecast();
    }

    /**
     * Kompaktowy endpoint dla widgetów dashboardu
     * GET /api/analytics/widgets
     */
    @GetMapping("/widgets")
    public Map<String, Object> getDashboardWidgets() {
        Map<String, Object> widgets = Map.of(
            "taskSummary", analyticsService.getTaskSummary(),
            "userStats", analyticsService.getUserStats(),
            "performanceMetrics", analyticsService.getPerformanceMetrics(),
            "recentActivity", analyticsService.getRecentActivity(5)
        );
        return widgets;
    }

    /**
     * Szybkie statystyki dla nagłówka dashboardu
     * GET /api/analytics/quick-stats
     */
    @GetMapping("/quick-stats")
    public Map<String, Object> getQuickStats() {
        TaskSummaryDto summary = analyticsService.getTaskSummary();
        UserStatsDto userStats = analyticsService.getUserStats();
        
        return Map.of(
            "totalTasks", summary.getTotalTasks(),
            "completedTasks", summary.getCompletedTasks(),
            "activeTasks", summary.getActiveTasks(),
            "totalUsers", userStats.getTotalUsers(),
            "activeUsers", userStats.getActiveUsers(),
            "completionRate", summary.getCompletionRate()
        );
    }

    /**
     * Endpoint do testowania wydajności analytics
     * GET /api/analytics/health-check
     */
    @GetMapping("/health-check")
    public Map<String, Object> getAnalyticsHealthCheck() {
        long startTime = System.currentTimeMillis();
        
        // Wykonaj podstawowe zapytania
        TaskSummaryDto summary = analyticsService.getTaskSummary();
        UserStatsDto userStats = analyticsService.getUserStats();
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        return Map.of(
            "status", "healthy",
            "executionTimeMs", executionTime,
            "dataAvailable", summary.getTotalTasks() > 0,
            "totalTasks", summary.getTotalTasks(),
            "totalUsers", userStats.getTotalUsers(),
            "timestamp", LocalDate.now()
        );
    }
} 