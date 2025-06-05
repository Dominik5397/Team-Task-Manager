package org.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
} 