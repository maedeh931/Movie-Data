package com.student.movieapp.controller;

import com.student.movieapp.model.Movie;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Controller for the Add/Edit Movie modal dialog.
 * Handles user input, validation, and alerts according to project specifications.
 */
public class MovieDialogController {

    @FXML
    private Label dialogHeaderLabel;

    @FXML
    private TextField titleField;

    @FXML
    private ComboBox<String> genreComboBox;

    @FXML
    private TextField yearField;

    @FXML
    private TextField ratingField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private Stage dialogStage;
    private Movie movie;
    private boolean saveClicked = false;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

    /**
     * Initializes combo box items and default values.
     */
    @FXML
    private void initialize() {
        // Genres supported
        genreComboBox.setItems(FXCollections.observableArrayList(
                "Sci-Fi", "Action", "Drama", "Comedy", "Horror",
                "Romance", "Thriller", "Animation", "Adventure", "Crime", "Documentary"
        ));

        // Watch statuses supported
        statusComboBox.setItems(FXCollections.observableArrayList(
                "Want to Watch", "Watching", "Watched"
        ));

        // Default date is today
        datePicker.setValue(LocalDate.now());
    }

    /**
     * Sets the stage for this modal dialog.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the movie to be edited in the dialog.
     * If movie is null or empty, it behaves as "Add Movie".
     */
    public void setMovie(Movie movie) {
        this.movie = movie;

        if (movie != null && movie.getTitle() != null && !movie.getTitle().isEmpty()) {
            dialogHeaderLabel.setText("Edit Movie");
            titleField.setText(movie.getTitle());
            genreComboBox.setValue(movie.getGenre());
            yearField.setText(String.valueOf(movie.getReleaseYear()));
            ratingField.setText(String.valueOf(movie.getRating()));
            statusComboBox.setValue(movie.getStatus());

            if (movie.getDateAdded() != null && !movie.getDateAdded().isEmpty()) {
                try {
                    datePicker.setValue(LocalDate.parse(movie.getDateAdded(), dateFormatter));
                } catch (DateTimeParseException e) {
                    try {
                        datePicker.setValue(LocalDate.parse(movie.getDateAdded()));
                    } catch (Exception ex) {
                        datePicker.setValue(LocalDate.now());
                    }
                }
            }
        } else {
            dialogHeaderLabel.setText("Add Movie");
            statusComboBox.setValue("Want to Watch");
            datePicker.setValue(LocalDate.now());
        }
    }

    /**
     * Returns true if user clicked Save, false otherwise.
     */
    public boolean isSaveClicked() {
        return saveClicked;
    }

    /**
     * Handles Save button click. Validates all inputs before saving.
     */
    @FXML
    private void handleSave() {
        if (isInputValid()) {
            if (movie == null) {
                movie = new Movie();
            }

            movie.setTitle(titleField.getText().trim());
            movie.setGenre(genreComboBox.getValue());
            movie.setReleaseYear(Integer.parseInt(yearField.getText().trim()));
            movie.setRating(Double.parseDouble(ratingField.getText().trim()));
            movie.setStatus(statusComboBox.getValue());

            if (datePicker.getValue() != null) {
                movie.setDateAdded(datePicker.getValue().format(dateFormatter));
            } else {
                movie.setDateAdded(LocalDate.now().format(dateFormatter));
            }

            saveClicked = true;
            if (dialogStage != null) {
                dialogStage.close();
            }
        }
    }

    /**
     * Handles Cancel button click.
     */
    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /**
     * Validates user input according to assignment requirements:
     * - Required fields: title, genre, release year, rating, status, date
     * - Release year: valid integer between 1888 and 2100
     * - Rating: numeric between 0.0 and 10.0
     */
    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        // Validate title
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errorMessage.append("• Title is required.\n");
        }

        // Validate genre
        if (genreComboBox.getValue() == null || genreComboBox.getValue().trim().isEmpty()) {
            errorMessage.append("• Genre must be selected.\n");
        }

        // Validate release year
        if (yearField.getText() == null || yearField.getText().trim().isEmpty()) {
            errorMessage.append("• Release Year is required.\n");
        } else {
            try {
                int year = Integer.parseInt(yearField.getText().trim());
                if (year < 1888 || year > 2100) {
                    errorMessage.append("• Release Year must be between 1888 and 2100.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("• Release Year must be a valid number (e.g. 2024).\n");
            }
        }

        // Validate rating
        if (ratingField.getText() == null || ratingField.getText().trim().isEmpty()) {
            errorMessage.append("• Rating is required.\n");
        } else {
            try {
                double rating = Double.parseDouble(ratingField.getText().trim());
                if (rating < 0.0 || rating > 10.0) {
                    errorMessage.append("• Rating must be between 0.0 and 10.0.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("• Rating must be a valid decimal number (e.g. 8.5).\n");
            }
        }

        // Validate status
        if (statusComboBox.getValue() == null || statusComboBox.getValue().trim().isEmpty()) {
            errorMessage.append("• Watch status must be selected.\n");
        }

        // Validate date
        if (datePicker.getValue() == null) {
            errorMessage.append("• Date Added must be selected.\n");
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
    }

    public Movie getMovie() {
        return movie;
    }
}
