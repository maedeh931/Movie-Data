package com.student.movieapp.controller;

import com.student.movieapp.model.Movie;
import com.student.movieapp.dao.DatabaseManager;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

// Controls the main movie dashboard screen
public class MainController {

    // Sidebar collection buttons
    @FXML private VBox sidebarCollectionsBox;
    @FXML private Button navAllMovies;
    @FXML private Button navWantToWatch;
    @FXML private Button navWatched;
    @FXML private Button navFavorites;
    @FXML private Button navQuickPicks;
    @FXML private Button navDateNight;
    @FXML private Button navMovieNight;
    @FXML private Button navAwardWinners;

    // Header stats and view switcher buttons
    @FXML private Label categoryTitleLabel;
    @FXML private Label totalMoviesStatLabel;
    @FXML private Label avgRatingStatLabel;
    @FXML private Label watchTimeStatLabel;
    @FXML private Button gridViewToggleBtn;
    @FXML private Button tableViewToggleBtn;
    @FXML private ComboBox<String> sortComboBox;

    // Search bar and filter dropdowns
    @FXML private TextField searchTextField;
    @FXML private ComboBox<String> genreFilterBox;
    @FXML private ComboBox<String> statusFilterBox;
    @FXML private DatePicker dateFilterPicker;

    // Grid and table display areas
    @FXML private StackPane contentStackPane;
    @FXML private ScrollPane gridScrollPane;
    @FXML private FlowPane movieCardsFlowPane;
    @FXML private TableView<Movie> movieTableView;
    @FXML private TableColumn<Movie, String> titleCol;
    @FXML private TableColumn<Movie, String> genreCol;
    @FXML private TableColumn<Movie, Integer> yearCol;
    @FXML private TableColumn<Movie, Double> ratingCol;
    @FXML private TableColumn<Movie, String> statusCol;
    @FXML private TableColumn<Movie, String> dateCol;

    // Stored movie lists and screen tracking variables
    private final ObservableList<Movie> masterMovieList = FXCollections.observableArrayList();
    private FilteredList<Movie> filteredMovieList;
    private SortedList<Movie> sortedMovieList;

    private int nextId = 1;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");
    private String currentCollection = "All Movies";
    private Button activeNavButton;
    private Movie selectedMovie;
    private Node selectedCardNode;
    private boolean isGridView = true;

    // Prevents duplicate screen updates while setting up
    private boolean isInitializing = true;

    // Remembers loaded posters so they don't reload when switching views
    private static final java.util.Map<String, javafx.scene.image.Image> imageCache = new java.util.HashMap<>();

    // Background worker threads for fetching movie posters
    private static final java.util.concurrent.ExecutorService posterExecutor =
            java.util.concurrent.Executors.newFixedThreadPool(4, r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            });

    // Shut down background workers when closing the app
    public static void shutdownPosterExecutor() {
        if (posterExecutor != null && !posterExecutor.isShutdown()) {
            posterExecutor.shutdownNow();
        }
    }

    // Colorful background gradients for cards when no poster image is available
    private static final String[] POSTER_GRADIENTS = {
            "linear-gradient(to bottom right, #8b5cf6, #ec4899)", // Purple to Pink
            "linear-gradient(to bottom right, #06b6d4, #3b82f6)", // Cyan to Ocean Blue
            "linear-gradient(to bottom right, #10b981, #059669)", // Mint to Deep Emerald
            "linear-gradient(to bottom right, #f59e0b, #ef4444)", // Peach / Amber to Red
            "linear-gradient(to bottom right, #6366f1, #a855f7)", // Indigo to Purple
            "linear-gradient(to bottom right, #f43f5e, #fb7185)", // Rose to Coral
            "linear-gradient(to bottom right, #14b8a6, #0ea5e9)", // Teal to Sky
            "linear-gradient(to bottom right, #8e2de2, #4a00e0)", // Royal Violet
            "linear-gradient(to bottom right, #ff0844, #ffb199)", // Sunset Crimson
            "linear-gradient(to bottom right, #11998e, #38ef7d)"  // Cyber Lime
    };

    // Set up the dashboard when the screen first loads
    @FXML
    private void initialize() {
        activeNavButton = navAllMovies;
        isInitializing = true;

        // 1. Configure the table view columns
        setupTableColumns();

        // 2. Set up the dropdown filters and sorting options
        setupComboBoxes();

        // 3. Load movies from the database
        loadDemoData();

        // 4. Hook up live searching and sorting to the table
        filteredMovieList = new FilteredList<>(masterMovieList, p -> true);
        sortedMovieList = new SortedList<>(filteredMovieList);
        sortedMovieList.comparatorProperty().bind(movieTableView.comparatorProperty());
        movieTableView.setItems(sortedMovieList);

        // 5. Update results whenever the user types in the search bar
        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> handleSearchAndFilter());

        // 6. Highlight the matching card when a table row is clicked
        movieTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedMovie = newVal;
                highlightCardForMovie(newVal);
            }
        });

        // 7. Ready for user interaction
        isInitializing = false;

        // 8. Draw the movie cards and show starting collection stats
        renderMovieCards();
        updateStats();

        // 9. Turn on keyboard shortcuts once the window is ready
        contentStackPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupKeyboardShortcuts(newScene);
            }
        });
    }

    // Connect table columns to movie details
    private void setupTableColumns() {
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        yearCol.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));

        // Show yellow stars for ratings
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        ratingCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.format("★ %.1f", item));
                    setStyle("-fx-text-fill: #facc15; -fx-font-weight: 700;");
                }
            }
        });

        // Show colorful status badge pills
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    badge.setText(item);
                    badge.getStyleClass().clear();
                    if ("Watched".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-status-watched");
                    } else if ("Want to Watch".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("badge-status-want");
                    } else {
                        badge.getStyleClass().add("badge-status-watching");
                    }
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });
    }

    // Add options to filter and sort dropdowns
    private void setupComboBoxes() {
        genreFilterBox.setItems(FXCollections.observableArrayList(
                "All Genres", "Sci-Fi", "Action", "Drama", "Comedy",
                "Horror", "Romance", "Thriller", "Animation", "Adventure", "Crime", "Documentary"
        ));
        genreFilterBox.setValue("All Genres");
        genreFilterBox.setOnAction(e -> handleSearchAndFilter());

        statusFilterBox.setItems(FXCollections.observableArrayList(
                "All Statuses", "Want to Watch", "Watching", "Watched"
        ));
        statusFilterBox.setValue("All Statuses");
        statusFilterBox.setOnAction(e -> handleSearchAndFilter());

        dateFilterPicker.setOnAction(e -> handleSearchAndFilter());

        // Fill in sort dropdown options
        sortComboBox.setItems(FXCollections.observableArrayList(
                "Sort by: Recently Added",
                "Sort by: Highest Rating",
                "Sort by: Lowest Rating",
                "Sort by: Title (A-Z)",
                "Sort by: Release Year (Newest)",
                "Sort by: Release Year (Oldest)"
        ));
        sortComboBox.setValue("Sort by: Recently Added");
        sortComboBox.setOnAction(e -> handleSortSelection());
    }

    // Load saved movies from the database into memory
    private void loadDemoData() {
        DatabaseManager.initializeDatabase();
        List<Movie> movies = DatabaseManager.getAllMovies();
        masterMovieList.setAll(movies);
    }

    // Draw the movie cards in the grid view
    private void renderMovieCards() {
        movieCardsFlowPane.getChildren().clear();

        for (Movie movie : filteredMovieList) {
            VBox card = createMovieCard(movie);
            movieCardsFlowPane.getChildren().add(card);
        }
    }

    // Build a single visual card for a movie
    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox();
        card.getStyleClass().add("movie-card");

        // 1. Top poster image area
        StackPane posterBox = new StackPane();
        posterBox.getStyleClass().add("card-poster");

        // Fallback colorful background with the movie initials
        int gradientIndex = Math.abs(movie.getTitle().hashCode()) % POSTER_GRADIENTS.length;
        posterBox.setStyle("-fx-background-color: " + POSTER_GRADIENTS[gradientIndex] + ";");
        Label initialsLabel = new Label(getMovieInitials(movie.getTitle()));
        initialsLabel.getStyleClass().add("card-initials");
        posterBox.getChildren().add(initialsLabel);

        // Round the top corners of the poster
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(175, 190);
        clip.setArcWidth(22);
        clip.setArcHeight(22);
        posterBox.setClip(clip);

        String posterUrl = movie.getPosterUrl();
        if (posterUrl != null && !posterUrl.trim().isEmpty()) {
            String url = posterUrl.trim();

            // Load poster image from cache if available, or fetch in background
            javafx.scene.image.Image cached = imageCache.get(url);
            if (cached != null && !cached.isError()) {
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(cached);
                iv.setFitWidth(175);
                iv.setFitHeight(190);
                posterBox.getChildren().clear();
                posterBox.setStyle("-fx-background-color: transparent;");
                posterBox.getChildren().add(iv);
            } else {
                // Fetch the image on a background worker thread
                posterExecutor.submit(() -> {
                    javafx.scene.image.Image image = new javafx.scene.image.Image(url, 175, 190, false, true, false);
                    if (!image.isError()) {
                        imageCache.put(url, image);
                        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
                        imageView.setFitWidth(175);
                        imageView.setFitHeight(190);

                        javafx.application.Platform.runLater(() -> {
                            posterBox.getChildren().clear();
                            posterBox.setStyle("-fx-background-color: transparent;");
                            posterBox.getChildren().add(imageView);
                        });
                    }
                });
            }
        }

        // 2. Bottom card details area
        VBox footerBox = new VBox();
        footerBox.getStyleClass().add("card-footer");

        // Movie title
        Label titleLabel = new Label(movie.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(false);
        titleLabel.setMaxWidth(155);

        // Rating and release year row
        HBox infoRow = new HBox(6);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        Label ratingLabel = new Label(String.format("★ %.1f", movie.getRating()));
        ratingLabel.getStyleClass().add("card-rating-text");

        Label yearLabel = new Label("• " + movie.getReleaseYear());
        yearLabel.getStyleClass().add("card-year-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status badge
        Label statusBadge = new Label(movie.getStatus().replace("to Watch", "").trim());
        if ("Watched".equalsIgnoreCase(movie.getStatus())) {
            statusBadge.getStyleClass().add("badge-status-watched");
        } else if ("Want to Watch".equalsIgnoreCase(movie.getStatus())) {
            statusBadge.getStyleClass().add("badge-status-want");
        } else {
            statusBadge.getStyleClass().add("badge-status-watching");
        }

        infoRow.getChildren().addAll(ratingLabel, yearLabel, spacer, statusBadge);

        // Genre text
        Label genreLabel = new Label(movie.getGenre());
        genreLabel.getStyleClass().add("card-genre-text");

        footerBox.getChildren().addAll(titleLabel, infoRow, genreLabel);

        card.getChildren().addAll(posterBox, footerBox);

        // Select card on click, or open edit dialog on double click
        card.setOnMouseClicked(event -> {
            selectCard(card, movie);
            if (event.getClickCount() == 2) {
                handleEditMovie();
            }
        });

        // Right-click context menu options
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit Movie");
        editItem.setOnAction(e -> {
            selectCard(card, movie);
            handleEditMovie();
        });

        MenuItem deleteItem = new MenuItem("Delete Movie");
        deleteItem.setOnAction(e -> {
            selectCard(card, movie);
            handleDeleteMovie();
        });

        MenuItem toggleStatusItem = new MenuItem("Watched".equalsIgnoreCase(movie.getStatus()) ? "Mark as Want to Watch" : "Mark as Watched");
        toggleStatusItem.setOnAction(e -> {
            movie.setStatus("Watched".equalsIgnoreCase(movie.getStatus()) ? "Want to Watch" : "Watched");
            DatabaseManager.updateMovie(movie);
            renderMovieCards();
            updateStats();
            movieTableView.refresh();
        });

        contextMenu.getItems().addAll(editItem, toggleStatusItem, new SeparatorMenuItem(), deleteItem);
        card.setOnContextMenuRequested(event -> contextMenu.show(card, event.getScreenX(), event.getScreenY()));

        // Highlight this card if it is currently selected
        if (selectedMovie != null && selectedMovie.getId() == movie.getId()) {
            card.getStyleClass().add("movie-card-selected");
            selectedCardNode = card;
        }

        return card;
    }

    // Generate 2-letter uppercase initials from the movie title
    public static String getMovieInitials(String title) {
        if (title == null || title.trim().isEmpty()) return "CV";
        String clean = title.trim();
        String[] words = clean.split("[\\s:,-]+");
        if (words.length >= 2) {
            if (words[0].equalsIgnoreCase("The") && words.length > 2) {
                return ("" + words[1].charAt(0) + words[2].charAt(0)).toUpperCase();
            }
            return ("" + words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
        } else if (clean.length() >= 2) {
            return clean.substring(0, 2).toUpperCase();
        } else {
            return clean.toUpperCase();
        }
    }

    // Highlight a clicked movie card and select it in the table
    private void selectCard(Node cardNode, Movie movie) {
        if (selectedCardNode != null) {
            selectedCardNode.getStyleClass().remove("movie-card-selected");
        }
        selectedMovie = movie;
        selectedCardNode = cardNode;
        if (cardNode != null && !cardNode.getStyleClass().contains("movie-card-selected")) {
            cardNode.getStyleClass().add("movie-card-selected");
        }
        movieTableView.getSelectionModel().select(movie);
    }

    // Highlight a movie card when selected from the table view
    private void highlightCardForMovie(Movie movie) {
        if (movieCardsFlowPane == null) return;
        for (Node node : movieCardsFlowPane.getChildren()) {
            if (node instanceof VBox) {
                node.getStyleClass().remove("movie-card-selected");
            }
        }
        int index = filteredMovieList.indexOf(movie);
        if (index >= 0 && index < movieCardsFlowPane.getChildren().size()) {
            Node card = movieCardsFlowPane.getChildren().get(index);
            card.getStyleClass().add("movie-card-selected");
            selectedCardNode = card;
        }
    }

    // Filter movies based on the search box, genre, status, and collection
    @FXML
    private void handleSearchAndFilter() {
        if (isInitializing) return;

        String query = searchTextField.getText() != null ? searchTextField.getText().trim().toLowerCase() : "";
        String selectedGenre = genreFilterBox.getValue();
        String selectedStatus = statusFilterBox.getValue();
        LocalDate selectedDate = dateFilterPicker.getValue();

        // Check for special commands typed in the search bar
        if ("add movie".equals(query) || "new movie".equals(query)) {
            return;
        }

        filteredMovieList.setPredicate(movie -> {
            // 1. Filter by selected sidebar collection
            if (!matchCollection(movie, currentCollection)) {
                return false;
            }

            // 2. Filter by search text
            if (!query.isEmpty()) {
                if (query.startsWith("filter sci-fi") || query.equals("sci-fi")) {
                    if (movie.getGenre() == null || !movie.getGenre().toLowerCase().contains("sci-fi")) return false;
                } else if (query.startsWith("filter action") || query.equals("action")) {
                    if (movie.getGenre() == null || !movie.getGenre().toLowerCase().contains("action")) return false;
                } else if (query.startsWith("filter drama") || query.equals("drama")) {
                    if (movie.getGenre() == null || !movie.getGenre().toLowerCase().contains("drama")) return false;
                } else if (query.startsWith("filter watched") || query.equals("watched")) {
                    if (!"Watched".equalsIgnoreCase(movie.getStatus())) return false;
                } else {
                    boolean titleMatch = movie.getTitle() != null && movie.getTitle().toLowerCase().contains(query);
                    boolean genreMatch = movie.getGenre() != null && movie.getGenre().toLowerCase().contains(query);
                    boolean yearMatch = String.valueOf(movie.getReleaseYear()).contains(query);
                    boolean statusMatch = movie.getStatus() != null && movie.getStatus().toLowerCase().contains(query);
                    if (!titleMatch && !genreMatch && !yearMatch && !statusMatch) {
                        return false;
                    }
                }
            }

            // 3. Filter by selected genre
            if (selectedGenre != null && !selectedGenre.isEmpty() && !"All Genres".equalsIgnoreCase(selectedGenre)) {
                if (movie.getGenre() == null || (!movie.getGenre().equalsIgnoreCase(selectedGenre) && !movie.getGenre().toLowerCase().contains(selectedGenre.toLowerCase()))) {
                    return false;
                }
            }

            // 4. Filter by watch status
            if (selectedStatus != null && !selectedStatus.isEmpty() && !"All Statuses".equalsIgnoreCase(selectedStatus)) {
                if (movie.getStatus() == null || !movie.getStatus().equalsIgnoreCase(selectedStatus)) {
                    return false;
                }
            }

            // 5. Filter by date added
            if (selectedDate != null) {
                String targetDateStr = selectedDate.format(dateFormatter);
                if (movie.getDateAdded() == null || !movie.getDateAdded().equals(targetDateStr)) {
                    return false;
                }
            }

            return true;
        });

        // Redraw cards and update stats with the filtered results
        renderMovieCards();
        updateStats();
    }

    // Check if a movie belongs in the currently active sidebar collection
    private boolean matchCollection(Movie movie, String collection) {
        if ("All Movies".equalsIgnoreCase(collection)) return true;
        if ("Want to Watch".equalsIgnoreCase(collection)) return "Want to Watch".equalsIgnoreCase(movie.getStatus());
        if ("Watched".equalsIgnoreCase(collection)) return "Watched".equalsIgnoreCase(movie.getStatus());
        if ("Favorites".equalsIgnoreCase(collection)) return movie.getRating() >= 8.8;
        if ("Quick Picks".equalsIgnoreCase(collection)) return movie.getRating() >= 8.5;
        if ("Date Night".equalsIgnoreCase(collection)) {
            String g = movie.getGenre() != null ? movie.getGenre().toLowerCase() : "";
            return g.contains("drama") || g.contains("romance") || g.contains("comedy") || g.contains("animation");
        }
        if ("Movie Night".equalsIgnoreCase(collection)) {
            String g = movie.getGenre() != null ? movie.getGenre().toLowerCase() : "";
            return g.contains("sci-fi") || g.contains("action") || g.contains("thriller") || g.contains("adventure");
        }
        if ("Award Winners".equalsIgnoreCase(collection)) return movie.getRating() >= 8.5;
        return true;
    }

    // Reorder the movie list based on the chosen sort option
    private void handleSortSelection() {
        if (isInitializing) return;

        String selectedSort = sortComboBox.getValue();
        if (selectedSort == null) return;

        Comparator<Movie> comparator;
        switch (selectedSort) {
            case "Sort by: Highest Rating":
                comparator = Comparator.comparingDouble(Movie::getRating).reversed();
                break;
            case "Sort by: Lowest Rating":
                comparator = Comparator.comparingDouble(Movie::getRating);
                break;
            case "Sort by: Title (A-Z)":
                comparator = Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Sort by: Release Year (Newest)":
                comparator = Comparator.comparingInt(Movie::getReleaseYear).reversed();
                break;
            case "Sort by: Release Year (Oldest)":
                comparator = Comparator.comparingInt(Movie::getReleaseYear);
                break;
            case "Sort by: Recently Added":
            default:
                comparator = Comparator.comparingInt(Movie::getId).reversed();
                break;
        }

        masterMovieList.sort(comparator);
        renderMovieCards();
        movieTableView.refresh();
    }

    // Update total count, average rating, and estimated watch time in the header
    private void updateStats() {
        int count = filteredMovieList.size();
        totalMoviesStatLabel.setText(count + (count == 1 ? " movie" : " movies"));

        if (count > 0) {
            double avg = filteredMovieList.stream().mapToDouble(Movie::getRating).average().orElse(0.0);
            avgRatingStatLabel.setText(String.format("★ %.1f avg rating", avg));

            // Estimate watch time at around 2 hours per film
            int hours = count * 2;
            watchTimeStatLabel.setText("⏱ " + hours + " hours");
        } else {
            avgRatingStatLabel.setText("★ 0.0 avg rating");
            watchTimeStatLabel.setText("⏱ 0 hours");
        }
    }

    // Switch active sidebar collection button and filter the movies
    private void setActiveNav(Button btn, String collectionName) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("sidebar-nav-item-active");
        }
        activeNavButton = btn;
        if (btn != null && !btn.getStyleClass().contains("sidebar-nav-item-active")) {
            btn.getStyleClass().add("sidebar-nav-item-active");
        }
        currentCollection = collectionName;
        categoryTitleLabel.setText(collectionName);
        handleSearchAndFilter();
    }

    // Quick handlers for clicking each sidebar collection
    @FXML private void handleNavAllMovies() { setActiveNav(navAllMovies, "All Movies"); }
    @FXML private void handleNavWantToWatch() { setActiveNav(navWantToWatch, "Want to Watch"); }
    @FXML private void handleNavWatched() { setActiveNav(navWatched, "Watched"); }
    @FXML private void handleNavFavorites() { setActiveNav(navFavorites, "Favorites"); }
    @FXML private void handleNavQuickPicks() { setActiveNav(navQuickPicks, "Quick Picks"); }
    @FXML private void handleNavDateNight() { setActiveNav(navDateNight, "Date Night"); }
    @FXML private void handleNavMovieNight() { setActiveNav(navMovieNight, "Movie Night"); }
    @FXML private void handleNavAwardWinners() { setActiveNav(navAwardWinners, "Award Winners"); }

    // Ask the user for a new collection name and add it to the sidebar
    @FXML
    private void handleNewCollection() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Collection");
        dialog.setHeaderText("Create Collection");
        dialog.setContentText("Collection name:");
        applyDarkThemeToDialog(dialog);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                Button newNavBtn = new Button("◀   " + trimmed);
                newNavBtn.setMaxWidth(Double.MAX_VALUE);
                newNavBtn.setAlignment(Pos.BASELINE_LEFT);
                newNavBtn.getStyleClass().add("sidebar-nav-item");
                newNavBtn.setOnAction(e -> setActiveNav(newNavBtn, trimmed));
                sidebarCollectionsBox.getChildren().add(newNavBtn);
                setActiveNav(newNavBtn, trimmed);
            }
        });
    }

    // Switch to card grid view
    @FXML
    private void handleShowGridView() {
        isGridView = true;
        gridScrollPane.setVisible(true);
        movieTableView.setVisible(false);
        gridViewToggleBtn.getStyleClass().add("view-toggle-btn-active");
        tableViewToggleBtn.getStyleClass().remove("view-toggle-btn-active");
    }

    // Switch to table view
    @FXML
    private void handleShowTableView() {
        isGridView = false;
        gridScrollPane.setVisible(false);
        movieTableView.setVisible(true);
        tableViewToggleBtn.getStyleClass().add("view-toggle-btn-active");
        gridViewToggleBtn.getStyleClass().remove("view-toggle-btn-active");
    }

    // Open dialog to add a new movie
    @FXML
    private void handleAddMovie() {
        Movie tempMovie = new Movie();
        boolean okClicked = showMovieDialog(tempMovie, "Add Movie");
        if (okClicked) {
            int generatedId = DatabaseManager.addMovie(tempMovie);
            if (generatedId > 0) {
                tempMovie.setId(generatedId);
            }
            masterMovieList.add(0, tempMovie);
            handleSearchAndFilter();
            updateStats();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            applyDarkThemeToDialog(alert);
            alert.setTitle("Success");
            alert.setHeaderText("Movie Added");
            alert.setContentText("Movie \"" + tempMovie.getTitle() + "\" was added to Movie Watchlist Manager database.");
            alert.showAndWait();
        }
    }

    // Open dialog to edit the selected movie
    @FXML
    private void handleEditMovie() {
        Movie target = getSelectedMovie();
        if (target != null) {
            String oldUrl = target.getPosterUrl();
            boolean okClicked = showMovieDialog(target, "Edit Movie");
            if (okClicked) {
                // Remove old poster from cache so new one loads fresh
                if (oldUrl != null) imageCache.remove(oldUrl);
                DatabaseManager.updateMovie(target);
                renderMovieCards();
                movieTableView.refresh();
                updateStats();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                applyDarkThemeToDialog(alert);
                alert.setTitle("Success");
                alert.setHeaderText("Movie Updated");
                alert.setContentText("Movie \"" + target.getTitle() + "\" was successfully updated in database.");
                alert.showAndWait();
            }
        } else {
            showNoSelectionWarning("Please select a movie from the grid or table to edit.");
        }
    }

    // Confirm and delete the selected movie
    @FXML
    private void handleDeleteMovie() {
        Movie target = getSelectedMovie();
        if (target != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            applyDarkThemeToDialog(confirmation);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Delete Movie");
            confirmation.setContentText("Are you sure you want to delete this movie from the database?\n\n\"" + target.getTitle() + "\"");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                DatabaseManager.deleteMovie(target.getId());
                masterMovieList.remove(target);
                selectedMovie = null;
                selectedCardNode = null;
                renderMovieCards();
                updateStats();

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                applyDarkThemeToDialog(info);
                info.setTitle("Movie Deleted");
                info.setHeaderText(null);
                info.setContentText("The movie was removed from Movie Watchlist Manager database.");
                info.showAndWait();
            }
        } else {
            showNoSelectionWarning("Please select a movie from the grid or table to delete.");
        }
    }

    // Run the filter check
    @FXML
    private void handleFilter() {
        handleSearchAndFilter();
    }

    // Reset all filters back to default
    @FXML
    private void handleRefresh() {
        isInitializing = true;
        searchTextField.clear();
        genreFilterBox.setValue("All Genres");
        statusFilterBox.setValue("All Statuses");
        dateFilterPicker.setValue(null);
        sortComboBox.setValue("Sort by: Recently Added");
        isInitializing = false;
        setActiveNav(navAllMovies, "All Movies");
    }

    // Get the currently selected movie from either grid or table
    private Movie getSelectedMovie() {
        if (isGridView) {
            return selectedMovie != null ? selectedMovie : movieTableView.getSelectionModel().getSelectedItem();
        } else {
            return movieTableView.getSelectionModel().getSelectedItem() != null ?
                    movieTableView.getSelectionModel().getSelectedItem() : selectedMovie;
        }
    }

    // Show a popup warning if no movie was selected
    private void showNoSelectionWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        applyDarkThemeToDialog(alert);
        alert.setTitle("No Selection");
        alert.setHeaderText("No Movie Selected");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Open the Add/Edit movie popup dialog
    private boolean showMovieDialog(Movie movie, String dialogTitle) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/MovieDialog.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(dialogTitle);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            if (movieTableView.getScene() != null && movieTableView.getScene().getWindow() != null) {
                dialogStage.initOwner(movieTableView.getScene().getWindow());
            }

            Scene scene = new Scene(page);
            scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.sizeToScene();

            MovieDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMovie(movie);

            dialogStage.showAndWait();
            return controller.isSaveClicked();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            applyDarkThemeToDialog(alert);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load dialog");
            alert.setContentText("Unable to open movie form: " + e.getMessage());
            alert.showAndWait();
            return false;
        }
    }

    // Handle keyboard shortcuts (Ctrl+K, Ctrl+N, Ctrl+G, Ctrl+T, Delete, Enter, Escape)
    private void setupKeyboardShortcuts(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() || event.isMetaDown()) {
                if (event.getCode() == KeyCode.K) {
                    searchTextField.requestFocus();
                    searchTextField.selectAll();
                    event.consume();
                } else if (event.getCode() == KeyCode.N) {
                    handleAddMovie();
                    event.consume();
                } else if (event.getCode() == KeyCode.G) {
                    handleShowGridView();
                    event.consume();
                } else if (event.getCode() == KeyCode.T) {
                    handleShowTableView();
                    event.consume();
                }
            } else if (event.getCode() == KeyCode.DELETE) {
                if (!searchTextField.isFocused()) {
                    handleDeleteMovie();
                    event.consume();
                }
            } else if (event.getCode() == KeyCode.ENTER) {
                if (searchTextField.isFocused()) {
                    String query = searchTextField.getText() != null ? searchTextField.getText().trim().toLowerCase() : "";
                    if ("add movie".equals(query) || "new movie".equals(query)) {
                        searchTextField.clear();
                        handleAddMovie();
                        event.consume();
                    } else if ("delete".equals(query)) {
                        searchTextField.clear();
                        handleDeleteMovie();
                        event.consume();
                    }
                } else {
                    handleEditMovie();
                    event.consume();
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                searchTextField.clear();
                movieCardsFlowPane.requestFocus();
                event.consume();
            }
        });
    }

    // Apply dark theme styling to popup alerts and dialogs
    public void applyDarkThemeToDialog(Dialog<?> dialog) {
        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
        pane.getStyleClass().add("movie-dialog");
        Stage stage = (Stage) pane.getScene().getWindow();
        if (stage != null) {
            stage.setTitle(dialog.getTitle());
        }
    }
}
