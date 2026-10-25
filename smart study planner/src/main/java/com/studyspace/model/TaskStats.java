package com.studyspace.model;

/**
 * Encapsulates statistical metrics for planner tasks.
 */
public class TaskStats {
    private final int total;
    private final int completed;
    private final int pending;
    private final int percentage;

    public TaskStats(int total, int completed) {
        this.total = total;
        this.completed = completed;
        this.pending = Math.max(0, total - completed);
        this.percentage = total > 0 ? (int) Math.round((double) completed / total * 100.0) : 0;
    }

    public int getTotal() {
        return total;
    }

    public int getCompleted() {
        return completed;
    }

    public int getPending() {
        return pending;
    }

    public int getPercentage() {
        return percentage;
    }

    @Override
    public String toString() {
        return "TaskStats{" +
                "total=" + total +
                ", completed=" + completed +
                ", pending=" + pending +
                ", percentage=" + percentage + "%" +
                '}';
    }
}
