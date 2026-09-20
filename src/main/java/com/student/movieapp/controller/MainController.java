package com.student.movieapp.controller;

import com.student.movieapp.model.Movie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Main Controller for the Movie Watchlist Manager dashboard.
 * Manages TableView bindings, filtering, modal dialog orchestration,
 * alerts, and demo in-memory data operations.
 */
public class MainController {

    @FXML
    private TableView<Movie> movieTableView;

    @FXML
    private TableColumn<Movie, String> titleCol;

    @FXML
    private TableColumn<Movie, String> genreCol;

    @FXML
    private TableColumn<Movie, Integer> yearCol;

    @FXML
    private TableColumn<Movie, Double> ratingCol;

    @FXML
    private TableColumn<Movie, String> statusCol;

    @FXML
    private TableColumn<Movie, String> dateCol;

    @FXML
    private ComboBox<String> genreFilterBox;

    @FXML
    private ComboBox<String> statusFilterBox;

    @FXML
    private DatePicker dateFilterPicker;

    @FXML
    private Label totalMoviesLabel;

    // Master list holding demo data
    private final ObservableList<Movie> masterMovieList = FXCollections.observableArrayList();

    // Filtered list wrapping master list for search and filter operations
    private FilteredList<Movie> filteredMovieList;

    private int nextId = 1;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

    /**
     * Initializes the controller class. Invoked automatically after FXML loading.
     */
    @FXML
    private void initialize() {
        // 1. Setup Table Columns using PropertyValueFactory
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        yearCol.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));

        // 2. Setup Filter ComboBoxes
        genreFilterBox.setItems(FXCollections.observableArrayList(
                "All Genres", "Sci-Fi", "Action", "Drama", "Comedy",
                "Horror", "Romance", "Thriller", "Animation", "Adventure", "Crime", "Documentary"
        ));
        genreFilterBox.setValue("All Genres");

        statusFilterBox.setItems(FXCollections.observableArrayList(
                "All Statuses", "Want to Watch", "Watching", "Watched"
        ));
        statusFilterBox.setValue("All Statuses");

        // 3. Load initial demo data (matching specification document)
        loadDemoData();

        // 4. Bind FilteredList to TableView
        filteredMovieList = new FilteredList<>(masterMovieList, p -> true);
        movieTableView.setItems(filteredMovieList);

        // 5. Update initial count
        updateTotalCount();
    }

    /**
     * Pre-populates the watchlist with demo movies from the assignment spec.
     */
    private void loadDemoData() {
        masterMovieList.add(new Movie(nextId++, "Interstellar", "Sci-Fi", 2014, 8.7, "Watched", "04/09/26"));
        masterMovieList.add(new Movie(nextId++, "Inception", "Sci-Fi", 2010, 8.8, "Watched", "03/09/26"));
        masterMovieList.add(new Movie(nextId++, "Dune", "Sci-Fi", 2021, 8.0, "Want to Watch", "02/09/26"));
        masterMovieList.add(new Movie(nextId++, "Parasite", "Drama", 2019, 8.5, "Watched", "01/09/26"));
        masterMovieList.add(new Movie(nextId++, "The Dark Knight", "Action", 2008, 9.0, "Watched", "28/08/26"));
    }

    /**
     * Opens the modal dialog to add a new movie.
     */
    @FXML
    private void handleAddMovie() {
        Movie tempMovie = new Movie();
        boolean okClicked = showMovieDialog(tempMovie, "Add Movie");
        if (okClicked) {
            tempMovie.setId(nextId++);
            masterMovieList.add(tempMovie);
            updateTotalCount();

            // Success Alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(movieTableView.getScene().getWindow());
            alert.setTitle("Success");
            alert.setHeaderText("Movie Added");
            alert.setContentText("Movie \"" + tempMovie.getTitle() + "\" was added to your watchlist.");
            alert.showAndWait();
        }
    }

    /**
     * Opens the modal dialog to edit the selected movie.
     */
    @FXML
    private void handleEditMovie() {
        Movie selectedMovie = movieTableView.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            boolean okClicked = showMovieDialog(selectedMovie, "Edit Movie");
            if (okClicked) {
                movieTableView.refresh();

                // Success Alert
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.initOwner(movieTableView.getScene().getWindow());
                alert.setTitle("Success");
                alert.setHeaderText("Movie Updated");
                alert.setContentText("Movie \"" + selectedMovie.getTitle() + "\" was successfully updated.");
                alert.showAndWait();
            }
        } else {
            // Nothing selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(movieTableView.getScene().getWindow());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Movie Selected");
            alert.setContentText("Please select a movie from the table to edit.");
            alert.showAndWait();
        }
    }

    /**
     * Deletes the selected movie with confirmation prompt.
     */
    @FXML
    private void handleDeleteMovie() {
        Movie selectedMovie = movieTableView.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            // Confirmation alert per specification: "Are you sure you want to delete this movie?"
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.initOwner(movieTableView.getScene().getWindow());
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Delete Movie");
            confirmation.setContentText("Are you sure you want to delete this movie?\n\n\"" + selectedMovie.getTitle() + "\"");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                masterMovieList.remove(selectedMovie);
                updateTotalCount();

                // Success notification
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.initOwner(movieTableView.getScene().getWindow());
                info.setTitle("Movie Deleted");
                info.setHeaderText(null);
                info.setContentText("The movie was removed from your watchlist.");
                info.showAndWait();
            }
        } else {
            // Nothing selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(movieTableView.getScene().getWindow());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Movie Selected");
            alert.setContentText("Please select a movie from the table to delete.");
            alert.showAndWait();
        }
    }

    /**
     * Filters movies by Genre, Status, and Date Added.
     */
    @FXML
    private void handleFilter() {
        String selectedGenre = genreFilterBox.getValue();
        String selectedStatus = statusFilterBox.getValue();
        LocalDate selectedDate = dateFilterPicker.getValue();

        filteredMovieList.setPredicate(movie -> {
            // Filter by Genre
            if (selectedGenre != null && !selectedGenre.isEmpty() && !"All Genres".equalsIgnoreCase(selectedGenre)) {
                if (movie.getGenre() == null || !movie.getGenre().equalsIgnoreCase(selectedGenre)) {
                    return false;
                }
            }

            // Filter by Status
            if (selectedStatus != null && !selectedStatus.isEmpty() && !"All Statuses".equalsIgnoreCase(selectedStatus)) {
                if (movie.getStatus() == null || !movie.getStatus().equalsIgnoreCase(selectedStatus)) {
                    return false;
                }
            }

            // Filter by Date Added
            if (selectedDate != null) {
                String targetDateStr = selectedDate.format(dateFormatter);
                if (movie.getDateAdded() == null || !movie.getDateAdded().equals(targetDateStr)) {
                    return false;
                }
            }

            return true;
        });

        updateTotalCount();
    }

    /**
     * Resets all filters and displays the full dataset.
     */
    @FXML
    private void handleRefresh() {
        genreFilterBox.setValue("All Genres");
        statusFilterBox.setValue("All Statuses");
        dateFilterPicker.setValue(null);
        filteredMovieList.setPredicate(p -> true);
        movieTableView.refresh();
        updateTotalCount();
    }

    /**
     * Loads and displays the modal movie dialog (Add / Edit).
     */
    private boolean showMovieDialog(Movie movie, String dialogTitle) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/MovieDialog.fxml"));
            VBox page = loader.load();

            // Create the dialog Stage as APPLICATION_MODAL
            Stage dialogStage = new Stage();
            dialogStage.setTitle(dialogTitle);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(movieTableView.getScene().getWindow());

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            // Pass the movie and stage to the controller
            MovieDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMovie(movie);

            // Show dialog and wait for user response
            dialogStage.showAndWait();

            return controller.isSaveClicked();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(movieTableView.getScene().getWindow());
            alert.setTitle("Error");
            alert.setHeaderText("Could not load dialog");
            alert.setContentText("Unable to open movie form: " + e.getMessage());
            alert.showAndWait();
            return false;
        }
    }

    /**
     * Updates the total movie counter label.
     */
    private void updateTotalCount() {
        int count = filteredMovieList != null ? filteredMovieList.size() : masterMovieList.size();
        totalMoviesLabel.setText("Total Movies: " + count);
    }
}
