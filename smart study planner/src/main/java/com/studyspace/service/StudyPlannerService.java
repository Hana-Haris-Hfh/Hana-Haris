package com.studyspace.service;

import com.studyspace.model.Priority;
import com.studyspace.model.Task;
import com.studyspace.model.TaskStats;
import com.studyspace.model.TaskType;
import com.studyspace.repository.TaskRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Service managing business rules, filtering, searching, statistics,
 * and lifecycle events for study tasks.
 */
public class StudyPlannerService {

    private final TaskRepository repository;
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();

    public StudyPlannerService(TaskRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    public void addChangeListener(Runnable listener) {
        if (listener != null) {
            changeListeners.add(listener);
        }
    }

    public void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    private void notifyChange() {
        for (Runnable listener : changeListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                System.err.println("Listener error: " + e.getMessage());
            }
        }
    }

    public List<Task> getAllTasks() {
        return repository.findAll();
    }

    public Optional<Task> getTaskById(long id) {
        return repository.findById(id);
    }

    public TaskStats getStats() {
        List<Task> all = repository.findAll();
        int total = all.size();
        int completed = (int) all.stream().filter(Task::isDone).count();
        return new TaskStats(total, completed);
    }

    /**
     * Filters, searches, and sorts tasks matching the UI criteria.
     *
     * @param view "dashboard", "tasks", or "today"
     * @param status "all", "pending", or "completed"
     * @param query search query
     * @return filtered and sorted task list
     */
    public List<Task> getFilteredTasks(String view, String status, String query) {
        List<Task> all = repository.findAll();
        LocalDate today = LocalDate.now();
        String q = query != null ? query.trim().toLowerCase() : "";
        String s = status != null ? status.trim().toLowerCase() : "all";
        String v = view != null ? view.trim().toLowerCase() : "dashboard";

        return all.stream()
                .filter(t -> {
                    // Search query match
                    if (!q.isEmpty()) {
                        String fullText = (t.getSubject() + " " + t.getDescription() + " " + t.getType().getDisplayName()).toLowerCase();
                        if (!fullText.contains(q)) return false;
                    }
                    // Status filter match
                    if ("pending".equals(s) && t.isDone()) return false;
                    if ("completed".equals(s) && !t.isDone()) return false;
                    // View filter match
                    if ("today".equals(v) && !today.equals(t.getDueDate())) return false;
                    return true;
                })
                .sorted((a, b) -> {
                    // Uncompleted tasks first
                    if (a.isDone() != b.isDone()) {
                        return Boolean.compare(a.isDone(), b.isDone());
                    }
                    // Then by due date
                    int dateCmp = a.getDueDate().compareTo(b.getDueDate());
                    if (dateCmp != 0) return dateCmp;
                    // Then by priority (HIGH before MEDIUM before LOW)
                    return Integer.compare(b.getPriority().ordinal(), a.getPriority().ordinal());
                })
                .collect(Collectors.toList());
    }

    public Task createTask(String subject, String description, LocalDate dueDate, Priority priority, TaskType type) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Task description is required.");
        }
        LocalDate date = dueDate != null ? dueDate : LocalDate.now();
        Priority prio = priority != null ? priority : Priority.MEDIUM;
        TaskType tType = type != null ? type : TaskType.STUDY;

        Task task = new Task(System.currentTimeMillis(), subject, description, date, prio, tType, false);
        Task saved = repository.save(task);
        notifyChange();
        return saved;
    }

    public Task updateTask(long id, String subject, String description, LocalDate dueDate, Priority priority, TaskType type) {
        Optional<Task> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new NoSuchElementException("Task with id " + id + " not found.");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject is required.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Task description is required.");
        }

        Task task = existingOpt.get();
        task.setSubject(subject);
        task.setDescription(description);
        task.setDueDate(dueDate != null ? dueDate : LocalDate.now());
        task.setPriority(priority != null ? priority : Priority.MEDIUM);
        task.setType(type != null ? type : TaskType.STUDY);

        Task saved = repository.save(task);
        notifyChange();
        return saved;
    }

    public boolean toggleTaskDone(long id) {
        Optional<Task> existingOpt = repository.findById(id);
        if (existingOpt.isPresent()) {
            Task task = existingOpt.get();
            task.setDone(!task.isDone());
            repository.save(task);
            notifyChange();
            return task.isDone();
        }
        return false;
    }

    public boolean deleteTask(long id) {
        boolean removed = repository.deleteById(id);
        if (removed) {
            notifyChange();
        }
        return removed;
    }
}
