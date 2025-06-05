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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChangeLogService changeLogService;

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
    void createTask_ShouldLogChangeAndSaveTask() {
        // Given
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.createTask(testTask);

        // Then
        assertThat(result).isEqualTo(testTask);
        verify(taskRepository).save(testTask);
        verify(changeLogService).logChange(
            eq(testTask), 
            eq("task"), 
            eq(null), 
            eq("created"), 
            eq(ChangeLogOperationType.CREATE), 
            contains("Task created:")
        );
    }

    @Test
    void createTask_WithNullStatus_ShouldSetDefaultStatus() {
        // Given
        Task taskWithoutStatus = new Task();
        taskWithoutStatus.setTitle("Test Task");
        taskWithoutStatus.setPriority(TaskPriority.HIGH);
        taskWithoutStatus.setStatus(null);

        when(taskRepository.save(any(Task.class))).thenReturn(taskWithoutStatus);

        // When
        Task result = taskService.createTask(taskWithoutStatus);

        // Then
        assertThat(taskWithoutStatus.getStatus()).isEqualTo(TaskStatus.TODO);
        verify(taskRepository).save(taskWithoutStatus);
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
        verify(changeLogService).logTaskChanges(eq(existingTask), any(Task.class), eq(null));
    }

    @Test
    void updateTask_WhenTaskDoesNotExist_ShouldThrowEntityNotFoundException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.updateTask(1L, testTask))
                .isInstanceOf(org.example.exception.EntityNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void deleteTask_WhenTaskExists_ShouldDeleteTaskAndLogChange() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskRepository).findById(1L);
        verify(taskRepository).deleteById(1L);
        verify(changeLogService).logChange(
            eq(testTask), 
            eq("task"), 
            eq(testTask.getTitle()), 
            eq("deleted"), 
            eq(ChangeLogOperationType.DELETE), 
            contains("Task deleted:")
        );
    }

    @Test
    void deleteTask_WhenTaskDoesNotExist_ShouldThrowEntityNotFoundException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.deleteTask(1L))
                .isInstanceOf(org.example.exception.EntityNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void assignTaskToUser_ShouldAssignUserAndLogChange() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.assignTaskToUser(1L, 1L);

        // Then
        assertThat(result.getAssignedTo()).isEqualTo(testUser);
        verify(taskRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(taskRepository).save(testTask);
        verify(changeLogService).logChange(
            eq(testTask), 
            eq("assignedTo"), 
            eq("unassigned"), 
            eq(testUser.getUsername()), 
            eq(ChangeLogOperationType.ASSIGN), 
            eq(testUser)
        );
    }

    @Test
    void assignTaskToUser_WhenTaskNotFound_ShouldThrowEntityNotFoundException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.assignTaskToUser(1L, 1L))
                .isInstanceOf(org.example.exception.EntityNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void assignTaskToUser_WhenUserNotFound_ShouldThrowEntityNotFoundException() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.assignTaskToUser(1L, 1L))
                .isInstanceOf(org.example.exception.EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void unassignTask_ShouldUnassignUserAndLogChange() {
        // Given
        testTask.setAssignedTo(testUser);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.unassignTask(1L);

        // Then
        assertThat(result.getAssignedTo()).isNull();
        verify(changeLogService).logChange(
            eq(testTask), 
            eq("assignedTo"), 
            eq(testUser.getUsername()), 
            eq("unassigned"), 
            eq(ChangeLogOperationType.UNASSIGN)
        );
    }

    @Test
    void changeTaskStatus_ShouldUpdateStatusAndLogChange() {
        // Given
        testTask.setStatus(TaskStatus.TODO);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.changeTaskStatus(1L, TaskStatus.IN_PROGRESS);

        // Then
        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository).save(testTask);
        verify(changeLogService).logChange(
            eq(testTask), 
            eq("status"), 
            eq("To Do"), 
            eq("In Progress"), 
            eq(ChangeLogOperationType.STATUS_CHANGE)
        );
    }

    @Test
    void changeTaskPriority_ShouldUpdatePriorityAndLogChange() {
        // Given
        testTask.setPriority(TaskPriority.LOW);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.changeTaskPriority(1L, TaskPriority.HIGH);

        // Then
        assertThat(result.getPriority()).isEqualTo(TaskPriority.HIGH);
        verify(taskRepository).save(testTask);
        verify(changeLogService).logChange(
            eq(testTask), 
            eq("priority"), 
            eq("Low"), 
            eq("High"), 
            eq(ChangeLogOperationType.PRIORITY_CHANGE)
        );
    }

    @Test
    void getTasksByUser_WithValidUserId_ShouldReturnUserTasks() {
        // Given
        List<Task> expectedTasks = Arrays.asList(testTask);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByAssignedTo_Id(1L)).thenReturn(expectedTasks);

        // When
        List<Task> result = taskService.getTasksByUser(1L);

        // Then
        assertThat(result).isEqualTo(expectedTasks);
        verify(userRepository).existsById(1L);
        verify(taskRepository).findByAssignedTo_Id(1L);
    }

    @Test
    void getTasksByUser_WithInvalidUserId_ShouldThrowEntityNotFoundException() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> taskService.getTasksByUser(1L))
                .isInstanceOf(org.example.exception.EntityNotFoundException.class)
                .hasMessageContaining("User not found");
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
    void getTasksByStatus_WithNullStatus_ShouldThrowIllegalArgumentException() {
        // When & Then
        assertThatThrownBy(() -> taskService.getTasksByStatus(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task status cannot be null");
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

    @Test
    void getTasksByPriority_WithNullPriority_ShouldThrowIllegalArgumentException() {
        // When & Then
        assertThatThrownBy(() -> taskService.getTasksByPriority(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Task priority cannot be null");
    }

    // Tests for new ChangeLogService integration methods
    @Test
    void getTaskChangeHistory_ShouldReturnChangeHistory() {
        // Given
        List<ChangeLogEntry> expectedHistory = Arrays.asList(new ChangeLogEntry());
        when(changeLogService.getTaskHistory(1L)).thenReturn(expectedHistory);

        // When
        List<ChangeLogEntry> result = taskService.getTaskChangeHistory(1L);

        // Then
        assertThat(result).isEqualTo(expectedHistory);
        verify(changeLogService).getTaskHistory(1L);
    }

    @Test
    void getTaskChangeStats_ShouldReturnChangeStats() {
        // Given
        ChangeLogStats expectedStats = new ChangeLogStats();
        when(changeLogService.getTaskChangeStats(1L)).thenReturn(expectedStats);

        // When
        ChangeLogStats result = taskService.getTaskChangeStats(1L);

        // Then
        assertThat(result).isEqualTo(expectedStats);
        verify(changeLogService).getTaskChangeStats(1L);
    }

    @Test
    void exportTaskChangeHistory_ShouldReturnExportedHistory() {
        // Given
        String expectedExport = "exported history";
        when(changeLogService.exportTaskHistoryToJson(1L)).thenReturn(expectedExport);

        // When
        String result = taskService.exportTaskChangeHistory(1L);

        // Then
        assertThat(result).isEqualTo(expectedExport);
        verify(changeLogService).exportTaskHistoryToJson(1L);
    }
} 