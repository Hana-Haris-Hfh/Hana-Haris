package com.studyspace.repository;

import com.studyspace.model.Priority;
import com.studyspace.model.Task;
import com.studyspace.model.TaskType;
import com.studyspace.util.JsonUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * File-based TaskRepository implementation that persists tasks as JSON.
 * Thread-safe with atomic disk writes.
 */
public class JsonTaskRepository implements TaskRepository {

    private final Path storagePath;
    private final List<Task> tasks = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public JsonTaskRepository() {
        this(Paths.get("tasks.json"));
    }

    public JsonTaskRepository(Path storagePath) {
        this.storagePath = storagePath;
        load();
    }

    private void load() {
        lock.writeLock().lock();
        try {
            tasks.clear();
            if (Files.exists(storagePath)) {
                String content = Files.readString(storagePath, StandardCharsets.UTF_8);
                List<Task> loaded = JsonUtil.parseTaskList(content);
                tasks.addAll(loaded);
            } else {
                // Pre-populate with a few helpful initial sample tasks so the app doesn't start completely blank
                initSampleData();
                persistToFile();
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load tasks from " + storagePath + ": " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void initSampleData() {
        LocalDate today = LocalDate.now();
        tasks.add(new Task(
                1001L,
                "Data Structures",
                "Revise binary search trees and tree traversals",
                today,
                Priority.HIGH,
                TaskType.STUDY,
                false
        ));
        tasks.add(new Task(
                1002L,
                "Database Management",
                "Complete normalization assignment (1NF, 2NF, 3NF, BCNF)",
                today.plusDays(1),
                Priority.MEDIUM,
                TaskType.ASSIGNMENT,
                false
        ));
        tasks.add(new Task(
                1003L,
                "Operating Systems",
                "Review CPU scheduling algorithms and memory paging",
                today.minusDays(1),
                Priority.LOW,
                TaskType.REVISION,
                true
        ));
    }

    private void persistToFile() {
        try {
            Path parent = storagePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            String json = JsonUtil.tasksToJson(tasks);
            Path tempFile = storagePath.resolveSibling(storagePath.getFileName() + ".tmp");
            Files.writeString(tempFile, json, StandardCharsets.UTF_8);
            Files.move(tempFile, storagePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Error saving tasks to " + storagePath + ": " + e.getMessage());
        }
    }

    @Override
    public List<Task> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(tasks);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Task> findById(long id) {
        lock.readLock().lock();
        try {
            return tasks.stream().filter(t -> t.getId() == id).findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Task save(Task task) {
        lock.writeLock().lock();
        try {
            if (task.getId() == 0) {
                task.setId(System.currentTimeMillis());
            }
            int index = -1;
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getId() == task.getId()) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                tasks.set(index, task);
            } else {
                tasks.add(task);
            }
            persistToFile();
            return task;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean deleteById(long id) {
        lock.writeLock().lock();
        try {
            boolean removed = tasks.removeIf(t -> t.getId() == id);
            if (removed) {
                persistToFile();
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void saveAll(List<Task> updatedTasks) {
        lock.writeLock().lock();
        try {
            tasks.clear();
            if (updatedTasks != null) {
                tasks.addAll(updatedTasks);
            }
            persistToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
