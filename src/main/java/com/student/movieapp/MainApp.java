package com.student.movieapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application entry point for the Movie Watchlist Manager.
 * Loads the primary stage, main view FXML, and application stylesheet.
 */
public class MainApp extends Application {

    static {
        System.setProperty("http.agent", "Mozilla/5.0");
    }

    @Override
    public void init() {
        System.setProperty("http.agent", "Mozilla/5.0");
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1180, 780);
            
            // Apply stylesheet
            String cssPath = getClass().getResource("/styles/application.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            primaryStage.setTitle("CineVault — Movie Tracker & Vault");
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(650);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Failed to initialize application UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Standard main method to launch JavaFX application.
     */
    public static void main(String[] args) {
        // Ensure User-Agent is set before JavaFX launches
        System.setProperty("http.agent", "Mozilla/5.0");
        launch(args);
    }
}
