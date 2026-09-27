package com.student.movieapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

// Main starting point of the CineVault desktop app
public class MainApp extends Application {

    // Make sure web requests look like a normal browser
    static {
        System.setProperty("http.agent", "Mozilla/5.0");
    }

    // Set the browser header when the app initializes
    @Override
    public void init() {
        System.setProperty("http.agent", "Mozilla/5.0");
    }

    // Build and show the login window
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the login screen layout from the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Parent root = loader.load();

            // Create the window scene with custom width and height
            Scene scene = new Scene(root, 440, 480);

            // Apply the dark theme styles
            String cssPath = getClass().getResource("/styles/application.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            // Set up the window title and display it on screen
            primaryStage.setTitle("CineVault — Sign In");
            primaryStage.setResizable(false);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            // Print an error if the login screen fails to load
            System.err.println("Failed to initialize login UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Clean up background worker threads when closing the app
    @Override
    public void stop() throws Exception {
        super.stop();
        com.student.movieapp.controller.MainController.shutdownPosterExecutor();
    }

    // Start the application
    public static void main(String[] args) {
        System.setProperty("http.agent", "Mozilla/5.0");
        launch(args);
    }
}
