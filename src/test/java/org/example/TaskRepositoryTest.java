package org.example;

import org.example.Task;
import org.example.TaskRepository;
import org.example.TaskStatus;
import org.example.TaskPriority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class TaskRepositoryTest {
    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testCreateAndFindTask() {
        Task task = new Task();
        task.setTitle("Test task");
        task.setDescription("Opis testowego zadania");
        task.setDueDate(LocalDate.now().plusDays(7));
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        Task saved = taskRepository.save(task);

        List<Task> all = taskRepository.findAll();
        assertThat(all).isNotEmpty();
        assertThat(all.get(0).getTitle()).isEqualTo("Test task");
        assertThat(all.get(0).getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(all.get(0).getPriority()).isEqualTo(TaskPriority.HIGH);
    }
} 