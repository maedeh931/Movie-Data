# Movie Watchlist Manager

A JavaFX desktop application designed to organize and manage movies with a clean **Model-View-Controller (MVC)** architecture. Built with **JavaFX**, **Scene Builder**, **FXML**, and **CSS**.

---

## 1. Project Structure

```
MovieWatchlist/
├── pom.xml                                   # Maven dependencies (JavaFX 21 controls, FXML)
├── README.md                                 # Project documentation & Scene Builder guide
├── .gitignore                                # Git ignore rules
├── run.bat                                   # One-click launch script
├── open_in_scenebuilder.bat                  # One-click script to open MainView.fxml in Scene Builder
├── open_dialog_in_scenebuilder.bat           # One-click script to open MovieDialog.fxml in Scene Builder
└── src/
    └── main/
        ├── java/
        │   ├── module-info.java              # JavaFX modular declaration
        │   └── com/student/movieapp/
        │       ├── MainApp.java              # Application entry point & Stage setup
        │       ├── AppLauncher.java          # Fallback non-modular launcher
        │       ├── model/
        │       │   └── Movie.java            # Movie model entity with JavaFX observable properties
        │       └── controller/
        │           ├── MainController.java        # Main dashboard controller with demo data & filters
        │           └── MovieDialogController.java # Add/Edit modal dialog controller & validation
        └── resources/
            ├── views/
            │   ├── MainView.fxml             # Main dashboard FXML layout (Scene Builder ready)
            │   └── MovieDialog.fxml          # Add/Edit modal dialog FXML layout (Scene Builder ready)
            └── styles/
                └── application.css           # Modern CSS styling for tables, buttons, and dialogs
```

---

## 2. Architecture & Design Patterns

The project strictly follows the **Model-View-Controller (MVC)** pattern:

- **Model (`com.student.movieapp.model.Movie`)**:
  - Encapsulates movie properties: `id`, `title`, `genre`, `releaseYear`, `rating`, `status`, `dateAdded`.
  - Implements JavaFX Observable Properties (`StringProperty`, `IntegerProperty`, `DoubleProperty`) allowing reactive automatic UI updates in `TableView`.
  - Provides standard getters and setters.

- **View (`views/MainView.fxml`, `views/MovieDialog.fxml`, `styles/application.css`)**:
  - Declarative XML UI defined via FXML, designed for 100% compatibility with **Scene Builder**.
  - All visual styles and hover states are decoupled into `application.css`.
  - Supports responsive window resizing with constrained column resize policies.

- **Controller (`com.student.movieapp.controller`)**:
  - `MainController`: Binds `ObservableList<Movie>` to the `TableView` columns using `PropertyValueFactory`. Handles filtering by Genre, Status, and Date Added. Coordinates opening the modal dialog and displays confirmation/information `Alert` dialogs.
  - `MovieDialogController`: Controls the secondary `APPLICATION_MODAL` window. Implements validation logic (non-empty title, valid release year between 1888-2100, valid rating between 0.0-10.0, required selections) and displays `Alert.AlertType.ERROR` when invalid.

---

## 3. Demo Data Included

The application starts with the demo records specified in the assignment document:

| Title | Genre | Release Year | Rating | Status | Date Added |
|---|---|---|---|---|---|
| Interstellar | Sci-Fi | 2014 | 8.7 | Watched | 04/09/26 |
| Inception | Sci-Fi | 2010 | 8.8 | Watched | 03/09/26 |
| Dune | Sci-Fi | 2021 | 8.0 | Want to Watch | 02/09/26 |
| Parasite | Drama | 2019 | 8.5 | Watched | 01/09/26 |
| The Dark Knight | Action | 2008 | 9.0 | Watched | 28/08/26 |

---

## 4. How to Open in Scene Builder

You can visually edit and inspect the UI layout in **Scene Builder**:

### Method 1: Using the Batch Scripts
- Double-click `open_in_scenebuilder.bat` to launch `MainView.fxml` in Scene Builder.
- Double-click `open_dialog_in_scenebuilder.bat` to launch `MovieDialog.fxml` in Scene Builder.

### Method 2: From Scene Builder Application
1. Open **Scene Builder**.
2. Click **File -> Open...** (`Ctrl + O`).
3. Navigate to `src/main/resources/views/` and choose either:
   - `MainView.fxml` (Main dashboard)
   - `MovieDialog.fxml` (Add/Edit modal dialog)

### Method 3: From IntelliJ IDEA
1. In IntelliJ's Project tool window, expand `src/main/resources/views/`.
2. Right-click on `MainView.fxml` or `MovieDialog.fxml`.
3. Select **Open in SceneBuilder**.

---

## 5. How to Run the Application

### Option 1: Double-click
Double-click `run.bat` in the project root folder.

### Option 2: Command Line (PowerShell / Terminal)
```powershell
$env:JAVA_HOME = "C:\Users\Madiha_Rahman\.jdks\openjdk-26.0.2"
& "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" javafx:run
```

### Option 3: IntelliJ IDEA
1. Open the project folder in **IntelliJ IDEA**.
2. IntelliJ will detect `pom.xml` as a Maven project.
3. Open `com.student.movieapp.MainApp` and click the green **Run** arrow.

---

## 6. Manual Git Commit Instructions

As requested, you can manually commit this initial build to your Git repository:

```bash
git init
git add .
git commit -m "feat: Initial JavaFX GUI with MVC architecture and demo data"
```
