package com.student.movieapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

// Controls the login screen where users sign in
public class LoginController {

    // Inputs on the login screen
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    // Check if the login info is correct when the user clicks Sign In
    @FXML
    private void handleLogin() {
        // Read what the user typed in both fields
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";

        // Check if the credentials match the demo login (admin / admin)
        if ("admin".equalsIgnoreCase(username) && "admin".equals(password)) {
            openMainDashboard();
        } else {
            showLoginErrorAlert("Invalid username or password. Please try again.\n\nHint: Use admin / admin for demo access.");
        }
    }

    // Switch to the main dashboard screen
    private void openMainDashboard() {
        try {
            // Find the current window
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Load the main dashboard view from FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
            Parent root = loader.load();

            // Set up the scene with standard desktop dimensions
            Scene scene = new Scene(root, 1180, 780);
            String cssPath = getClass().getResource("/styles/application.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            // Configure window properties and show the dashboard
            stage.setTitle("Movie Watchlist Manager");
            stage.setMinWidth(1000);
            stage.setMinHeight(650);
            stage.setResizable(true);
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            // Show a popup error if the dashboard could not be loaded
            e.printStackTrace();
            showLoginErrorAlert("Failed to load Main Dashboard: " + e.getMessage());
        }
    }

    // Show a popup message explaining why login failed
    private void showLoginErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Authentication Error");
        alert.setHeaderText("Sign In Failed");
        alert.setContentText(message);

        // Apply dark styling to the alert popup
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
        pane.getStyleClass().add("movie-dialog");

        alert.showAndWait();
    }
}
