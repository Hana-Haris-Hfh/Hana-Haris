package com.studyspace.util;

import com.studyspace.model.Priority;
import com.studyspace.model.Task;
import com.studyspace.model.TaskStats;
import com.studyspace.model.TaskType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight, zero-dependency JSON utility designed specifically for Task entities
 * and REST communication in the Smart Study Planner.
 */
public final class JsonUtil {

    private JsonUtil() {}

    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < ' ') {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    public static String unescape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        boolean backslash = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (backslash) {
                switch (c) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (i + 4 < s.length()) {
                            try {
                                int code = Integer.parseInt(s.substring(i + 1, i + 5), 16);
                                sb.append((char) code);
                                i += 4;
                            } catch (NumberFormatException e) {
                                sb.append('u');
                            }
                        } else {
                            sb.append('u');
                        }
                        break;
                    default:
                        sb.append(c);
                        break;
                }
                backslash = false;
            } else if (c == '\\') {
                backslash = true;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String taskToJson(Task t) {
        if (t == null) return "null";
        return String.format(
                "{\"id\":%d,\"subject\":\"%s\",\"description\":\"%s\",\"due\":\"%s\",\"priority\":\"%s\",\"type\":\"%s\",\"done\":%b}",
                t.getId(),
                escape(t.getSubject()),
                escape(t.getDescription()),
                t.getDueDate() != null ? t.getDueDate().toString() : LocalDate.now().toString(),
                t.getPriority().getDisplayName(),
                t.getType().getDisplayName(),
                t.isDone()
        );
    }

    public static String tasksToJson(List<Task> list) {
        if (list == null) return "[]";
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < list.size(); i++) {
            sb.append("  ").append(taskToJson(list.get(i)));
            if (i < list.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String statsToJson(TaskStats stats) {
        return String.format(
                "{\"total\":%d,\"completed\":%d,\"pending\":%d,\"percentage\":%d}",
                stats.getTotal(),
                stats.getCompleted(),
                stats.getPending(),
                stats.getPercentage()
        );
    }

    public static Map<String, String> parseSimpleJsonObject(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null) return map;
        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }

        // Match key-value pairs: "key": value
        Pattern p = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"[^\"]*\"|true|false|null|[0-9.]+)");
        Matcher m = p.matcher(trimmed);
        while (m.find()) {
            String key = m.group(1);
            String rawVal = m.group(2).trim();
            if (rawVal.startsWith("\"") && rawVal.endsWith("\"")) {
                rawVal = unescape(rawVal.substring(1, rawVal.length() - 1));
            }
            map.put(key, rawVal);
        }
        return map;
    }

    public static Task parseTask(String json) {
        Map<String, String> map = parseSimpleJsonObject(json);
        if (map.isEmpty()) return null;

        Task t = new Task();
        if (map.containsKey("id")) {
            try {
                t.setId(Long.parseLong(map.get("id")));
            } catch (Exception ignored) {}
        }
        if (map.containsKey("subject")) {
            t.setSubject(map.get("subject"));
        }
        if (map.containsKey("description")) {
            t.setDescription(map.get("description"));
        }
        if (map.containsKey("due")) {
            try {
                t.setDueDate(LocalDate.parse(map.get("due")));
            } catch (Exception ignored) {
                t.setDueDate(LocalDate.now());
            }
        }
        if (map.containsKey("priority")) {
            t.setPriority(Priority.fromString(map.get("priority")));
        }
        if (map.containsKey("type")) {
            t.setType(TaskType.fromString(map.get("type")));
        }
        if (map.containsKey("done")) {
            t.setDone(Boolean.parseBoolean(map.get("done")));
        }
        return t;
    }

    public static List<Task> parseTaskList(String json) {
        List<Task> result = new ArrayList<>();
        if (json == null) return result;
        String s = json.trim();
        if (!s.startsWith("[") || !s.endsWith("]")) return result;

        // Find each { ... } block
        int depth = 0;
        int start = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    String objStr = s.substring(start, i + 1);
                    Task task = parseTask(objStr);
                    if (task != null) {
                        result.add(task);
                    }
                    start = -1;
                }
            }
        }
        return result;
    }
}
