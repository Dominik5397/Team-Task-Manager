package org.example;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDate;

@Entity
@Table(name = "task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @JsonSerialize(using = TaskStatusConverter.Serializer.class)
    @JsonDeserialize(using = TaskStatusConverter.Deserializer.class)
    private TaskStatus status;
    
    @NotNull(message = "Priority is required")  
    @Enumerated(EnumType.STRING)
    @JsonSerialize(using = TaskPriorityConverter.Serializer.class)
    @JsonDeserialize(using = TaskPriorityConverter.Deserializer.class)
    private TaskPriority priority;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnoreProperties("tasks")
    private User assignedTo;

    @Column(columnDefinition = "TEXT")
    private String changeLog; // JSON z historią zmian

    // Gettery i settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }
    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
    public String getChangeLog() { return changeLog; }
    public void setChangeLog(String changeLog) { this.changeLog = changeLog; }
    
    // Metody pomocnicze
    public boolean isHighPriority() {
        return priority == TaskPriority.HIGH;
    }
    
    public boolean isCompleted() {
        return status == TaskStatus.DONE;
    }
    
    public boolean isInProgress() {
        return status == TaskStatus.IN_PROGRESS;
    }
} 