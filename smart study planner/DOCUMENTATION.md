# StudySpace — Smart Study Planner: Comprehensive Project Documentation

---

## 1. Executive Summary

**StudySpace** is an all-in-one, modern **Smart Study Planner** desktop and web application engineered entirely in **Java (JDK 17+ / JDK 21 LTS)** with zero external dependencies.

Designed specifically for students, researchers, and self-directed learners, StudySpace bridges focused daily task execution with long-term study planning. It integrates real-time progress statistics, structured categorizations (Study, Assignment, Exam, Revision), priority weighting, instant quick-logging, and safe local persistence.

The software offers dual interface capabilities:
1. **A Native Desktop Application** built with Java Swing and custom 2D anti-aliased vector components that emulate modern card-based design systems.
2. **A Built-in Web Server & REST API** utilizing Java standard `com.sun.net.httpserver.HttpServer`, which hosts the responsive web dashboard and exposes complete RESTful endpoints for cross-platform interoperability.

---

## 2. System Architecture

StudySpace follows **Clean Architecture** and strict **Separation of Concerns (SoC)**, dividing functionality into independent, decoupled layers.

```
+-----------------------------------------------------------------------+
|                         Presentation Layer                            |
|  +--------------------------------+  +-----------------------------+  |
|  |     Modern Desktop GUI         |  |   Web UI (HTML5/CSS3/JS)    |  |
|  |   (Swing & Vector Graphics)    |  |   (Browser Dashboard)       |  |
|  +----------------+---------------+  +--------------+--------------+  |
+-------------------|---------------------------------|-----------------+
                    |                                 |
+-------------------|---------------------------------|-----------------+
|                   |           HTTP / REST Layer     v                 |
|                   |          +-------------------------------------+  |
|                   |          | StudyPlannerWebServer (HttpServer)  |  |
|                   |          | Endpoints: /api/tasks, /api/stats   |  |
|                   v          +----------------------+--------------+  |
|   +-------------------------------------------------+                 |
|   |          StudyPlannerService (Business Logic)   |                 |
|   |  - Task validation & state management           |                 |
|   |  - Filtering (Today, Dashboard, All Tasks)      |                 |
|   |  - Real-time search query processor             |                 |
|   |  - Metric & percentage calculator               |                 |
|   |  - Reactive Observer / Change notifications     |                 |
|   +---------------------------------+---------------+                 |
+-------------------------------------|---------------------------------+
                                      v
+-----------------------------------------------------------------------+
|                       Data Access & Persistence                       |
|   +----------------------------------------------------------------+  |
|   | TaskRepository (Contract)  <-- JsonTaskRepository (Impl)       |  |
|   |  - Thread-safe ReentrantReadWriteLock                          |  |
|   |  - Atomic file swap replacement                                |  |
|   |  - Zero-dependency JSON Serializer/Deserializer (JsonUtil)    |  |
|   +---------------------------------+------------------------------+  |
|                                     v                                 |
|                              [ tasks.json ]                           |
+-----------------------------------------------------------------------+
```

---

## 3. Package Structure & Key Classes

```
com.studyspace
├── Main.java                          # Universal application bootstrap & CLI parser
│
├── model/                             # Domain Entities & Value Objects
│   ├── Task.java                      # Core study task entity
│   ├── Priority.java                  # Enum: LOW, MEDIUM, HIGH
│   ├── TaskType.java                  # Enum: STUDY, ASSIGNMENT, EXAM, REVISION
│   └── TaskStats.java                 # Aggregated metrics (total, done, pending, %)
│
├── repository/                        # Persistence Layer
│   ├── TaskRepository.java            # Interface contract for CRUD operations
│   └── JsonTaskRepository.java        # Atomic JSON storage with read/write locks
│
├── service/                           # Business Domain Logic
│   └── StudyPlannerService.java       # Filtering, sorting, stats & lifecycle events
│
├── util/                              # Utilities
│   └── JsonUtil.java                  # High-performance zero-dependency JSON parser
│
├── gui/                               # Native Desktop User Interface
│   ├── StudyPlannerApp.java           # Main JFrame window & layout manager
│   ├── theme/
│   │   └── UITheme.java               # Color palette, font definitions, rendering hints
│   ├── components/
│   │   ├── CardPanel.java             # Rounded card container
│   │   ├── ModernButton.java          # Smooth rounded button with hover state
│   │   ├── ModernProgressBar.java     # Gradient rounded horizontal progress bar
│   │   ├── ModernTextField.java       # Clean text input with placeholder support
│   │   ├── TagBadge.java              # Color-coded pill badges for Type & Priority
│   │   ├── TaskItemPanel.java         # Interactive task row with custom checkbox
│   │   └── ToastNotification.java     # Animated bottom-right alert banner
│   └── dialog/
│       └── TaskDialog.java            # Modal dialog for task authoring and editing
│
└── web/                               # HTTP Server & REST API
    ├── StudyPlannerWebServer.java     # Multi-threaded embedded web server
    └── WebServerApp.java              # Standalone web runner
```

---

## 4. Domain Data Model & Persistence

### 4.1 Task Entity Schema
Each task is encapsulated with the following properties:

| Field | Type | Description | Constraints / Default |
|---|---|---|---|
| `id` | `long` | Unique timestamp identifier | Generated via `System.currentTimeMillis()` |
| `subject` | `String` | Academic course or subject name | Non-null, non-empty, max 50 chars |
| `description` | `String` | Specific study objective or task | Non-null, non-empty, max 120 chars |
| `dueDate` | `LocalDate` | Due date for the task | Format `YYYY-MM-DD`, defaults to current date |
| `priority` | `Priority` | Urgency tier (`LOW`, `MEDIUM`, `HIGH`) | Defaults to `MEDIUM` |
| `type` | `TaskType` | Task classification | `STUDY`, `ASSIGNMENT`, `EXAM`, `REVISION` |
| `done` | `boolean` | Completion state | Defaults to `false` |
| `createdAt` | `LocalDateTime` | Record creation timestamp | Initialized on instantiation |

### 4.2 Atomic Persistence (`JsonTaskRepository`)
Tasks are persisted to `tasks.json` in the root workspace directory.
- **Thread Safety**: Uses `java.util.concurrent.locks.ReentrantReadWriteLock` to guarantee safe concurrent access between GUI Event Threads and HTTP Server worker threads.
- **Crash Resilience**: Implements atomic temporary-file writes (`Files.move(tempFile, targetFile, ATOMIC_MOVE, REPLACE_EXISTING)`) preventing data corruption on unexpected power loss or process kill.
- **Cold-Start Prepopulation**: If `tasks.json` does not exist upon initial boot, the repository seeds default sample tasks covering different subjects, priorities, and statuses.

---

## 5. Desktop GUI Implementation (Swing & Java2D)

The desktop graphical interface is crafted without third-party UI libraries, leveraging Java's built-in `Graphics2D` rendering engine:

1. **Anti-Aliasing Engine**: Enabled across all custom components via:
   ```java
   g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
   g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
   ```
2. **Color Palette (`UITheme.java`)**:
   - Background Canvas: `#f8f8fc`
   - Primary Brand Purple: `#7664d8`
   - Active Lavender: `#f0edff`
   - Primary Text: `#28263d`
   - Muted Labels: `#85839a`
   - Success Green: `#55b994`
   - Priority High (Coral): `#d66e60` on `#fff0ed`
   - Priority Low (Mint): `#459b78` on `#e6f7ef`
3. **Responsive Grid**:
   - **Left Sidebar**: Permanent navigation drawer with active view highlight, inspirational learning tip card, and footer branding.
   - **Main Content**: Top statistics card row (Total, Completed, Pending, Custom Gradient Progress Bar) followed by a two-column workspace.
   - **Tasks Panel**: Search filter, status filter (`All`, `Pending`, `Completed`), and dynamically generated task rows with strikethrough effects on completion.
   - **Quick Planner & Quote Card**: Rapid-entry form for immediate session logging and an inspirational gradient quote widget.
   - **Toast Notifications**: Floating translucent pill at the bottom right indicating task actions.

---

## 6. Built-in Web Server & RESTful API

Powered by `com.sun.net.httpserver.HttpServer` with a fixed thread pool (`Executors.newFixedThreadPool(10)`).

### 6.1 REST Endpoints

#### 1. Retrieve Tasks
- **Route**: `GET /api/tasks`
- **Query Parameters**:
  - `view`: `dashboard` (default), `tasks`, `today`
  - `status`: `all` (default), `pending`, `completed`
  - `q`: Search substring (matches subject, description, or type)
- **Response**: `200 OK`
```json
[
  {
    "id": 1001,
    "subject": "Data Structures",
    "description": "Revise binary search trees and tree traversals",
    "due": "2026-10-25",
    "priority": "High",
    "type": "Study",
    "done": false
  }
]
```

#### 2. Create Task
- **Route**: `POST /api/tasks`
- **Request Body**:
```json
{
  "subject": "Discrete Mathematics",
  "description": "Solve graph isomorphism problems",
  "due": "2026-10-26",
  "priority": "High",
  "type": "Assignment"
}
```
- **Response**: `201 Created`

#### 3. Update Task
- **Route**: `PUT /api/tasks/{id}`
- **Response**: `200 OK`

#### 4. Toggle Task Status
- **Route**: `POST /api/tasks/{id}/toggle`
- **Response**: `200 OK`
```json
{
  "id": 1001,
  "done": true
}
```

#### 5. Delete Task
- **Route**: `DELETE /api/tasks/{id}`
- **Response**: `200 OK` (`{"success": true}`)

#### 6. Retrieve Statistics
- **Route**: `GET /api/stats`
- **Response**: `200 OK`
```json
{
  "total": 5,
  "completed": 2,
  "pending": 3,
  "percentage": 40
}
```

---

## 7. Verification & Automated Testing

The project includes a standalone automated test suite (`StudyPlannerServiceTest.java`) validating core invariants:
1. **Pre-population Validation**: Confirms default sample dataset loads properly on clean install.
2. **Task Creation**: Tests input sanitization and ID generation.
3. **Status Toggling**: Verifies atomic status inversion and event propagation.
4. **Statistical Calculations**: Validates dynamic percentages and boundary conditions ($0\%$ and $100\%$).
5. **Today's Plan Filtering**: Asserts only records matching `LocalDate.now()` are returned.
6. **Search Substring Matching**: Verifies multi-word and case-insensitive search queries.
7. **JSON Serialization & Roundtrip Parsing**: Ensures escaped characters, dates, and types survive serialization.
8. **Task Deletion**: Verifies cascading cache removal and file synchronization.

To execute tests:
```cmd
test.bat
```

---

## 8. Deployment & Execution Guide

### 8.1 Prerequisites
- **Java Development Kit**: JDK 17, 21, or newer.

### 8.2 Command Reference

| Goal | Command | Description |
|---|---|---|
| **Launch Desktop App** | `run.bat` or `java -jar studyspace.jar` | Opens desktop window + starts background REST server |
| **Launch Web Mode** | `run-web.bat` or `java -jar studyspace.jar --web` | Starts server at `http://localhost:8080` & opens browser |
| **Custom Port Web Mode** | `java -jar studyspace.jar --web --port 9090` | Runs web server on port 9090 |
| **Run Unit Tests** | `test.bat` | Compiles and executes test suite |
| **Build from Source** | `build.bat` | Compiles classes and packages `studyspace.jar` |
| **Build with Maven** | `mvn clean package` | Standard Maven lifecycle build |
