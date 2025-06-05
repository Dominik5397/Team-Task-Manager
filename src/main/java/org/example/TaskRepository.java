package org.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Znajduje zadania przypisane do konkretnego użytkownika
     */
    List<Task> findByAssignedTo_Id(Long userId);
    
    /**
     * Znajduje zadania według statusu
     */
    List<Task> findByStatus(TaskStatus status);
    
    /**
     * Znajduje zadania według priorytetu
     */
    List<Task> findByPriority(TaskPriority priority);
    
    /**
     * Znajduje zadania przypisane do użytkownika według statusu
     */
    List<Task> findByAssignedTo_IdAndStatus(Long userId, TaskStatus status);
    
    /**
     * Znajduje zadania przypisane do użytkownika według priorytetu
     */
    List<Task> findByAssignedTo_IdAndPriority(Long userId, TaskPriority priority);
    
    /**
     * Znajduje zadania o wysokim priorytecie
     */
    @Query("SELECT t FROM Task t WHERE t.priority = org.example.TaskPriority.HIGH")
    List<Task> findHighPriorityTasks();
    
    /**
     * Znajduje niezakończone zadania (nie DONE)
     */
    @Query("SELECT t FROM Task t WHERE t.status != org.example.TaskStatus.DONE")
    List<Task> findIncompleteTasks();
    
    /**
     * Znajduje zadania bez przypisanego użytkownika
     */
    List<Task> findByAssignedToIsNull();
    
    /**
     * Znajduje zadania według części tytułu (case insensitive)
     */
    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Task> findByTitleContainingIgnoreCase(@Param("title") String title);

    // ===== ANALYTICS QUERIES =====
    
    /**
     * Zlicza zadania według statusu
     */
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countTasksByStatus();
    
    /**
     * Zlicza zadania według priorytetu
     */
    @Query("SELECT t.priority, COUNT(t) FROM Task t GROUP BY t.priority")
    List<Object[]> countTasksByPriority();
    
    /**
     * Zlicza zadania przypisane do użytkowników
     */
    @Query("SELECT u.username, COUNT(t) FROM Task t JOIN t.assignedTo u GROUP BY u.id, u.username ORDER BY COUNT(t) DESC")
    List<Object[]> countTasksByUser();
    
    /**
     * Zlicza przeterminowane zadania
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate < :currentDate AND t.status != org.example.TaskStatus.DONE")
    Long countOverdueTasks(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Zlicza nieprzypisane zadania
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo IS NULL")
    Long countUnassignedTasks();
    
    /**
     * Zlicza zadania ukończone
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = org.example.TaskStatus.DONE")
    Long countCompletedTasks();
    
    /**
     * Zlicza aktywne zadania (nie ukończone)
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status != org.example.TaskStatus.DONE")
    Long countActiveTasks();
    
    /**
     * Znajduje użytkowników z najwyższą liczbą ukończonych zadań
     */
    @Query("SELECT u.username, COUNT(t) FROM Task t JOIN t.assignedTo u WHERE t.status = org.example.TaskStatus.DONE GROUP BY u.id, u.username ORDER BY COUNT(t) DESC")
    List<Object[]> findTopPerformersByCompletedTasks();
    
    /**
     * Znajduje zadania według zakresu dat utworzenia/modyfikacji
     */
    @Query("SELECT t FROM Task t WHERE t.id IN (SELECT DISTINCT cle.task.id FROM ChangeLogEntry cle WHERE cle.changedAt >= :fromDate AND cle.changedAt <= :toDate)")
    List<Task> findTasksModifiedBetween(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
    
    /**
     * Statystyki zadań według użytkownika
     */
    @Query("SELECT u.username, " +
           "COUNT(t), " +
           "SUM(CASE WHEN t.status = org.example.TaskStatus.DONE THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN t.status = org.example.TaskStatus.IN_PROGRESS THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN t.status = org.example.TaskStatus.TODO THEN 1 ELSE 0 END) " +
           "FROM Task t JOIN t.assignedTo u GROUP BY u.id, u.username")
    List<Object[]> getUserTaskStatistics();
    
    /**
     * Dystrybucja zadań według kombinacji statusu i priorytetu
     */
    @Query("SELECT CONCAT(t.status, '-', t.priority), COUNT(t) FROM Task t GROUP BY t.status, t.priority")
    List<Object[]> getTaskDistributionByStatusAndPriority();
} 