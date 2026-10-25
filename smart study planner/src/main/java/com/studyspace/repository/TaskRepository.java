package com.studyspace.repository;

import com.studyspace.model.Task;
import java.util.List;
import java.util.Optional;

/**
 * Data access contract for managing task entities.
 */
public interface TaskRepository {
    List<Task> findAll();
    Optional<Task> findById(long id);
    Task save(Task task);
    boolean deleteById(long id);
    void saveAll(List<Task> tasks);
}
