# StudySpace · Smart Study Planner (Java)

A complete, production-grade **Smart Study Planner** built entirely in **Java (JDK 17+)** with zero external dependencies.

It features a **Modern Desktop GUI (Java Swing)** with custom anti-aliased styling mirroring the web design, alongside a built-in **Java HTTP & REST API Web Server** (`com.sun.net.httpserver.HttpServer`) serving the responsive web client and persistent JSON storage.

---

## 🌟 Highlights & Features

- **Triple-View Navigation**:
  - **Dashboard**: Complete summary overview, real-time statistics cards, recent study tasks, and quick session planner.
  - **All Tasks**: Searchable, filterable list of all study tasks across subjects.
  - **Today's Plan**: Automatically isolates and tracks items due today.
- **Dynamic Metrics & Statistics**:
  - **Total Tasks**: Overall count of scheduled tasks.
  - **Completed**: Track completed goals.
  - **Pending**: Tasks requiring attention.
  - **Progress Bar**: Custom rounded gradient progress indicator displaying exact completion percentage.
- **Task Management**:
  - **Add / Edit Modal Dialog**: Subject, task description, due date picker/format, priority (Low, Medium, High), and category type (Study, Assignment, Exam, Revision).
  - **Quick Planner**: Instant single-click task capture right from the dashboard.
  - **Status Toggle**: Instant check-off with completed checkmark styling, strikethrough, and audio/toast feedback.
  - **Deletion & Confirmation**: Safe removal with confirmation prompt.
- **Real-Time Filtering & Search**:
  - Instant keystroke search matching subject, description, or task type.
  - Status dropdown filter: *All status*, *Pending*, *Completed*.
- **Data Persistence**:
  - Automatically saves tasks to `tasks.json` in local storage using thread-safe atomic file writes. Data persists across sessions.
- **Dual Execution Modes**:
  1. **Modern Desktop GUI**: Native Java Swing application with rounded cards, custom buttons, soft borders, and floating toast notifications.
  2. **Web / REST Server**: Built-in HTTP server providing REST API (`/api/tasks`, `/api/stats`) and serving the responsive web UI at `http://localhost:8080`.

---

## 📂 Project Structure

```
smart study planner/
├── pom.xml                                      # Maven build configuration
├── build.bat                                    # One-click Windows compile & package script
├── run.bat                                      # Launch Desktop GUI (+ background server)
├── run-web.bat                                  # Launch standalone Web Server (browser)
├── test.bat                                     # Run automated unit test suite
├── README.md                                    # Comprehensive documentation
├── tasks.json                                   # Persistent JSON database (auto-created)
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/studyspace/
    │   │       ├── Main.java                    # Universal launcher (GUI / Web / CLI)
    │   │       ├── model/
    │   │       │   ├── Task.java                # Core task entity
    │   │       │   ├── Priority.java            # LOW, MEDIUM, HIGH enum
    │   │       │   ├── TaskType.java            # STUDY, ASSIGNMENT, EXAM, REVISION
    │   │       │   └── TaskStats.java           # Summary statistics data model
    │   │       ├── repository/
    │   │       │   ├── TaskRepository.java      # Data access interface
    │   │       │   └── JsonTaskRepository.java  # Thread-safe JSON file persistence
    │   │       ├── service/
    │   │       │   └── StudyPlannerService.java # Business logic, search, filter & stats
    │   │       ├── util/
    │   │       │   └── JsonUtil.java            # Zero-dependency JSON serializer & parser
    │   │       ├── gui/
    │   │       │   ├── StudyPlannerApp.java     # Main Swing desktop application window
    │   │       │   ├── theme/
    │   │       │   │   └── UITheme.java         # Color palette, fonts, anti-aliasing
    │   │       │   ├── components/
    │   │       │   │   ├── CardPanel.java       # Rounded card container
    │   │       │   │   ├── ModernButton.java    # Rounded interactive button with hover
    │   │       │   │   ├── ModernProgressBar.java # Gradient progress bar
    │   │       │   │   ├── ModernTextField.java # Rounded text input with placeholder
    │   │       │   │   ├── TagBadge.java        # Priority and Type pill badges
    │   │       │   │   ├── TaskItemPanel.java   # Individual task row component
    │   │       │   │   └── ToastNotification.java # Bottom-right animated notification
    │   │       │   └── dialog/
    │   │       │       └── TaskDialog.java      # Modal dialog for task creation & editing
    │   │       └── web/
    │   │           ├── StudyPlannerWebServer.java # Java SE HTTP & REST API server
    │   │           └── WebServerApp.java        # Standalone web runner
    │   └── resources/
    │       └── web/
    │           └── index.html                   # Modern Web UI client
    └── test/
        └── java/
            └── com/studyspace/
                └── StudyPlannerServiceTest.java # Core service & persistence test suite
```

---

## 🚀 How to Run

### Quick Windows One-Click Launchers

- **Run Desktop Application**:
  Double-click `run.bat` or run in terminal:
  ```cmd
  run.bat
  ```
  *Launches the native Java Swing Desktop GUI window with the background REST server enabled.*

- **Run Web Server & Browser App**:
  Double-click `run-web.bat` or run in terminal:
  ```cmd
  run-web.bat
  ```
  *Starts the server at `http://localhost:8080` and opens your default browser.*

- **Rebuild from Source**:
  ```cmd
  build.bat
  ```
  *Compiles Java classes into `bin/` and creates the standalone `studyspace.jar`.*

- **Run Test Suite**:
  ```cmd
  test.bat
  ```

---

### Command Line Options (`java -jar studyspace.jar`)

You can also run the packaged JAR with custom flags:

```bash
# Launch Desktop GUI + Local REST server (Default)
java -jar studyspace.jar

# Launch Desktop GUI only
java -jar studyspace.jar --gui

# Launch Web Server only on port 8080
java -jar studyspace.jar --web

# Launch Web Server on a custom port
java -jar studyspace.jar --web --port 9090

# View help
java -jar studyspace.jar --help
```

---

### Building with Maven

If you use an IDE (IntelliJ IDEA, Eclipse, VS Code, NetBeans) or Maven CLI:

```bash
# Compile and build JAR
mvn clean package

# Run with Maven
java -jar target/smart-study-planner-1.0.0.jar
```

---

## 📡 REST API Documentation

When the web server is running (`http://localhost:8080`):

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/tasks` | Get all tasks (supports query params: `view`, `status`, `q`) |
| `POST` | `/api/tasks` | Create a new task (`{subject, description, due, priority, type}`) |
| `GET` | `/api/tasks/{id}` | Get task by ID |
| `PUT` | `/api/tasks/{id}` | Update an existing task |
| `POST` | `/api/tasks/{id}/toggle` | Toggle task completion status |
| `DELETE` | `/api/tasks/{id}` | Delete a task |
| `GET` | `/api/stats` | Retrieve planner statistics (`total`, `completed`, `pending`, `percentage`) |
| `GET` | `/` or `/index.html` | Serves the web frontend client |

---

## 💡 Tech Stack & Architecture

- **Language**: Java 17+ / Java 21 (Microsoft OpenJDK)
- **GUI Framework**: Java Swing & AWT (`javax.swing`, `java.awt`) with 2D vector graphics and anti-aliasing
- **Server**: Standard Java HTTP Server (`com.sun.net.httpserver.HttpServer`)
- **Persistence**: File-based JSON storage (`tasks.json`) with atomic replace semantics
- **Design Pattern**: MVC / Clean Architecture (Domain Models, Data Repositories, Business Service Layer, Presentation Layer)
- **Dependencies**: **0 external runtime dependencies** (pure standard Java SE library).
