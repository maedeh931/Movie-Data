package com.student.movieapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the CineVault Login Screen.
 * Demonstrates JavaFX PasswordField control and authentication validation.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    /**
     * Handles Login button click.
     * Performs demo authentication check (admin / admin).
     * On success, opens MainView.fxml; on failure, displays error alert.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";

        // Demo-level authentication check (admin / admin)
        if ("admin".equalsIgnoreCase(username) && "admin".equals(password)) {
            openMainDashboard();
        } else {
            showLoginErrorAlert("Invalid username or password. Please try again.\n\nHint: Use admin / admin for demo access.");
        }
    }

    /**
     * Loads and displays the main movie dashboard (MainView.fxml).
     */
    private void openMainDashboard() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1180, 780);
            String cssPath = getClass().getResource("/styles/application.css").toExternalForm();
            scene.getStylesheets().add(cssPath);

            stage.setTitle("CineVault — Movie Tracker & Vault");
            stage.setMinWidth(1000);
            stage.setMinHeight(650);
            stage.setResizable(true);
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showLoginErrorAlert("Failed to load Main Dashboard: " + e.getMessage());
        }
    }

    /**
     * Displays a dark-themed error alert dialog on authentication failure.
     */
    private void showLoginErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Authentication Error");
        alert.setHeaderText("Sign In Failed");
        alert.setContentText(message);

        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
        pane.getStyleClass().add("cinevault-dialog");

        alert.showAndWait();
    }
}
