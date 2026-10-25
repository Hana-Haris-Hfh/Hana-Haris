package com.studyspace;

import com.studyspace.model.Priority;
import com.studyspace.model.Task;
import com.studyspace.model.TaskStats;
import com.studyspace.model.TaskType;
import com.studyspace.repository.JsonTaskRepository;
import com.studyspace.service.StudyPlannerService;
import com.studyspace.util.JsonUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Self-contained tests for verifying core business logic, JSON processing, and filtering.
 */
public class StudyPlannerServiceTest {

    public static void main(String[] args) throws IOException {
        System.out.println("Running StudyPlanner core tests...");
        Path tempFile = Files.createTempFile("test-tasks-", ".json");
        Files.deleteIfExists(tempFile);

        try {
            JsonTaskRepository repo = new JsonTaskRepository(tempFile);
            StudyPlannerService service = new StudyPlannerService(repo);

            // Test 1: Initial demo data pre-population
            List<Task> initial = service.getAllTasks();
            assertTrue(initial.size() >= 3, "Initial tasks should be populated");
            System.out.println("✓ Test 1: Pre-populated sample data verified (" + initial.size() + " tasks)");

            // Test 2: Create task
            Task created = service.createTask("Algorithms", "Study graph Dijkstra and Bellman-Ford", LocalDate.now(), Priority.HIGH, TaskType.STUDY);
            assertNotNull(created, "Created task must not be null");
            assertTrue(created.getId() > 0, "Task should receive valid ID");
            assertEquals("Algorithms", created.getSubject(), "Subject match");
            System.out.println("✓ Test 2: Task creation verified");

            // Test 3: Toggle task done
            boolean done = service.toggleTaskDone(created.getId());
            assertTrue(done, "Task status should be completed (true)");
            Task fetched = service.getTaskById(created.getId()).orElseThrow();
            assertTrue(fetched.isDone(), "Fetched task should reflect completed status");
            System.out.println("✓ Test 3: Toggle task done verified");

            // Test 4: Stats calculation
            TaskStats stats = service.getStats();
            assertTrue(stats.getTotal() >= 4, "Total tasks count >= 4");
            assertTrue(stats.getCompleted() >= 2, "Completed count check");
            assertTrue(stats.getPercentage() > 0, "Percentage calculation check");
            System.out.println("✓ Test 4: Statistics calculation verified (" + stats + ")");

            // Test 5: Filtering
            List<Task> todayTasks = service.getFilteredTasks("today", "all", "");
            for (Task t : todayTasks) {
                assertEquals(LocalDate.now(), t.getDueDate(), "Task should be due today");
            }
            System.out.println("✓ Test 5: Filter by Today view verified (" + todayTasks.size() + " tasks)");

            // Test 6: Search
            List<Task> searchResults = service.getFilteredTasks("all", "all", "Dijkstra");
            assertEquals(1, searchResults.size(), "Search should find 1 task");
            System.out.println("✓ Test 6: Search query verified");

            // Test 7: JSON serialization & parsing
            String json = JsonUtil.taskToJson(created);
            Task parsed = JsonUtil.parseTask(json);
            assertNotNull(parsed, "Parsed task must not be null");
            assertEquals(created.getId(), parsed.getId(), "IDs must match");
            assertEquals(created.getSubject(), parsed.getSubject(), "Subjects must match");
            System.out.println("✓ Test 7: JSON serialization/deserialization verified");

            // Test 8: Delete task
            boolean deleted = service.deleteTask(created.getId());
            assertTrue(deleted, "Task deletion should return true");
            assertTrue(service.getTaskById(created.getId()).isEmpty(), "Task should no longer exist");
            System.out.println("✓ Test 8: Task deletion verified");

            System.out.println("\nAll 8 core tests PASSED successfully!");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("FAILED: " + message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) throw new AssertionError("FAILED: " + message + " (expected: " + expected + ", got: " + actual + ")");
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) throw new AssertionError("FAILED: " + message);
    }
}
