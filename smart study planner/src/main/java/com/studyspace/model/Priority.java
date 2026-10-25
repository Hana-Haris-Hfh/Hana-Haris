package com.studyspace.model;

/**
 * Task priority levels with display labels and theme colors.
 */
public enum Priority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Priority fromString(String value) {
        if (value == null) return MEDIUM;
        for (Priority p : values()) {
            if (p.displayName.equalsIgnoreCase(value.trim()) || p.name().equalsIgnoreCase(value.trim())) {
                return p;
            }
        }
        return MEDIUM;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
