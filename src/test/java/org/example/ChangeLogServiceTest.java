package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy dla serwisu szczegółowego śledzenia zmian zadań.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChangeLogServiceTest {

    @Autowired
    private ChangeLogService changeLogService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private Task testTask;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Tworzenie użytkownika testowego
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser = userRepository.save(testUser);

        // Tworzenie zadania testowego
        testTask = new Task();
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(TaskPriority.MEDIUM);
        testTask.setDueDate(LocalDate.now().plusDays(7));
        testTask = taskRepository.save(testTask);
    }

    @Test
    void shouldLogSingleChange() {
        // Given
        String fieldName = "title";
        String oldValue = "Old Title";
        String newValue = "New Title";
        ChangeLogOperationType operationType = ChangeLogOperationType.TITLE_CHANGE;

        // When
        ChangeLogEntry entry = changeLogService.logChange(testTask, fieldName, oldValue, newValue, operationType);

        // Then
        assertNotNull(entry);
        assertNotNull(entry.getId());
        assertEquals(testTask.getId(), entry.getTask().getId());
        assertEquals(fieldName, entry.getFieldName());
        assertEquals(oldValue, entry.getOldValue());
        assertEquals(newValue, entry.getNewValue());
        assertEquals(operationType, entry.getOperationType());
        assertNotNull(entry.getChangedAt());
    }

    @Test
    void shouldLogChangeWithUser() {
        // Given
        String fieldName = "status";
        String oldValue = "TODO";
        String newValue = "IN_PROGRESS";
        ChangeLogOperationType operationType = ChangeLogOperationType.STATUS_CHANGE;

        // When
        ChangeLogEntry entry = changeLogService.logChange(testTask, fieldName, oldValue, newValue, operationType, testUser);

        // Then
        assertNotNull(entry);
        assertEquals(testUser.getId(), entry.getChangedBy().getId());
        assertEquals(testUser.getUsername(), entry.getChangedBy().getUsername());
    }

    @Test
    void shouldLogChangeWithDescription() {
        // Given
        String fieldName = "priority";
        String oldValue = "MEDIUM";
        String newValue = "HIGH";
        ChangeLogOperationType operationType = ChangeLogOperationType.PRIORITY_CHANGE;
        String description = "Priority increased due to urgency";

        // When
        ChangeLogEntry entry = changeLogService.logChange(testTask, fieldName, oldValue, newValue, operationType, description);

        // Then
        assertNotNull(entry);
        assertEquals(description, entry.getDescription());
    }

    @Test
    void shouldLogTaskCreation() {
        // Given
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("New Description");
        newTask.setStatus(TaskStatus.TODO);
        newTask.setPriority(TaskPriority.LOW);
        newTask = taskRepository.save(newTask);

        // When
        List<ChangeLogEntry> changes = changeLogService.logTaskChanges(null, newTask, testUser);

        // Then
        assertNotNull(changes);
        assertEquals(1, changes.size());
        
        ChangeLogEntry createEntry = changes.get(0);
        assertEquals(ChangeLogOperationType.CREATE, createEntry.getOperationType());
        assertEquals("task", createEntry.getFieldName());
        assertNull(createEntry.getOldValue());
        assertEquals("created", createEntry.getNewValue());
        assertEquals(testUser.getId(), createEntry.getChangedBy().getId());
        assertTrue(createEntry.getDescription().contains("Task created: New Task"));
    }

    @Test
    void shouldLogMultipleFieldChanges() {
        // Given
        Task oldTask = createTaskCopy(testTask);
        
        testTask.setTitle("Updated Title");
        testTask.setDescription("Updated Description");
        testTask.setStatus(TaskStatus.IN_PROGRESS);
        testTask.setPriority(TaskPriority.HIGH);
        testTask.setDueDate(LocalDate.now().plusDays(14));

        // When
        List<ChangeLogEntry> changes = changeLogService.logTaskChanges(oldTask, testTask, testUser);

        // Then
        assertNotNull(changes);
        assertEquals(5, changes.size()); // title, description, status, priority, dueDate

        // Sprawdzenie czy wszystkie zmiany zostały zarejestrowane
        List<String> changedFields = changes.stream()
                .map(ChangeLogEntry::getFieldName)
                .toList();
        
        assertTrue(changedFields.contains("title"));
        assertTrue(changedFields.contains("description"));
        assertTrue(changedFields.contains("status"));
        assertTrue(changedFields.contains("priority"));
        assertTrue(changedFields.contains("dueDate"));
    }

    @Test
    void shouldLogAssignmentChange() {
        // Given
        Task oldTask = createTaskCopy(testTask);
        testTask.setAssignedTo(testUser);

        // When
        List<ChangeLogEntry> changes = changeLogService.logTaskChanges(oldTask, testTask, testUser);

        // Then
        assertNotNull(changes);
        assertEquals(1, changes.size());
        
        ChangeLogEntry assignEntry = changes.get(0);
        assertEquals(ChangeLogOperationType.ASSIGN, assignEntry.getOperationType());
        assertEquals("assignedTo", assignEntry.getFieldName());
        assertEquals("unassigned", assignEntry.getOldValue());
        assertEquals(testUser.getUsername(), assignEntry.getNewValue());
    }

    @Test
    void shouldGetTaskHistory() {
        // Given
        changeLogService.logChange(testTask, "title", "Old", "New", ChangeLogOperationType.TITLE_CHANGE);
        changeLogService.logChange(testTask, "status", "TODO", "IN_PROGRESS", ChangeLogOperationType.STATUS_CHANGE);
        changeLogService.logChange(testTask, "priority", "MEDIUM", "HIGH", ChangeLogOperationType.PRIORITY_CHANGE);

        // When
        List<ChangeLogEntry> history = changeLogService.getTaskHistory(testTask.getId());

        // Then
        assertNotNull(history);
        assertEquals(3, history.size());
        
        // Sprawdzenie czy historia jest posortowana chronologicznie (najnowsze pierwsze)
        LocalDateTime previousTime = LocalDateTime.now().plusHours(1); // Przyszły czas do porównania
        for (ChangeLogEntry entry : history) {
            assertTrue(entry.getChangedAt().isBefore(previousTime));
            previousTime = entry.getChangedAt();
        }
    }

    @Test
    void shouldGetRecentTaskChanges() {
        // Given
        for (int i = 0; i < 15; i++) {
            changeLogService.logChange(testTask, "title", "Old" + i, "New" + i, ChangeLogOperationType.TITLE_CHANGE);
        }

        // When
        List<ChangeLogEntry> recentChanges = changeLogService.getRecentTaskChanges(testTask.getId(), 10);

        // Then
        assertNotNull(recentChanges);
        assertEquals(10, recentChanges.size());
    }

    @Test
    void shouldGetChangesByOperationType() {
        // Given
        changeLogService.logChange(testTask, "title", "Old", "New", ChangeLogOperationType.TITLE_CHANGE);
        changeLogService.logChange(testTask, "status", "TODO", "IN_PROGRESS", ChangeLogOperationType.STATUS_CHANGE);
        changeLogService.logChange(testTask, "status", "IN_PROGRESS", "DONE", ChangeLogOperationType.STATUS_CHANGE);

        // When
        List<ChangeLogEntry> statusChanges = changeLogService.getChangesByOperationType(ChangeLogOperationType.STATUS_CHANGE);

        // Then
        assertNotNull(statusChanges);
        assertEquals(2, statusChanges.size());
        assertTrue(statusChanges.stream().allMatch(entry -> entry.getOperationType() == ChangeLogOperationType.STATUS_CHANGE));
    }

    @Test
    void shouldGetChangesByUser() {
        // Given
        changeLogService.logChange(testTask, "title", "Old", "New", ChangeLogOperationType.TITLE_CHANGE, testUser);
        changeLogService.logChange(testTask, "status", "TODO", "IN_PROGRESS", ChangeLogOperationType.STATUS_CHANGE, testUser);

        // When
        List<ChangeLogEntry> userChanges = changeLogService.getChangesByUser(testUser.getId());

        // Then
        assertNotNull(userChanges);
        assertEquals(2, userChanges.size());
        assertTrue(userChanges.stream().allMatch(entry -> 
            entry.getChangedBy() != null && entry.getChangedBy().getId().equals(testUser.getId())));
    }

    @Test
    void shouldGetTaskChangeStats() {
        // Given
        changeLogService.logChange(testTask, "title", "Old", "New", ChangeLogOperationType.TITLE_CHANGE);
        changeLogService.logChange(testTask, "status", "TODO", "IN_PROGRESS", ChangeLogOperationType.STATUS_CHANGE);
        changeLogService.logChange(testTask, "status", "IN_PROGRESS", "DONE", ChangeLogOperationType.STATUS_CHANGE);

        // When
        ChangeLogStats stats = changeLogService.getTaskChangeStats(testTask.getId());

        // Then
        assertNotNull(stats);
        assertEquals(testTask.getId(), stats.getEntityId());
        assertEquals("task", stats.getEntityType());
        assertEquals(3, stats.getTotalChanges());
        assertNotNull(stats.getFirstChange());
        assertNotNull(stats.getLastChange());
        assertEquals(2, stats.getChangesByType().get(ChangeLogOperationType.STATUS_CHANGE));
        assertEquals(1, stats.getChangesByType().get(ChangeLogOperationType.TITLE_CHANGE));
    }

    @Test
    void shouldExportTaskHistoryToJson() {
        // Given
        changeLogService.logChange(testTask, "title", "Old", "New", ChangeLogOperationType.TITLE_CHANGE);
        changeLogService.logChange(testTask, "status", "TODO", "IN_PROGRESS", ChangeLogOperationType.STATUS_CHANGE);

        // When
        String jsonHistory = changeLogService.exportTaskHistoryToJson(testTask.getId());

        // Then
        assertNotNull(jsonHistory);
        assertFalse(jsonHistory.isEmpty());
        assertTrue(jsonHistory.contains("\"fieldName\" : \"title\""));
        assertTrue(jsonHistory.contains("\"fieldName\" : \"status\""));
        assertTrue(jsonHistory.contains("\"operationType\" : \"TITLE_CHANGE\""));
        assertTrue(jsonHistory.contains("\"operationType\" : \"STATUS_CHANGE\""));
    }

    @Test
    void shouldHandleTaskHistoryDateRange() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusHours(2);
        changeLogService.logChange(testTask, "title", "Old", "New", ChangeLogOperationType.TITLE_CHANGE);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);

        // When
        List<ChangeLogEntry> history = changeLogService.getTaskHistory(testTask.getId(), startTime, endTime);

        // Then
        assertNotNull(history);
        assertEquals(1, history.size());
        assertTrue(history.get(0).getChangedAt().isAfter(startTime));
        assertTrue(history.get(0).getChangedAt().isBefore(endTime));
    }

    @Test
    void shouldDetectNoChanges() {
        // Given
        Task oldTask = createTaskCopy(testTask);
        Task newTask = createTaskCopy(testTask);

        // When
        List<ChangeLogEntry> changes = changeLogService.logTaskChanges(oldTask, newTask, testUser);

        // Then
        assertNotNull(changes);
        assertTrue(changes.isEmpty());
    }

    /**
     * Pomocnicza metoda do tworzenia kopii zadania
     */
    private Task createTaskCopy(Task original) {
        Task copy = new Task();
        copy.setId(original.getId());
        copy.setTitle(original.getTitle());
        copy.setDescription(original.getDescription());
        copy.setStatus(original.getStatus());
        copy.setPriority(original.getPriority());
        copy.setDueDate(original.getDueDate());
        copy.setAssignedTo(original.getAssignedTo());
        return copy;
    }
} 