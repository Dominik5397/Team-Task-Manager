package org.example;

import java.util.List;
import java.util.Optional;

/**
 * Interface dla serwisu obsługującego logikę biznesową związaną z użytkownikami.
 * Definiuje operacje CRUD oraz dodatkowe funkcjonalności biznesowe.
 */
public interface UserService {
    
    /**
     * Pobiera wszystkich użytkowników
     */
    List<User> getAllUsers();
    
    /**
     * Pobiera użytkownika po ID
     */
    Optional<User> getUserById(Long id);
    
    /**
     * Tworzy nowego użytkownika z walidacją
     */
    User createUser(User user);
    
    /**
     * Aktualizuje istniejącego użytkownika
     */
    User updateUser(Long id, User user);
    
    /**
     * Usuwa użytkownika po ID
     */
    void deleteUser(Long id);
    
    /**
     * Znajdź użytkownika po email
     */
    Optional<User> getUserByEmail(String email);
    
    /**
     * Znajdź użytkownika po username
     */
    Optional<User> getUserByUsername(String username);
    
    /**
     * Sprawdza czy email już istnieje
     */
    boolean emailExists(String email);
    
    /**
     * Sprawdza czy username już istnieje
     */
    boolean usernameExists(String username);
    
    /**
     * Pobiera użytkowników z zadaniami
     */
    List<User> getUsersWithTasks();
    
    /**
     * Pobiera użytkowników bez zadań
     */
    List<User> getUsersWithoutTasks();
    
    /**
     * Inicjalizuje dane testowe użytkowników
     */
    void seedUsers();
    
    /**
     * Pobiera statystyki użytkownika (liczba zadań, ukończone, etc.)
     */
    UserStats getUserStats(Long userId);
} 