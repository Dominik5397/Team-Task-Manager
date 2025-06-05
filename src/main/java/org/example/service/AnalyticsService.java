package org.example.service;

import org.example.dto.TaskSummaryDto;
import org.example.dto.UserStatsDto;
import org.example.dto.DashboardDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interface serwisu analitycznego dostarczającego zagregowane dane.
 */
public interface AnalyticsService {
    
    /**
     * Pobiera podsumowanie zadań z podstawowymi statystykami
     */
    TaskSummaryDto getTaskSummary();
    
    /**
     * Pobiera statystyki użytkowników
     */
    UserStatsDto getUserStats();
    
    /**
     * Pobiera kompletne dane dla dashboardu
     */
    DashboardDto getDashboardData();
    
    /**
     * Pobiera dystrybucję zadań według różnych kryteriów
     */
    Map<String, Object> getTaskDistribution();
    
    /**
     * Śledzenie postępu na podstawie historii zmian
     */
    Map<String, Object> getProgressTracking(LocalDate fromDate, LocalDate toDate);
    
    /**
     * Metryki wydajności zespołu
     */
    Map<String, Double> getPerformanceMetrics();
    
    /**
     * Ostatnie aktywności w systemie
     */
    List<Map<String, Object>> getRecentActivity(int limit);
    
    /**
     * Statystyki trendu dla wykresów
     */
    Map<String, Object> getTrendData(LocalDate fromDate, LocalDate toDate);
    
    /**
     * Prognoza zakończenia zadań
     */
    Map<String, Object> getCompletionForecast();
} 