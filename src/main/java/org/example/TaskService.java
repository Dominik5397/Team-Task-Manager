package org.example;

import java.util.List;
import java.util.Optional;

/**
 * Interface dla serwisu obsługującego logikę biznesową związaną z zadaniami.
 * Definiuje operacje CRUD oraz dodatkowe funkcjonalności biznesowe.
 */
public interface TaskService {
    
    /**
     * Pobiera wszystkie zadania
     */
    List<Task> getAllTasks();
    
    /**
     * Pobiera zadanie po ID
     */
    Optional<Task> getTaskById(Long id);
    
    /**
     * Tworzy nowe zadanie z automatycznym logiem zmian
     */
    Task createTask(Task task);
    
    /**
     * Aktualizuje istniejące zadanie z logiem zmian
     */
    Task updateTask(Long id, Task task);
    
    /**
     * Usuwa zadanie po ID
     */
    void deleteTask(Long id);
    
    /**
     * Pobiera zadania przypisane do konkretnego użytkownika
     */
    List<Task> getTasksByUser(Long userId);
    
    /**
     * Pobiera zadania według statusu
     */
    List<Task> getTasksByStatus(TaskStatus status);
    
    /**
     * Pobiera zadania według priorytetu
     */
    List<Task> getTasksByPriority(TaskPriority priority);
    
    /**
     * Przypisuje zadanie do użytkownika
     */
    Task assignTaskToUser(Long taskId, Long userId);
    
    /**
     * Usuwa przypisanie zadania od użytkownika
     */
    Task unassignTask(Long taskId);
    
    /**
     * Zmienia status zadania z logiem
     */
    Task changeTaskStatus(Long taskId, TaskStatus newStatus);
    
    /**
     * Zmienia priorytet zadania z logiem
     */
    Task changeTaskPriority(Long taskId, TaskPriority newPriority);
} 