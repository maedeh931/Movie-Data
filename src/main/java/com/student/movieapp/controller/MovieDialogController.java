package com.student.movieapp.controller;

import com.student.movieapp.model.Movie;
import com.student.movieapp.service.TmdbMovieResult;
import com.student.movieapp.service.TmdbService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Controls the Add and Edit Movie popup window
public class MovieDialogController {

    // Screen inputs and buttons on the popup dialog
    @FXML
    private Label dialogHeaderLabel;

    @FXML
    private TextField titleField;

    @FXML
    private Button searchTmdbButton;

    @FXML
    private Label tmdbStatusLabel;

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
    private TextField posterUrlField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    // Window and data state
    private Stage dialogStage;
    private Movie movie;
    private boolean saveClicked = false;

    // Background workers and helpers
    private final TmdbService tmdbService = new TmdbService();
    private static final ExecutorService TMDB_EXECUTOR = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "tmdb-search-thread");
        t.setDaemon(true);
        return t;
    });

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

    // Fill in starting options when the dialog opens
    @FXML
    private void initialize() {
        // Add standard movie genres to the dropdown
        genreComboBox.setItems(FXCollections.observableArrayList(
                "Sci-Fi", "Action", "Drama", "Comedy", "Horror",
                "Romance", "Thriller", "Animation", "Adventure", "Crime", "Documentary"
        ));

        // Add watch status choices to the dropdown
        statusComboBox.setItems(FXCollections.observableArrayList(
                "Want to Watch", "Watching", "Watched"
        ));

        // Set today's date as default
        datePicker.setValue(LocalDate.now());

        // Let the user hit Enter in the title box to search TMDB
        titleField.setOnAction(e -> handleSearchTmdb());
    }

    // Connect this controller to the dialog window
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Populate the form fields with movie details
    public void setMovie(Movie movie) {
        this.movie = movie;
        resetTmdbStatus();

        // If editing an existing movie, fill in all its details
        if (movie != null && movie.getTitle() != null && !movie.getTitle().isEmpty()) {
            dialogHeaderLabel.setText("Edit Movie");
            titleField.setText(movie.getTitle());
            if (movie.getGenre() != null && !movie.getGenre().isBlank()) {
                if (!genreComboBox.getItems().contains(movie.getGenre())) {
                    genreComboBox.getItems().add(movie.getGenre());
                }
                genreComboBox.setValue(movie.getGenre());
            }
            yearField.setText(String.valueOf(movie.getReleaseYear()));
            ratingField.setText(String.valueOf(movie.getRating()));
            statusComboBox.setValue(movie.getStatus());
            posterUrlField.setText(movie.getPosterUrl() == null ? "" : movie.getPosterUrl());

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
            // If adding a new movie, reset fields to fresh defaults
            dialogHeaderLabel.setText("Add Movie");
            statusComboBox.setValue("Want to Watch");
            datePicker.setValue(LocalDate.now());
            posterUrlField.setText("");
        }
    }

    // Search TMDB online when the user clicks Search TMDB
    @FXML
    private void handleSearchTmdb() {
        // Make sure a title was entered first
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        if (title.isEmpty()) {
            showTmdbStatus("⚠ Please enter a movie title to search.", "#f87171");
            return;
        }

        // Disable the search button and show searching status
        if (searchTmdbButton != null) {
            searchTmdbButton.setDisable(true);
        }
        showTmdbStatus("Searching TMDB for \"" + title + "\"...", "#818cf8");

        // Run the web search on a background worker thread
        TMDB_EXECUTOR.submit(() -> {
            try {
                Optional<TmdbMovieResult> resultOpt = tmdbService.searchMovie(title);
                Platform.runLater(() -> {
                    if (searchTmdbButton != null) {
                        searchTmdbButton.setDisable(false);
                    }
                    // If a movie was found, fill in its poster, year, rating, and genre
                    if (resultOpt.isPresent()) {
                        TmdbMovieResult result = resultOpt.get();
                        if (result.posterUrl() != null && !result.posterUrl().isBlank()) {
                            posterUrlField.setText(result.posterUrl());
                        }
                        if (result.releaseYear() != null) {
                            yearField.setText(String.valueOf(result.releaseYear()));
                        }
                        if (result.rating() != null) {
                            ratingField.setText(String.valueOf(result.rating()));
                        }
                        if (result.genre() != null && genreComboBox.getItems().contains(result.genre())) {
                            genreComboBox.setValue(result.genre());
                        }
                        showTmdbStatus("✓ Matched: " + result.title() + (result.releaseYear() != null ? " (" + result.releaseYear() + ")" : ""), "#34d399");
                    } else {
                        // If no movie was found, let the user know gently
                        showTmdbStatus("⚠ No match found on TMDB. You can enter details manually.", "#fbbf24");
                    }
                });
            } catch (Exception ex) {
                // Show an error message if something went wrong with the search
                Platform.runLater(() -> {
                    if (searchTmdbButton != null) {
                        searchTmdbButton.setDisable(false);
                    }
                    String msg = ex.getMessage();
                    if (msg != null && msg.contains("API key")) {
                        showTmdbStatus("⚠ TMDB API key missing in config.properties.", "#f87171");
                    } else {
                        showTmdbStatus("⚠ Search failed (" + (msg != null ? msg : "network error") + "). Enter details manually.", "#f87171");
                    }
                });
            }
        });
    }

    // Show a colored status message under the title box
    private void showTmdbStatus(String message, String colorHex) {
        if (tmdbStatusLabel != null) {
            tmdbStatusLabel.setText(message);
            tmdbStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + colorHex + ";");
            tmdbStatusLabel.setVisible(true);
            tmdbStatusLabel.setManaged(true);
        }
    }

    // Hide the status message
    private void resetTmdbStatus() {
        if (tmdbStatusLabel != null) {
            tmdbStatusLabel.setText("");
            tmdbStatusLabel.setVisible(false);
            tmdbStatusLabel.setManaged(false);
        }
    }

    // Check whether the user clicked Save or Cancel
    public boolean isSaveClicked() {
        return saveClicked;
    }

    // Save the movie if all required fields are filled out correctly
    @FXML
    private void handleSave() {
        if (isInputValid()) {
            if (movie == null) {
                movie = new Movie();
            }

            // Transfer the form inputs into the movie object
            movie.setTitle(titleField.getText().trim());
            movie.setGenre(genreComboBox.getValue());
            movie.setReleaseYear(Integer.parseInt(yearField.getText().trim()));
            movie.setRating(Double.parseDouble(ratingField.getText().trim()));
            movie.setStatus(statusComboBox.getValue());
            movie.setPosterUrl(posterUrlField.getText() != null ? posterUrlField.getText().trim() : "");

            if (datePicker.getValue() != null) {
                movie.setDateAdded(datePicker.getValue().format(dateFormatter));
            } else {
                movie.setDateAdded(LocalDate.now().format(dateFormatter));
            }

            saveClicked = true;

            // Close the popup window
            if (dialogStage != null) {
                dialogStage.close();
            }
        }
    }

    // Close the popup without saving
    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    // Check that the user filled in all required fields properly
    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        // Check the title
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errorMessage.append("• Title is required.\n");
        }

        // Check the genre
        if (genreComboBox.getValue() == null || genreComboBox.getValue().trim().isEmpty()) {
            errorMessage.append("• Genre must be selected.\n");
        }

        // Check the release year (must be between 1888 and 2100)
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

        // Check the rating (must be between 0.0 and 10.0)
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

        // Check the watch status
        if (statusComboBox.getValue() == null || statusComboBox.getValue().trim().isEmpty()) {
            errorMessage.append("• Watch status must be selected.\n");
        }

        // Check the date
        if (datePicker.getValue() == null) {
            errorMessage.append("• Date Added must be selected.\n");
        }

        // If no errors, we are good to go
        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show an alert popup if anything was entered incorrectly
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errorMessage.toString());

            DialogPane pane = alert.getDialogPane();
            pane.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            pane.getStyleClass().add("cinevault-dialog");

            alert.showAndWait();
            return false;
        }
    }

    // Return the saved movie
    public Movie getMovie() {
        return movie;
    }
}
