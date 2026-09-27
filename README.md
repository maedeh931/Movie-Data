# CineVault — Modern Movie Tracker & Watchlist

CineVault is a dark-mode desktop movie tracker built in Java following the Model-View-Controller (MVC) architecture. It lets you organize a personal movie collection with ratings, release years, and watch statuses, calculate real-time stats, and fetch official movie posters live from The Movie Database (TMDB) API.

---

## How to Run the App (Quick Demo Guide)

- **Option 1 (IntelliJ IDEA — Recommended)**:
  1. Open the project in IntelliJ IDEA.
  2. Select the **`Run Application`** configuration from the top toolbar.
  3. Click the green **Run** button (or press `Shift + F10`).
- **Option 2 (One-Click Script)**:
  - Double-click [`run.bat`](file:///c:/Users/Madiha_Rahman/Movie-Data/run.bat) in the project root folder.
- **Demo Login Credentials**:
  - **Username**: `admin`
  - **Password**: `admin`

---

## Feature Breakdown (For Presentation & Demo)

### 1. Advanced OOP Concepts
- **What it is**: The project uses core Object-Oriented principles. Encapsulation protects movie data inside JavaFX property fields with getters/setters (`Movie.java`). Inheritance is demonstrated by extending JavaFX's `Application` (`MainApp.java`). It also implements the DAO pattern to isolate database code (`DatabaseManager.java`) and the DTO pattern for clean JSON data transfer (`DatabaseManager.MovieDTO` and `TmdbMovieResult`).
- **Where to show in code**:
  - Encapsulation & Properties: [`Movie.java`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/java/com/student/movieapp/model/Movie.java)
  - Inheritance: [`MainApp.java`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/java/com/student/movieapp/MainApp.java) (`extends Application`)
  - DAO Pattern & DTO: [`DatabaseManager.java`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/java/com/student/movieapp/dao/DatabaseManager.java) (`class DatabaseManager` & `class MovieDTO`)

### 2. JavaFX UI Design
- **What it is**: The interface is designed in Scene Builder using FXML and styled with custom dark-mode CSS. It uses diverse layout panes (`BorderPane`, `VBox`, `HBox`, `GridPane`, `FlowPane`, `ScrollPane`, `StackPane`) and controls (`TableView`, `ComboBox`, `DatePicker`, `TextField`, `PasswordField`, `Button`). Includes a dedicated login screen with password masking before entering the dashboard.
- **Where to show in code**:
  - Login View & PasswordField: [`LoginView.fxml`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/resources/views/LoginView.fxml) & [`LoginController.java`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/java/com/student/movieapp/controller/LoginController.java)
  - Main Dashboard Layout: [`MainView.fxml`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/resources/views/MainView.fxml)
  - Add/Edit Dialog Layout: [`MovieDialog.fxml`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/resources/views/MovieDialog.fxml)
  - CSS Theme: [`application.css`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/resources/styles/application.css)

### 3. Layout Responsiveness
- **What it is**: The layout dynamically adapts when resizing the window. In Grid View, movie cards are contained in a `FlowPane` wrapped inside a `ScrollPane`, automatically rearranging into more or fewer columns depending on window width. Table columns and search bars expand proportionally using `HBox.hgrow="ALWAYS"` and percentage constraints.
- **Where to show in code**:
  - FXML FlowPane & ScrollPane setup: [`MainView.fxml`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/resources/views/MainView.fxml) (`gridScrollPane` & `movieCardsFlowPane`)
  - Dynamic Card Re-rendering: [`MainController.java`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/java/com/student/movieapp/controller/MainController.java) (`renderMovieCards()`)

### 4. Concurrency (Multithreading)
- **What it is**: Network calls and image downloads never block the JavaFX Application Thread, keeping animations and interactions smooth. Movie poster downloads and TMDB API searches run on background daemon threads via `ExecutorService` thread pools, and UI updates safely return to the main thread with `Platform.runLater()`.
- **Where to show in code**:
  - Background Poster Fetching: [`MainController.java`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/java/com/student/movieapp/controller/MainController.java) (`posterExecutor`, `createMovieCard()`)
  - Background TMDB Search: [`MovieDialogController.java`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/java/com/student/movieapp/controller/MovieDialogController.java) (`TMDB_EXECUTOR`, `handleSearchTmdb()`)

### 5. Database Integration (Relational Schema)
- **What it is**: Data persists in a local SQLite database (`cinevault.db`). It features a normalized relational database design with three tables: `movies`, `genres` (unique genre dictionary), and `movie_genres` (a many-to-many join table with foreign keys and cascade delete). This allows movies to belong to multiple genres simultaneously (e.g. "Action, Sci-Fi").
- **Where to show in code**:
  - Database Schema & Tables: [`DatabaseManager.java`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/java/com/student/movieapp/dao/DatabaseManager.java) (`initializeDatabase()`)
  - Default Genre Seeding: [`DatabaseManager.java`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/java/com/student/movieapp/dao/DatabaseManager.java) (`seedDefaultGenres()`)
  - Genre Join Table Syncing: [`DatabaseManager.java`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/java/com/student/movieapp/dao/DatabaseManager.java) (`syncMovieGenres()`)

### 6. CRUD Operations
- **What it is**: Complete Create, Read, Update, and Delete operations for movies and their genre links. Users can add new films, view/filter the collection, edit any attribute, and delete movies with immediate database persistence and table/grid UI refreshes.
- **Where to show in code**:
  - Database CRUD: [`DatabaseManager.java`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/java/com/student/movieapp/dao/DatabaseManager.java) (`addMovie()`, `getAllMovies()`, `updateMovie()`, `deleteMovie()`)
  - UI Event Handlers: [`MainController.java`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/java/com/student/movieapp/controller/MainController.java) (`handleAddMovie()`, `handleEditMovie()`, `handleDeleteMovie()`)

### 7. Networking & Data Parsing (TMDB API)
- **What it is**: The Add Movie dialog connects live to The Movie Database (TMDB) REST API via `java.net.http.HttpClient`. Typing a title and clicking "Search TMDB" queries the online API, parses the response with Jackson `ObjectMapper`, and automatically populates the poster image URL, release year, rating, and genre. The API key is kept secure in an uncommitted local properties file.
- **Where to show in code**:
  - HTTP Client & Jackson Parsing: [`TmdbService.java`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/java/com/student/movieapp/service/TmdbService.java) (`searchMovie()`, `loadApiKey()`)
  - Auto-fill UI Action: [`MovieDialogController.java`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/java/com/student/movieapp/controller/MovieDialogController.java) (`handleSearchTmdb()`)
  - Config Example: [`config.properties.example`](file:///c:/Users/Madiha_Rahman/Movie-Data/src/main/resources/config.properties.example)

---

## Tech Stack

- **Language**: Java 17+
- **GUI Framework**: JavaFX 21 (FXML, Scene Builder, CSS)
- **Database**: SQLite & SQLite-JDBC (with many-to-many join table)
- **JSON Parsing**: Jackson Databind (`com.fasterxml.jackson.core`)
- **Networking**: Java HTTP Client (`java.net.http.HttpClient`) + The Movie Database (TMDB) REST API
- **Build Tool**: Apache Maven

---

## Project Structure

```
Movie-Data/
├── pom.xml                                   # Maven dependencies & JavaFX compiler configuration
├── run.bat                                   # One-click startup script
├── .gitignore                                # Git ignore rules (protects API keys and local DB)
└── src/main/
    ├── java/
    │   ├── module-info.java                  # JavaFX module definitions and requirements
    │   └── com/student/movieapp/
    │       ├── MainApp.java                  # Main application entry point & window lifecycle
    │       ├── AppLauncher.java              # Fallback launcher for classpath execution
    │       ├── controller/                   # Handles user interactions and UI logic
    │       │   ├── LoginController.java      # Login screen & password verification
    │       │   ├── MainController.java       # Dashboard controller (search, sort, grid/table view)
    │       │   └── MovieDialogController.java# Add/Edit popup & TMDB search auto-fill
    │       ├── dao/                          # Data Access Layer
    │       │   └── DatabaseManager.java      # SQLite connection, schema, and CRUD methods
    │       ├── model/                        # Data entities
    │       │   └── Movie.java                # Movie model with observable JavaFX properties
    │       └── service/                      # External services & networking
    │           ├── TmdbService.java          # TMDB REST API client & key loader
    │           └── TmdbMovieResult.java      # Movie search result record
    └── resources/
        ├── config.properties.example         # Template for TMDB API key
        ├── data/movies.json                  # Sample starting movie dataset
        ├── styles/application.css            # CineVault dark theme stylesheet
        └── views/                            # Scene Builder FXML layouts
            ├── LoginView.fxml                # Login window layout
            ├── MainView.fxml                 # Main dashboard layout
            └── MovieDialog.fxml              # Add/Edit popup layout
```
