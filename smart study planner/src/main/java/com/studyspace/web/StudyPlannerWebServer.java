package com.studyspace.web;

import com.studyspace.model.Priority;
import com.studyspace.model.Task;
import com.studyspace.model.TaskStats;
import com.studyspace.model.TaskType;
import com.studyspace.service.StudyPlannerService;
import com.studyspace.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Built-in HTTP and REST API server for StudySpace.
 * Powered by standard Java SE HttpServer with zero third-party dependencies.
 */
public class StudyPlannerWebServer {

    private final int port;
    private final StudyPlannerService service;
    private HttpServer server;

    public StudyPlannerWebServer(int port, StudyPlannerService service) {
        this.port = port;
        this.service = service;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // API endpoints
        server.createContext("/api/tasks", new TasksHandler());
        server.createContext("/api/stats", new StatsHandler());

        // Static files (Web UI)
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("StudySpace Web Server started at http://localhost:" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public int getPort() {
        return port;
    }

    // ==========================================
    // HANDLERS
    // ==========================================

    private class TasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            String method = exchange.getRequestMethod().toUpperCase();

            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            // Path could be:
            // /api/tasks
            // /api/tasks/{id}
            // /api/tasks/{id}/toggle

            String[] parts = path.split("/");
            // parts: ["", "api", "tasks", optional {id}, optional {toggle}]

            try {
                if (parts.length <= 3) {
                    // /api/tasks
                    if ("GET".equals(method)) {
                        Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                        String view = params.getOrDefault("view", "dashboard");
                        String status = params.getOrDefault("status", "all");
                        String query = params.getOrDefault("q", "");

                        List<Task> list = service.getFilteredTasks(view, status, query);
                        sendJsonResponse(exchange, 200, JsonUtil.tasksToJson(list));
                    } else if ("POST".equals(method)) {
                        String body = readBody(exchange);
                        Task taskData = JsonUtil.parseTask(body);
                        if (taskData == null || taskData.getSubject().isEmpty() || taskData.getDescription().isEmpty()) {
                            sendJsonResponse(exchange, 400, "{\"error\":\"Subject and description are required\"}");
                            return;
                        }
                        Task created = service.createTask(
                                taskData.getSubject(),
                                taskData.getDescription(),
                                taskData.getDueDate(),
                                taskData.getPriority(),
                                taskData.getType()
                        );
                        sendJsonResponse(exchange, 201, JsonUtil.taskToJson(created));
                    } else {
                        exchange.sendResponseHeaders(405, -1);
                    }
                } else {
                    // /api/tasks/{id} or /api/tasks/{id}/toggle
                    long id = Long.parseLong(parts[3]);

                    if (parts.length == 4) {
                        // /api/tasks/{id}
                        if ("GET".equals(method)) {
                            Optional<Task> taskOpt = service.getTaskById(id);
                            if (taskOpt.isPresent()) {
                                sendJsonResponse(exchange, 200, JsonUtil.taskToJson(taskOpt.get()));
                            } else {
                                sendJsonResponse(exchange, 404, "{\"error\":\"Task not found\"}");
                            }
                        } else if ("PUT".equals(method)) {
                            String body = readBody(exchange);
                            Task taskData = JsonUtil.parseTask(body);
                            Task updated = service.updateTask(
                                    id,
                                    taskData.getSubject(),
                                    taskData.getDescription(),
                                    taskData.getDueDate(),
                                    taskData.getPriority(),
                                    taskData.getType()
                            );
                            sendJsonResponse(exchange, 200, JsonUtil.taskToJson(updated));
                        } else if ("DELETE".equals(method)) {
                            boolean deleted = service.deleteTask(id);
                            if (deleted) {
                                sendJsonResponse(exchange, 200, "{\"success\":true}");
                            } else {
                                sendJsonResponse(exchange, 404, "{\"error\":\"Task not found\"}");
                            }
                        } else {
                            exchange.sendResponseHeaders(405, -1);
                        }
                    } else if (parts.length == 5 && "toggle".equalsIgnoreCase(parts[4])) {
                        // /api/tasks/{id}/toggle
                        if ("POST".equals(method) || "PUT".equals(method)) {
                            boolean isDone = service.toggleTaskDone(id);
                            sendJsonResponse(exchange, 200, "{\"id\":" + id + ",\"done\":" + isDone + "}");
                        } else {
                            exchange.sendResponseHeaders(405, -1);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"error\":\"" + JsonUtil.escape(e.getMessage()) + "\"}");
            }
        }
    }

    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            TaskStats stats = service.getStats();
            sendJsonResponse(exchange, 200, JsonUtil.statsToJson(stats));
        }
    }

    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Look in resources or local folder
            byte[] content = null;
            String contentType = "text/html; charset=UTF-8";

            // Try loading from classpath first
            String resourcePath = "/web" + path;
            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                if (is != null) {
                    content = is.readAllBytes();
                }
            }

            // Fallback to local files if not packaged in jar
            if (content == null) {
                Path localFile = Paths.get("src", "main", "resources", "web", path.substring(1));
                if (Files.exists(localFile)) {
                    content = Files.readAllBytes(localFile);
                } else {
                    Path rootFile = Paths.get("web", path.substring(1));
                    if (Files.exists(rootFile)) {
                        content = Files.readAllBytes(rootFile);
                    }
                }
            }

            if (content != null) {
                if (path.endsWith(".css")) contentType = "text/css; charset=UTF-8";
                else if (path.endsWith(".js")) contentType = "application/javascript; charset=UTF-8";
                else if (path.endsWith(".json")) contentType = "application/json; charset=UTF-8";

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, content.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(content);
                }
            } else {
                String notFound = "<h1>404 Not Found</h1>";
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    // ==========================================
    // UTILITY METHODS
    // ==========================================

    private static void setCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int read;
            while ((read = is.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                try {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                    String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    map.put(key, val);
                } catch (Exception ignored) {}
            }
        }
        return map;
    }
}
