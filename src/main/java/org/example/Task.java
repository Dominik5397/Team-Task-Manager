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
     * Nowa szczegółowa historia zmian jako osobne encje
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("task")
    private List<ChangeLogEntry> changeLogEntries = new ArrayList<>();

    /**
     * Stary format change log (JSON) - zachowany dla kompatybilności wstecznej
     * @deprecated Używaj changeLogEntries zamiast tego pola
     */
    @Deprecated
    @Column(columnDefinition = "TEXT")
    private String changeLog;

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
    
    public List<ChangeLogEntry> getChangeLogEntries() { return changeLogEntries; }
    public void setChangeLogEntries(List<ChangeLogEntry> changeLogEntries) { this.changeLogEntries = changeLogEntries; }
    
    /**
     * @deprecated Używaj getChangeLogEntries() zamiast tego
     */
    @Deprecated
    public String getChangeLog() { return changeLog; }
    
    /**
     * @deprecated Używaj addChangeLogEntry() zamiast tego
     */
    @Deprecated
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

    /**
     * Dodaje nowy wpis do historii zmian
     */
    public void addChangeLogEntry(ChangeLogEntry entry) {
        if (changeLogEntries == null) {
            changeLogEntries = new ArrayList<>();
        }
        changeLogEntries.add(entry);
        entry.setTask(this);
    }

    /**
     * Usuwa wpis z historii zmian
     */
    public void removeChangeLogEntry(ChangeLogEntry entry) {
        if (changeLogEntries != null) {
            changeLogEntries.remove(entry);
            entry.setTask(null);
        }
    }

    /**
     * Pobiera liczbę wpisów w historii zmian
     */
    public int getChangeLogEntriesCount() {
        return changeLogEntries != null ? changeLogEntries.size() : 0;
    }

    /**
     * Sprawdza czy zadanie ma jakiekolwiek wpisy w historii zmian
     */
    public boolean hasChangeLogEntries() {
        return changeLogEntries != null && !changeLogEntries.isEmpty();
    }
} 