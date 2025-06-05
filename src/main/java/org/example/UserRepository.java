package org.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Znajduje użytkownika po adresie email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Znajduje użytkownika po nazwie użytkownika
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Sprawdza czy email już istnieje
     */
    boolean existsByEmail(String email);
    
    /**
     * Sprawdza czy username już istnieje
     */
    boolean existsByUsername(String username);
    
    /**
     * Znajduje użytkowników, którzy mają przypisane zadania
     */
    @Query("SELECT DISTINCT u FROM User u WHERE u.tasks IS NOT EMPTY")
    List<User> findUsersWithTasks();
    
    /**
     * Znajduje użytkowników bez przypisanych zadań
     */
    @Query("SELECT u FROM User u WHERE u.tasks IS EMPTY")
    List<User> findUsersWithoutTasks();
    
    /**
     * Znajduje użytkowników według części nazwy użytkownika (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    List<User> findByUsernameContainingIgnoreCase(@Param("username") String username);
    
    /**
     * Znajduje użytkowników według części emaila (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<User> findByEmailContainingIgnoreCase(@Param("email") String email);
} 