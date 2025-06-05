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
import java.util.List;
import java.util.ArrayList;

/**
 * Task entity representing a task in the task management system.
 * Uses dedicated ChangeLogEntry entities for change tracking.
 */
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

    /**
     * Change history as dedicated entities managed by ChangeLogService
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("task")
    private List<ChangeLogEntry> changeLogEntries = new ArrayList<>();

    // Constructors
    public Task() {}

    public Task(String title, String description, TaskStatus status, TaskPriority priority) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
    }

    // Getters and Setters
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
    
    public List<ChangeLogEntry> getChangeLogEntries() { return changeLogEntries; }
    public void setChangeLogEntries(List<ChangeLogEntry> changeLogEntries) { this.changeLogEntries = changeLogEntries; }

    // Business Logic Methods
    public boolean isHighPriority() {
        return priority == TaskPriority.HIGH;
    }
    
    public boolean isCompleted() {
        return status == TaskStatus.DONE;
    }
    
    public boolean isInProgress() {
        return status == TaskStatus.IN_PROGRESS;
    }

    // Change Log Management Methods
    /**
     * Adds a new change log entry to this task
     */
    public void addChangeLogEntry(ChangeLogEntry entry) {
        if (changeLogEntries == null) {
            changeLogEntries = new ArrayList<>();
        }
        changeLogEntries.add(entry);
        entry.setTask(this);
    }

    /**
     * Removes a change log entry from this task
     */
    public void removeChangeLogEntry(ChangeLogEntry entry) {
        if (changeLogEntries != null) {
            changeLogEntries.remove(entry);
            entry.setTask(null);
        }
    }

    /**
     * Gets the count of change log entries for this task
     */
    public int getChangeLogEntriesCount() {
        return changeLogEntries != null ? changeLogEntries.size() : 0;
    }

    /**
     * Checks if this task has any change log entries
     */
    public boolean hasChangeLogEntries() {
        return changeLogEntries != null && !changeLogEntries.isEmpty();
    }

    // Backwards compatibility for JSON serialization
    /**
     * Returns empty string for backwards compatibility with frontend
     * @deprecated Use getChangeLogEntries() instead
     */
    @Deprecated
    public String getChangeLog() {
        return "[]"; // Empty JSON array for compatibility
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", assignedTo=" + (assignedTo != null ? assignedTo.getUsername() : "unassigned") +
                '}';
    }
} 