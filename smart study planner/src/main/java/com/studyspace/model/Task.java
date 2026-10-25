package com.studyspace.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a study task in the planner.
 */
public class Task {
    private long id;
    private String subject;
    private String description;
    private LocalDate dueDate;
    private Priority priority;
    private TaskType type;
    private boolean done;
    private LocalDateTime createdAt;

    public Task() {
        this.id = System.currentTimeMillis();
        this.dueDate = LocalDate.now();
        this.priority = Priority.MEDIUM;
        this.type = TaskType.STUDY;
        this.done = false;
        this.createdAt = LocalDateTime.now();
    }

    public Task(long id, String subject, String description, LocalDate dueDate, Priority priority, TaskType type, boolean done) {
        this.id = id;
        this.subject = subject != null ? subject.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.dueDate = dueDate != null ? dueDate : LocalDate.now();
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.type = type != null ? type : TaskType.STUDY;
        this.done = done;
        this.createdAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject != null ? subject.trim() : "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate != null ? dueDate : LocalDate.now();
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority != null ? priority : Priority.MEDIUM;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type != null ? type : TaskType.STUDY;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDueToday() {
        return dueDate != null && dueDate.equals(LocalDate.now());
    }

    public boolean isOverdue() {
        return !done && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", subject='" + subject + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", priority=" + priority +
                ", type=" + type +
                ", done=" + done +
                '}';
    }
}
