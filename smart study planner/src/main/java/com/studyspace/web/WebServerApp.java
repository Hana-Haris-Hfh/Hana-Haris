package com.studyspace.web;

import com.studyspace.repository.JsonTaskRepository;
import com.studyspace.service.StudyPlannerService;

import java.awt.Desktop;
import java.net.URI;

/**
 * Standalone runner for the StudySpace Web Server.
 */
public class WebServerApp {

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        JsonTaskRepository repository = new JsonTaskRepository();
        StudyPlannerService service = new StudyPlannerService(repository);
        StudyPlannerWebServer server = new StudyPlannerWebServer(port, service);

        try {
            server.start();
            System.out.println("==================================================");
            System.out.println("  StudySpace Web Application is running!");
            System.out.println("  Open your browser at: http://localhost:" + port);
            System.out.println("  Press Ctrl+C to stop.");
            System.out.println("==================================================");

            // Attempt to automatically open default browser
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + port));
                } catch (Exception ignored) {}
            }

            // Keep process running
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Server stopped.");
        } catch (Exception e) {
            System.err.println("Failed to start web server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
