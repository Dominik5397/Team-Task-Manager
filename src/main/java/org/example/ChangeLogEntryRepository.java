package org.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository do zarządzania historią zmian zadań.
 * Umożliwia zaawansowane przeszukiwanie i filtrowanie wpisów w historii.
 */
public interface ChangeLogEntryRepository extends JpaRepository<ChangeLogEntry, Long> {
    
    /**
     * Pobiera wszystkie wpisy historii dla konkretnego zadania, posortowane chronologicznie (najnowsze pierwsze)
     */
    List<ChangeLogEntry> findByTaskIdOrderByChangedAtDesc(Long taskId);
    
    /**
     * Pobiera wszystkie wpisy historii dla konkretnego zadania w określonym zakresie dat
     */
    List<ChangeLogEntry> findByTaskIdAndChangedAtBetweenOrderByChangedAtDesc(
        Long taskId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Pobiera wpisy historii według typu operacji
     */
    List<ChangeLogEntry> findByOperationTypeOrderByChangedAtDesc(ChangeLogOperationType operationType);
    
    /**
     * Pobiera wpisy historii dla zadań przypisanych do konkretnego użytkownika
     */
    @Query("SELECT cle FROM ChangeLogEntry cle WHERE cle.task.assignedTo.id = :userId ORDER BY cle.changedAt DESC")
    List<ChangeLogEntry> findByTaskAssignedToUserOrderByChangedAtDesc(@Param("userId") Long userId);
    
    /**
     * Pobiera wpisy historii zmian wykonanych przez konkretnego użytkownika
     */
    List<ChangeLogEntry> findByChangedByIdOrderByChangedAtDesc(Long userId);
    
    /**
     * Pobiera wpisy historii dla konkretnego pola zadania
     */
    List<ChangeLogEntry> findByFieldNameOrderByChangedAtDesc(String fieldName);
    
    /**
     * Pobiera wpisy historii dla konkretnego zadania i pola
     */
    List<ChangeLogEntry> findByTaskIdAndFieldNameOrderByChangedAtDesc(Long taskId, String fieldName);
    
    /**
     * Pobiera ostatnie N zmian dla konkretnego zadania
     */
    @Query("SELECT cle FROM ChangeLogEntry cle WHERE cle.task.id = :taskId ORDER BY cle.changedAt DESC LIMIT :limit")
    List<ChangeLogEntry> findTopNByTaskIdOrderByChangedAtDesc(@Param("taskId") Long taskId, @Param("limit") int limit);
    
    /**
     * Pobiera ostatnie N zmian w całym systemie (dla analytics)
     */
    @Query("SELECT cle FROM ChangeLogEntry cle ORDER BY cle.changedAt DESC LIMIT :limit")
    List<ChangeLogEntry> findTopNByOrderByChangedAtDesc(@Param("limit") int limit);
    
    /**
     * Pobiera wszystkie zmiany w określonym zakresie dat
     */
    List<ChangeLogEntry> findByChangedAtBetweenOrderByChangedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Pobiera zmiany według typu operacji i zakresu dat
     */
    List<ChangeLogEntry> findByOperationTypeAndChangedAtBetweenOrderByChangedAtDesc(
        ChangeLogOperationType operationType, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Pobiera zmiany dla zadań o konkretnym statusie
     */
    @Query("SELECT cle FROM ChangeLogEntry cle WHERE cle.task.status = :status ORDER BY cle.changedAt DESC")
    List<ChangeLogEntry> findByTaskStatusOrderByChangedAtDesc(@Param("status") TaskStatus status);
    
    /**
     * Pobiera zmiany dla zadań o konkretnym priorytecie
     */
    @Query("SELECT cle FROM ChangeLogEntry cle WHERE cle.task.priority = :priority ORDER BY cle.changedAt DESC")
    List<ChangeLogEntry> findByTaskPriorityOrderByChangedAtDesc(@Param("priority") TaskPriority priority);
    
    /**
     * Pobiera liczbę zmian dla konkretnego zadania
     */
    long countByTaskId(Long taskId);
    
    /**
     * Pobiera liczbę zmian wykonanych przez konkretnego użytkownika
     */
    long countByChangedById(Long userId);
    
    /**
     * Pobiera liczbę zmian według typu operacji
     */
    long countByOperationType(ChangeLogOperationType operationType);
    
    /**
     * Znajdź zmiany zawierające konkretny tekst w opisie
     */
    @Query("SELECT cle FROM ChangeLogEntry cle WHERE LOWER(cle.description) LIKE LOWER(CONCAT('%', :searchText, '%')) ORDER BY cle.changedAt DESC")
    List<ChangeLogEntry> findByDescriptionContainingIgnoreCaseOrderByChangedAtDesc(@Param("searchText") String searchText);
    
    /**
     * Pobiera zmiany dla konkretnego zadania według typu operacji
     */
    List<ChangeLogEntry> findByTaskIdAndOperationTypeOrderByChangedAtDesc(Long taskId, ChangeLogOperationType operationType);
    
    /**
     * Pobiera ostatnie zmiany w systemie (dla dashboard)
     */
    @Query("SELECT cle FROM ChangeLogEntry cle ORDER BY cle.changedAt DESC LIMIT :limit")
    List<ChangeLogEntry> findRecentChanges(@Param("limit") int limit);
    
    /**
     * Pobiera statystyki zmian według typu operacji
     */
    @Query("SELECT cle.operationType, COUNT(cle) FROM ChangeLogEntry cle GROUP BY cle.operationType")
    List<Object[]> getChangeStatsByOperationType();
    
    /**
     * Pobiera statystyki zmian według użytkownika (kto wykonał najwięcej zmian)
     */
    @Query("SELECT cle.changedBy, COUNT(cle) FROM ChangeLogEntry cle WHERE cle.changedBy IS NOT NULL GROUP BY cle.changedBy ORDER BY COUNT(cle) DESC")
    List<Object[]> getChangeStatsByUser();
} 