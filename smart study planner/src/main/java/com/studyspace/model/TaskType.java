package com.studyspace.model;

/**
 * Task category / type.
 */
public enum TaskType {
    STUDY("Study"),
    ASSIGNMENT("Assignment"),
    EXAM("Exam"),
    REVISION("Revision");

    private final String displayName;

    TaskType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TaskType fromString(String value) {
        if (value == null) return STUDY;
        for (TaskType t : values()) {
            if (t.displayName.equalsIgnoreCase(value.trim()) || t.name().equalsIgnoreCase(value.trim())) {
                return t;
            }
        }
        return STUDY;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
