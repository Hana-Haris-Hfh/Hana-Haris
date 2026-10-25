package com.studyspace;

import com.studyspace.gui.StudyPlannerApp;
import com.studyspace.repository.JsonTaskRepository;
import com.studyspace.service.StudyPlannerService;
import com.studyspace.web.StudyPlannerWebServer;

import javax.swing.*;
import java.awt.*;

/**
 * Main application launcher for StudySpace.
 * Supports launching the Modern Desktop GUI, the Web Server, or both simultaneously.
 */
public class Main {

    public static void main(String[] args) {
        boolean webOnly = false;
        boolean guiOnly = false;
        int port = 8080;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            if ("--web".equals(arg)) {
                webOnly = true;
            } else if ("--gui".equals(arg)) {
                guiOnly = true;
            } else if ("--port".equals(arg) && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[++i]);
                } catch (NumberFormatException ignored) {}
            } else if ("--help".equals(arg) || "-h".equals(arg)) {
                printHelp();
                return;
            }
        }

        // Shared data repository and service
        JsonTaskRepository repository = new JsonTaskRepository();
        StudyPlannerService service = new StudyPlannerService(repository);

        if (webOnly) {
            startWebServer(service, port, false);
            return;
        }

        // Configure high-DPI and modern Swing appearance
        configureSwingEnvironment();

        if (guiOnly) {
            launchGui(service);
        } else {
            // Default mode: Launch Desktop GUI AND start local background Web/REST API
            try {
                StudyPlannerWebServer webServer = new StudyPlannerWebServer(port, service);
                webServer.start();
                System.out.println("Local REST server running at http://localhost:" + port);
            } catch (Exception e) {
                System.out.println("Info: Web server could not bind to port " + port + " (" + e.getMessage() + "), continuing in Desktop GUI mode.");
            }

            launchGui(service);
        }
    }

    private static void launchGui(StudyPlannerService service) {
        SwingUtilities.invokeLater(() -> {
            try {
                StudyPlannerApp app = new StudyPlannerApp(service);
                app.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error displaying GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static void startWebServer(StudyPlannerService service, int port, boolean autoOpen) {
        try {
            StudyPlannerWebServer server = new StudyPlannerWebServer(port, service);
            server.start();
            System.out.println("==================================================");
            System.out.println("  StudySpace Web Application is running!");
            System.out.println("  Open your browser at: http://localhost:" + port);
            System.out.println("  Press Ctrl+C to stop.");
            System.out.println("==================================================");

            if (autoOpen && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI("http://localhost:" + port));
                } catch (Exception ignored) {}
            }

            // Keep main thread alive for web-only execution
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Server stopped.");
        } catch (Exception e) {
            System.err.println("Failed to start web server: " + e.getMessage());
        }
    }

    private static void configureSwingEnvironment() {
        // Enable anti-aliasing and modern font rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    private static void printHelp() {
        System.out.println("StudySpace - Smart Study Planner");
        System.out.println("Usage:");
        System.out.println("  java -jar studyspace.jar              Launch Desktop GUI + Web REST Server");
        System.out.println("  java -jar studyspace.jar --gui        Launch Desktop GUI only");
        System.out.println("  java -jar studyspace.jar --web        Launch Web Server only");
        System.out.println("  java -jar studyspace.jar --port 9090  Custom web server port");
        System.out.println("  java -jar studyspace.jar --help       Show this help message");
    }
}
