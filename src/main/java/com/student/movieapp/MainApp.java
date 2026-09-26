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

    @Override
    public void init() {
        // Set standard User-Agent header so JavaFX Image can load web posters from CDN URLs without 403 Forbidden
        System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
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
        System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        launch(args);
    }
}
