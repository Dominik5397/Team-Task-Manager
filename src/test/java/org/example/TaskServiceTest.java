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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task testTask;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("Test User");
        testUser.setEmail("test@example.com");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(TaskPriority.HIGH);
        testTask.setDueDate(LocalDate.now().plusDays(7));
    }

    @Test
    void getAllTasks_ShouldReturnAllTasks() {
        // Given
        List<Task> expectedTasks = Arrays.asList(testTask);
        when(taskRepository.findAll()).thenReturn(expectedTasks);

        // When
        List<Task> actualTasks = taskService.getAllTasks();

        // Then
        assertThat(actualTasks).isEqualTo(expectedTasks);
        verify(taskRepository).findAll();
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTask() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When
        Optional<Task> result = taskService.getTaskById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testTask);
        verify(taskRepository).findById(1L);
    }

    @Test
    void getTaskById_WhenTaskDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<Task> result = taskService.getTaskById(1L);

        // Then
        assertThat(result).isEmpty();
        verify(taskRepository).findById(1L);
    }

    @Test
    void createTask_ShouldAddChangeLogAndSaveTask() {
        // Given
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.createTask(testTask);

        // Then
        assertThat(result).isEqualTo(testTask);
        assertThat(testTask.getChangeLog()).isNotNull();
        assertThat(testTask.getChangeLog()).contains("created");
        verify(taskRepository).save(testTask);
    }

    @Test
    void updateTask_WhenTaskExists_ShouldUpdateWithChangeLog() {
        // Given
        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Old Title");
        existingTask.setStatus(TaskStatus.TODO);
        existingTask.setPriority(TaskPriority.LOW);

        Task updatedTask = new Task();
        updatedTask.setTitle("New Title");
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        updatedTask.setPriority(TaskPriority.HIGH);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // When
        Task result = taskService.updateTask(1L, updatedTask);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_WhenTaskDoesNotExist_ShouldThrowException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.updateTask(1L, testTask))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task not found with id: 1");
    }

    @Test
    void deleteTask_WhenTaskExists_ShouldDeleteTask() {
        // Given
        when(taskRepository.existsById(1L)).thenReturn(true);

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskRepository).existsById(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void deleteTask_WhenTaskDoesNotExist_ShouldThrowException() {
        // Given
        when(taskRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> taskService.deleteTask(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task not found with id: 1");
    }

    @Test
    void assignTaskToUser_ShouldAssignUserAndAddChangeLog() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.assignTaskToUser(1L, 1L);

        // Then
        assertThat(result.getAssignedTo()).isEqualTo(testUser);
        assertThat(result.getChangeLog()).contains("assigned");
        verify(taskRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(taskRepository).save(testTask);
    }

    @Test
    void assignTaskToUser_WhenTaskNotFound_ShouldThrowException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.assignTaskToUser(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task not found with id: 1");
    }

    @Test
    void assignTaskToUser_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.assignTaskToUser(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with id: 1");
    }

    @Test
    void changeTaskStatus_ShouldUpdateStatusAndAddChangeLog() {
        // Given
        testTask.setStatus(TaskStatus.TODO);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.changeTaskStatus(1L, TaskStatus.IN_PROGRESS);

        // Then
        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(result.getChangeLog()).contains("status_changed");
        verify(taskRepository).save(testTask);
    }

    @Test
    void changeTaskPriority_ShouldUpdatePriorityAndAddChangeLog() {
        // Given
        testTask.setPriority(TaskPriority.LOW);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.changeTaskPriority(1L, TaskPriority.HIGH);

        // Then
        assertThat(result.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(result.getChangeLog()).contains("priority_changed");
        verify(taskRepository).save(testTask);
    }

    @Test
    void getTasksByUser_ShouldReturnUserTasks() {
        // Given
        List<Task> expectedTasks = Arrays.asList(testTask);
        when(taskRepository.findByAssignedTo_Id(1L)).thenReturn(expectedTasks);

        // When
        List<Task> result = taskService.getTasksByUser(1L);

        // Then
        assertThat(result).isEqualTo(expectedTasks);
        verify(taskRepository).findByAssignedTo_Id(1L);
    }

    @Test
    void getTasksByStatus_ShouldReturnTasksWithGivenStatus() {
        // Given
        List<Task> expectedTasks = Arrays.asList(testTask);
        when(taskRepository.findByStatus(TaskStatus.TODO)).thenReturn(expectedTasks);

        // When
        List<Task> result = taskService.getTasksByStatus(TaskStatus.TODO);

        // Then
        assertThat(result).isEqualTo(expectedTasks);
        verify(taskRepository).findByStatus(TaskStatus.TODO);
    }

    @Test
    void getTasksByPriority_ShouldReturnTasksWithGivenPriority() {
        // Given
        List<Task> expectedTasks = Arrays.asList(testTask);
        when(taskRepository.findByPriority(TaskPriority.HIGH)).thenReturn(expectedTasks);

        // When
        List<Task> result = taskService.getTasksByPriority(TaskPriority.HIGH);

        // Then
        assertThat(result).isEqualTo(expectedTasks);
        verify(taskRepository).findByPriority(TaskPriority.HIGH);
    }
} 