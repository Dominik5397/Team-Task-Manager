package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class ValidationTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    public void testUserValidation_ValidUser() {
        User user = new User();
        user.setUsername("Jan Kowalski");
        user.setEmail("jan.kowalski@example.com");
        user.setAvatarUrl("https://example.com/avatar.jpg");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testUserValidation_InvalidEmail() {
        User user = new User();
        user.setUsername("Jan Kowalski");
        user.setEmail("invalid-email");
        user.setAvatarUrl("https://example.com/avatar.jpg");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Email should be valid");
    }

    @Test
    public void testUserValidation_BlankUsername() {
        User user = new User();
        user.setUsername("");
        user.setEmail("jan.kowalski@example.com");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).hasSize(2);
        boolean hasRequiredMessage = violations.stream()
            .anyMatch(v -> v.getMessage().contains("Username is required"));
        boolean hasSizeMessage = violations.stream()
            .anyMatch(v -> v.getMessage().contains("Username must be between 2 and 50 characters"));
        assertThat(hasRequiredMessage || hasSizeMessage).isTrue();
    }

    @Test
    public void testTaskValidation_ValidTask() {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test description");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(LocalDate.now().plusDays(7));

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testTaskValidation_BlankTitle() {
        Task task = new Task();
        task.setTitle("");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertThat(violations).hasSize(2);
        boolean hasRequiredMessage = violations.stream()
            .anyMatch(v -> v.getMessage().contains("Title is required"));
        boolean hasSizeMessage = violations.stream()
            .anyMatch(v -> v.getMessage().contains("Title must be between 3 and 100 characters"));
        assertThat(hasRequiredMessage || hasSizeMessage).isTrue();
    }

    @Test
    public void testTaskValidation_NullStatus() {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setStatus(null);
        task.setPriority(TaskPriority.HIGH);

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Status is required");
    }

    @Test
    public void testTaskValidation_NullPriority() {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(null);

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Priority is required");
    }

    @Test
    public void testTaskValidation_PastDueDate() {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(LocalDate.now().minusDays(1));

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Due date must be in the future");
    }

    @Test
    public void testTaskStatus_EnumValues() {
        assertThat(TaskStatus.TODO.getDisplayName()).isEqualTo("To Do");
        assertThat(TaskStatus.IN_PROGRESS.getDisplayName()).isEqualTo("In Progress");
        assertThat(TaskStatus.DONE.getDisplayName()).isEqualTo("Done");
    }

    @Test
    public void testTaskPriority_EnumValues() {
        assertThat(TaskPriority.LOW.getDisplayName()).isEqualTo("Low");
        assertThat(TaskPriority.LOW.getLevel()).isEqualTo(1);
        
        assertThat(TaskPriority.MEDIUM.getDisplayName()).isEqualTo("Medium");
        assertThat(TaskPriority.MEDIUM.getLevel()).isEqualTo(2);
        
        assertThat(TaskPriority.HIGH.getDisplayName()).isEqualTo("High");
        assertThat(TaskPriority.HIGH.getLevel()).isEqualTo(3);
        
        assertThat(TaskPriority.HIGH.isHigherThan(TaskPriority.LOW)).isTrue();
        assertThat(TaskPriority.LOW.isLowerThan(TaskPriority.HIGH)).isTrue();
    }

    @Test
    public void testTaskStatus_FromString() {
        assertThat(TaskStatus.fromString("To Do")).isEqualTo(TaskStatus.TODO);
        assertThat(TaskStatus.fromString("TODO")).isEqualTo(TaskStatus.TODO);
        assertThat(TaskStatus.fromString("In Progress")).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(TaskStatus.fromString("Done")).isEqualTo(TaskStatus.DONE);
    }

    @Test
    public void testTaskPriority_FromString() {
        assertThat(TaskPriority.fromString("Low")).isEqualTo(TaskPriority.LOW);
        assertThat(TaskPriority.fromString("LOW")).isEqualTo(TaskPriority.LOW);
        assertThat(TaskPriority.fromString("Medium")).isEqualTo(TaskPriority.MEDIUM);
        assertThat(TaskPriority.fromString("High")).isEqualTo(TaskPriority.HIGH);
    }
} 