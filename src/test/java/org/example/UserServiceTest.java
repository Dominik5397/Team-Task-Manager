package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("Test User");
        testUser.setEmail("test@example.com");
        testUser.setAvatarUrl("https://example.com/avatar.jpg");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(TaskPriority.HIGH);
        testTask.setDueDate(LocalDate.now().plusDays(7));
        testTask.setAssignedTo(testUser);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        List<User> expectedUsers = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // When
        List<User> actualUsers = userService.getAllUsers();

        // Then
        assertThat(actualUsers).isEqualTo(expectedUsers);
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findById(1L);
    }

    @Test
    void createUser_WhenValidUser_ShouldCreateUser() {
        // Given
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser(testUser);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).existsByEmail(testUser.getEmail());
        verify(userRepository).existsByUsername(testUser.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void createUser_WhenUsernameExists_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateUser() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("Old Username");
        existingUser.setEmail("old@example.com");

        User updatedUser = new User();
        updatedUser.setUsername("New Username");
        updatedUser.setEmail("new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("New Username")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        User result = userService.updateUser(1L, updatedUser);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldThrowException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(1L, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with id: 1");
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByAssignedTo_Id(1L)).thenReturn(Arrays.asList());

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_WhenUserHasTasks_ShouldUnassignTasksAndDeleteUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByAssignedTo_Id(1L)).thenReturn(Arrays.asList(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        userService.deleteUser(1L);

        // Then
        verify(taskRepository).save(testTask);
        assertThat(testTask.getAssignedTo()).isNull();
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldThrowException() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with id: 1");
    }

    @Test
    void getUserByEmail_ShouldReturnUser() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserByEmail("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getUserByUsername_ShouldReturnUser() {
        // Given
        when(userRepository.findByUsername("Test User")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserByUsername("Test User");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
        verify(userRepository).findByUsername("Test User");
    }

    @Test
    void emailExists_ShouldReturnTrue() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean result = userService.emailExists("test@example.com");

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void usernameExists_ShouldReturnTrue() {
        // Given
        when(userRepository.existsByUsername("Test User")).thenReturn(true);

        // When
        boolean result = userService.usernameExists("Test User");

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByUsername("Test User");
    }

    @Test
    void getUserStats_ShouldCalculateCorrectStats() {
        // Given
        Task completedTask = new Task();
        completedTask.setStatus(TaskStatus.DONE);
        completedTask.setPriority(TaskPriority.HIGH);

        Task inProgressTask = new Task();
        inProgressTask.setStatus(TaskStatus.IN_PROGRESS);
        inProgressTask.setPriority(TaskPriority.MEDIUM);

        Task overdueTask = new Task();
        overdueTask.setStatus(TaskStatus.TODO);
        overdueTask.setPriority(TaskPriority.LOW);
        overdueTask.setDueDate(LocalDate.now().minusDays(1));

        List<Task> userTasks = Arrays.asList(completedTask, inProgressTask, overdueTask);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(taskRepository.findByAssignedTo_Id(1L)).thenReturn(userTasks);

        // When
        UserStats result = userService.getUserStats(1L);

        // Then
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("Test User");
        assertThat(result.getTotalTasks()).isEqualTo(3);
        assertThat(result.getCompletedTasks()).isEqualTo(1);
        assertThat(result.getInProgressTasks()).isEqualTo(1);
        assertThat(result.getTodoTasks()).isEqualTo(1);
        assertThat(result.getHighPriorityTasks()).isEqualTo(1);
        assertThat(result.getMediumPriorityTasks()).isEqualTo(1);
        assertThat(result.getLowPriorityTasks()).isEqualTo(1);
        assertThat(result.getOverdueTasks()).isEqualTo(1);
        assertThat(result.getCompletionRate()).isEqualTo(33.3, within(0.1));
    }

    @Test
    void seedUsers_WhenNoUsersExist_ShouldCreateUsers() {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.seedUsers();

        // Then
        verify(userRepository).count();
        verify(userRepository, times(3)).save(any(User.class));
    }

    @Test
    void seedUsers_WhenUsersExist_ShouldNotCreateUsers() {
        // Given
        when(userRepository.count()).thenReturn(2L);

        // When
        userService.seedUsers();

        // Then
        verify(userRepository).count();
        verify(userRepository, never()).save(any(User.class));
    }
} 