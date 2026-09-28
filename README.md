# Movie Watchlist Manager

Movie Watchlist Manager is a dark-mode desktop movie tracker built in Java following the Model-View-Controller (MVC) architecture. It lets you organize a personal movie collection with ratings, release years, and watch statuses, calculate real-time stats, and fetch official movie posters live from The Movie Database (TMDB) API.

---

## How to Run the App (Quick Demo Guide)

- **Option 1 (IntelliJ IDEA — Recommended)**:
  1. Open the project in IntelliJ IDEA.
  2. Select the **`Run Application`** configuration from the top toolbar.
  3. Click the green **Run** button (or press `Shift + F10`).
- **Option 2 (One-Click Script)**:
  - Double-click [`run.bat`](run.bat) in the project root folder.
- **Demo Login Credentials**:
  - **Username**: `admin`
  - **Password**: `admin`

---

## Feature-to-Code Quick Reference

| Feature | Primary Code Files & Locations | Key Classes & Methods |
| :--- | :--- | :--- |
| **Advanced OOP** | [`Movie.java`](src/main/java/com/student/movieapp/model/Movie.java)<br>[`MainApp.java`](src/main/java/com/student/movieapp/MainApp.java)<br>[`DatabaseManager.java`](src/main/java/com/student/movieapp/dao/DatabaseManager.java)<br>[`TmdbMovieResult.java`](src/main/java/com/student/movieapp/service/TmdbMovieResult.java) | Encapsulation (`StringProperty`, `DoubleProperty`)<br>Inheritance (`extends Application`)<br>DAO & DTO patterns (`DatabaseManager`, `MovieDTO`, `TmdbMovieResult`) |
| **JavaFX UI Design** | [`LoginView.fxml`](src/main/resources/views/LoginView.fxml)<br>[`LoginController.java`](src/main/java/com/student/movieapp/controller/LoginController.java)<br>[`MainView.fxml`](src/main/resources/views/MainView.fxml)<br>[`MovieDialog.fxml`](src/main/resources/views/MovieDialog.fxml)<br>[`application.css`](src/main/resources/styles/application.css) | `PasswordField` masked auth (`handleLogin()`)<br>`BorderPane`, `GridPane`, `TableView`, `ComboBox`<br>Custom dark CSS theme styling |
| **Layout Responsiveness** | [`MainView.fxml`](src/main/resources/views/MainView.fxml)<br>[`MainController.java`](src/main/java/com/student/movieapp/controller/MainController.java) | `FlowPane` inside `ScrollPane`<br>Auto-reflowing card columns (`renderMovieCards()`)<br>Proportional sizing (`HBox.hgrow="ALWAYS"`) |
| **Concurrency & Multithreading** | [`MainController.java`](src/main/java/com/student/movieapp/controller/MainController.java)<br>[`MovieDialogController.java`](src/main/java/com/student/movieapp/controller/MovieDialogController.java) | Thread pool `posterExecutor` (`createMovieCard()`)<br>Thread pool `TMDB_EXECUTOR` (`handleSearchTmdb()`)<br>UI sync via `Platform.runLater()` |
| **Database Integration** | [`DatabaseManager.java`](src/main/java/com/student/movieapp/dao/DatabaseManager.java) | SQLite schema setup (`initializeDatabase()`)<br>Normalized `movies`, `genres`, `movie_genres`<br>Genre join sync (`syncMovieGenres()`, `seedDefaultGenres()`) |
| **CRUD Operations** | [`DatabaseManager.java`](src/main/java/com/student/movieapp/dao/DatabaseManager.java)<br>[`MainController.java`](src/main/java/com/student/movieapp/controller/MainController.java)<br>[`MovieDialogController.java`](src/main/java/com/student/movieapp/controller/MovieDialogController.java) | DB CRUD (`addMovie()`, `getAllMovies()`, `updateMovie()`, `deleteMovie()`)<br>UI Actions (`handleAddMovie()`, `handleEditMovie()`, `handleDeleteMovie()`) |
| **Networking & TMDB API** | [`TmdbService.java`](src/main/java/com/student/movieapp/service/TmdbService.java)<br>[`MovieDialogController.java`](src/main/java/com/student/movieapp/controller/MovieDialogController.java)<br>[`config.properties.example`](src/main/resources/config.properties.example) | `HttpClient` REST API calls (`searchMovie()`)<br>Jackson JSON deserialization (`ObjectMapper`)<br>Live auto-fill (`handleSearchTmdb()`) |

---

## Detailed Feature Breakdown (For Presentation & Demo)

### 1. Advanced OOP Concepts
- **What it is**: The project uses core Object-Oriented principles. Encapsulation protects movie data inside JavaFX property fields with getters/setters (`Movie.java`). Inheritance is demonstrated by extending JavaFX's `Application` (`MainApp.java`). It also implements the DAO pattern to isolate database code (`DatabaseManager.java`) and the DTO pattern for clean JSON data transfer (`DatabaseManager.MovieDTO` and `TmdbMovieResult`).
- **Where to show in code**:
  - Encapsulation & Observable Properties: [`Movie.java`](src/main/java/com/student/movieapp/model/Movie.java)
  - Class Inheritance & Window Lifecycle: [`MainApp.java`](src/main/java/com/student/movieapp/MainApp.java) (`extends Application`)
  - DAO Pattern & DTO Helpers: [`DatabaseManager.java`](src/main/java/com/student/movieapp/dao/DatabaseManager.java) (`class DatabaseManager` & `class MovieDTO`) and [`TmdbMovieResult.java`](src/main/java/com/student/movieapp/service/TmdbMovieResult.java)

### 2. JavaFX UI Design
- **What it is**: The interface is designed in Scene Builder using FXML and styled with custom dark-mode CSS. It uses diverse layout panes (`BorderPane`, `VBox`, `HBox`, `GridPane`, `FlowPane`, `ScrollPane`, `StackPane`) and controls (`TableView`, `ComboBox`, `DatePicker`, `TextField`, `PasswordField`, `Button`). Includes a dedicated login screen with password masking before entering the dashboard.
- **Where to show in code**:
  - Login View & PasswordField: [`LoginView.fxml`](src/main/resources/views/LoginView.fxml) & [`LoginController.java`](src/main/java/com/student/movieapp/controller/LoginController.java)
  - Main Dashboard Layout: [`MainView.fxml`](src/main/resources/views/MainView.fxml)
  - Add/Edit Dialog Layout: [`MovieDialog.fxml`](src/main/resources/views/MovieDialog.fxml)
  - Dark Theme Stylesheet: [`application.css`](src/main/resources/styles/application.css)

### 3. Layout Responsiveness
- **What it is**: The layout dynamically adapts when resizing the window. In Grid View, movie cards are contained in a `FlowPane` wrapped inside a `ScrollPane`, automatically rearranging into more or fewer columns depending on window width. Table columns and search bars expand proportionally using `HBox.hgrow="ALWAYS"` and percentage constraints.
- **Where to show in code**:
  - FXML FlowPane & ScrollPane setup: [`MainView.fxml`](src/main/resources/views/MainView.fxml) (`gridScrollPane` & `movieCardsFlowPane`)
  - Dynamic Card Re-rendering: [`MainController.java`](src/main/java/com/student/movieapp/controller/MainController.java) (`renderMovieCards()`)

### 4. Concurrency (Multithreading)
- **What it is**: Network calls and image downloads never block the JavaFX Application Thread, keeping animations and interactions smooth. Movie poster downloads and TMDB API searches run on background daemon threads via `ExecutorService` thread pools, and UI updates safely return to the main thread with `Platform.runLater()`.
- **Where to show in code**:
  - Background Poster Fetching: [`MainController.java`](src/main/java/com/student/movieapp/controller/MainController.java) (`posterExecutor`, `createMovieCard()`)
  - Background TMDB Search: [`MovieDialogController.java`](src/main/java/com/student/movieapp/controller/MovieDialogController.java) (`TMDB_EXECUTOR`, `handleSearchTmdb()`)

### 5. Database Integration (Relational Schema)
- **What it is**: Data persists in a local SQLite database (`cinevault.db`). It features a normalized relational database design with three tables: `movies`, `genres` (unique genre dictionary), and `movie_genres` (a many-to-many join table with foreign keys and cascade delete). This allows movies to belong to multiple genres simultaneously (e.g. "Action, Sci-Fi").
- **Where to show in code**:
  - Database Schema & Tables: [`DatabaseManager.java`](src/main/java/com/student/movieapp/dao/DatabaseManager.java) (`initializeDatabase()`)
  - Default Genre Seeding: [`DatabaseManager.java`](src/main/java/com/student/movieapp/dao/DatabaseManager.java) (`seedDefaultGenres()`)
  - Genre Join Table Syncing: [`DatabaseManager.java`](src/main/java/com/student/movieapp/dao/DatabaseManager.java) (`syncMovieGenres()`)

### 6. CRUD Operations
- **What it is**: Complete Create, Read, Update, and Delete operations for movies and their genre links. Users can add new films, view/filter the collection, edit any attribute, and delete movies with immediate database persistence and table/grid UI refreshes.
- **Where to show in code**:
  - Database CRUD: [`DatabaseManager.java`](src/main/java/com/student/movieapp/dao/DatabaseManager.java) (`addMovie()`, `getAllMovies()`, `updateMovie()`, `deleteMovie()`)
  - UI Event Handlers: [`MainController.java`](src/main/java/com/student/movieapp/controller/MainController.java) (`handleAddMovie()`, `handleEditMovie()`, `handleDeleteMovie()`)
  - Dialog Form Controller: [`MovieDialogController.java`](src/main/java/com/student/movieapp/controller/MovieDialogController.java) (`setMovie()`, `handleSave()`)

### 7. Networking & Data Parsing (TMDB API)
- **What it is**: The Add Movie dialog connects live to The Movie Database (TMDB) REST API via `java.net.http.HttpClient`. Typing a title and clicking "Search TMDB" queries the online API, parses the response with Jackson `ObjectMapper`, and automatically populates the poster image URL, release year, rating, and genre. The API key is kept secure in an uncommitted local properties file.
- **Where to show in code**:
  - HTTP Client & Jackson Parsing: [`TmdbService.java`](src/main/java/com/student/movieapp/service/TmdbService.java) (`searchMovie()`, `loadApiKey()`)
  - Search Result Record: [`TmdbMovieResult.java`](src/main/java/com/student/movieapp/service/TmdbMovieResult.java)
  - Auto-fill UI Action: [`MovieDialogController.java`](src/main/java/com/student/movieapp/controller/MovieDialogController.java) (`handleSearchTmdb()`)
  - Config Example: [`config.properties.example`](src/main/resources/config.properties.example)

---

## Tech Stack

- **Language**: Java 17+
- **GUI Framework**: JavaFX 21 (FXML, Scene Builder, CSS)
- **Database**: SQLite & SQLite-JDBC (with many-to-many join table)
- **JSON Parsing**: Jackson Databind (`com.fasterxml.jackson.core`)
- **Networking**: Java HTTP Client (`java.net.http.HttpClient`) + The Movie Database (TMDB) REST API
- **Build Tool**: Apache Maven

---

## Project Structure & File Links

Click any file to view its implementation directly in the repository:

- [`pom.xml`](pom.xml) — Maven build dependencies and JavaFX runtime plugin configuration
- [`run.bat`](run.bat) — One-click launcher script for Windows
- [`.gitignore`](.gitignore) — Git rules protecting local database (`cinevault.db`) and API keys
- [`src/main/java/module-info.java`](src/main/java/module-info.java) — JavaFX module declarations and exports
- **Application Core**:
  - [`MainApp.java`](src/main/java/com/student/movieapp/MainApp.java) — JavaFX application entry point & window lifecycle
  - [`AppLauncher.java`](src/main/java/com/student/movieapp/AppLauncher.java) — Fallback launcher for classpath execution
- **Controllers (UI Logic)**:
  - [`LoginController.java`](src/main/java/com/student/movieapp/controller/LoginController.java) — Login screen authentication logic
  - [`MainController.java`](src/main/java/com/student/movieapp/controller/MainController.java) — Dashboard UI controller (search, sort, card grid, table view)
  - [`MovieDialogController.java`](src/main/java/com/student/movieapp/controller/MovieDialogController.java) — Add/Edit popup controller with live TMDB search
- **Data Access & Storage**:
  - [`DatabaseManager.java`](src/main/java/com/student/movieapp/dao/DatabaseManager.java) — SQLite database schema, connections, and CRUD operations
  - [`Movie.java`](src/main/java/com/student/movieapp/model/Movie.java) — Movie data model with observable JavaFX properties
- **Services & Networking**:
  - [`TmdbService.java`](src/main/java/com/student/movieapp/service/TmdbService.java) — TMDB REST API client & API key loader
  - [`TmdbMovieResult.java`](src/main/java/com/student/movieapp/service/TmdbMovieResult.java) — TMDB API search result data record
- **Resources & UI Layouts**:
  - [`config.properties.example`](src/main/resources/config.properties.example) — API key configuration template
  - [`movies.json`](src/main/resources/data/movies.json) — Initial movie seed dataset
  - [`application.css`](src/main/resources/styles/application.css) — Movie Watchlist Manager dark theme stylesheet
  - [`LoginView.fxml`](src/main/resources/views/LoginView.fxml) — Login view layout
  - [`MainView.fxml`](src/main/resources/views/MainView.fxml) — Main dashboard view layout
  - [`MovieDialog.fxml`](src/main/resources/views/MovieDialog.fxml) — Add/Edit movie modal layout
