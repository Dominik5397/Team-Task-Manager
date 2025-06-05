package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementacja serwisu obsługującego logikę biznesową dla użytkowników.
 * Zawiera operacje CRUD oraz zaawansowane funkcjonalności biznesowe.
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public User createUser(User user) {
        // Walidacja unikalności email i username
        validateUserUniqueness(user);
        
        return userRepository.save(user);
    }
    
    @Override
    public User updateUser(Long id, User user) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isEmpty()) {
            throw new RuntimeException("User not found with id: " + id);
        }
        
        User existingUser = existingUserOpt.get();
        user.setId(id);
        
        // Walidacja unikalności tylko jeśli dane się zmieniły
        validateUserUniquenessForUpdate(user, existingUser);
        
        return userRepository.save(user);
    }
    
    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        
        // Sprawdzenie czy użytkownik ma przypisane zadania
        List<Task> userTasks = taskRepository.findByAssignedTo_Id(id);
        if (!userTasks.isEmpty()) {
            // Opcjonalnie można usunąć przypisania zadań zamiast rzucania błędu
            userTasks.forEach(task -> {
                task.setAssignedTo(null);
                taskRepository.save(task);
            });
        }
        
        userRepository.deleteById(id);
    }
    
    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    public List<User> getUsersWithTasks() {
        return userRepository.findUsersWithTasks();
    }
    
    @Override
    public List<User> getUsersWithoutTasks() {
        return userRepository.findUsersWithoutTasks();
    }
    
    @Override
    public void seedUsers() {
        if (userRepository.count() == 0) {
            User user1 = new User();
            user1.setUsername("Jan Kowalski");
            user1.setEmail("jan.kowalski@example.com");
            user1.setAvatarUrl("https://randomuser.me/api/portraits/men/1.jpg");
            userRepository.save(user1);
            
            User user2 = new User();
            user2.setUsername("Anna Nowak");
            user2.setEmail("anna.nowak@example.com");
            user2.setAvatarUrl("https://randomuser.me/api/portraits/women/2.jpg");
            userRepository.save(user2);
            
            User user3 = new User();
            user3.setUsername("Piotr Wiśniewski");
            user3.setEmail("piotr.wisniewski@example.com");
            user3.setAvatarUrl("https://randomuser.me/api/portraits/men/3.jpg");
            userRepository.save(user3);
        }
    }
    
    @Override
    public UserStats getUserStats(Long userId) {
        User user = getUserById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        UserStats stats = new UserStats(userId, user.getUsername());
        
        // Pobieranie zadań użytkownika
        List<Task> userTasks = taskRepository.findByAssignedTo_Id(userId);
        stats.setTotalTasks(userTasks.size());
        
        // Liczenie zadań według statusu
        int completedTasks = 0;
        int inProgressTasks = 0;
        int todoTasks = 0;
        
        // Liczenie zadań według priorytetu
        int highPriorityTasks = 0;
        int mediumPriorityTasks = 0;
        int lowPriorityTasks = 0;
        
        // Liczenie przeterminowanych zadań
        int overdueTasks = 0;
        LocalDate today = LocalDate.now();
        
        for (Task task : userTasks) {
            // Statystyki statusu
            switch (task.getStatus()) {
                case DONE:
                    completedTasks++;
                    break;
                case IN_PROGRESS:
                    inProgressTasks++;
                    break;
                case TODO:
                    todoTasks++;
                    break;
            }
            
            // Statystyki priorytetu
            switch (task.getPriority()) {
                case HIGH:
                    highPriorityTasks++;
                    break;
                case MEDIUM:
                    mediumPriorityTasks++;
                    break;
                case LOW:
                    lowPriorityTasks++;
                    break;
            }
            
            // Sprawdzenie czy zadanie jest przeterminowane
            if (task.getDueDate() != null && 
                task.getDueDate().isBefore(today) && 
                task.getStatus() != TaskStatus.DONE) {
                overdueTasks++;
            }
        }
        
        stats.setCompletedTasks(completedTasks);
        stats.setInProgressTasks(inProgressTasks);
        stats.setTodoTasks(todoTasks);
        stats.setHighPriorityTasks(highPriorityTasks);
        stats.setMediumPriorityTasks(mediumPriorityTasks);
        stats.setLowPriorityTasks(lowPriorityTasks);
        stats.setOverdueTasks(overdueTasks);
        
        return stats;
    }
    
    /**
     * Waliduje unikalność email i username dla nowego użytkownika
     */
    private void validateUserUniqueness(User user) {
        if (emailExists(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        if (usernameExists(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
    }
    
    /**
     * Waliduje unikalność email i username dla aktualizacji użytkownika
     */
    private void validateUserUniquenessForUpdate(User newUser, User existingUser) {
        // Sprawdzenie email tylko jeśli się zmienił
        if (!newUser.getEmail().equals(existingUser.getEmail()) && emailExists(newUser.getEmail())) {
            throw new RuntimeException("Email already exists: " + newUser.getEmail());
        }
        
        // Sprawdzenie username tylko jeśli się zmienił
        if (!newUser.getUsername().equals(existingUser.getUsername()) && usernameExists(newUser.getUsername())) {
            throw new RuntimeException("Username already exists: " + newUser.getUsername());
        }
    }
} 