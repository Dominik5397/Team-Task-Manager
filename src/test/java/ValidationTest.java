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
        task.setStatus("To Do");
        task.setPriority("High");
        task.setDueDate(LocalDate.now().plusDays(7));

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testTaskValidation_BlankTitle() {
        Task task = new Task();
        task.setTitle("");
        task.setStatus("To Do");
        task.setPriority("High");

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertThat(violations).hasSize(2);
        boolean hasRequiredMessage = violations.stream()
            .anyMatch(v -> v.getMessage().contains("Title is required"));
        boolean hasSizeMessage = violations.stream()
            .anyMatch(v -> v.getMessage().contains("Title must be between 3 and 100 characters"));
        assertThat(hasRequiredMessage || hasSizeMessage).isTrue();
    }

    @Test
    public void testTaskValidation_InvalidStatus() {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setStatus("Invalid Status");
        task.setPriority("High");

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Status must be one of");
    }

    @Test
    public void testTaskValidation_InvalidPriority() {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setStatus("To Do");
        task.setPriority("Invalid Priority");

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Priority must be one of");
    }

    @Test
    public void testTaskValidation_PastDueDate() {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setStatus("To Do");
        task.setPriority("High");
        task.setDueDate(LocalDate.now().minusDays(1)); // Data w przeszłości

        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Due date must be in the future");
    }
} 