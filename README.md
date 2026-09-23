# CineVault — Modern Movie Tracker & Collection Manager

A sleek, dark-mode JavaFX desktop application designed to organize, track, and manage your personal movie collection with a modern dashboard aesthetic. Built with **JavaFX**, **Scene Builder**, **FXML**, and **CSS** following a clean **Model-View-Controller (MVC)** architecture.

---

## 1. Key Features

- **Sidebar Navigation Collections (Left)**:
  - Curated collection categories: *All Movies*, *Want to Watch*, *Watched*, *Favorites*, *Quick Picks*, *Date Night*, *Movie Night*, and *Award Winners*.
  - Left-pointing triangle icons (`◀`) and active state highlight.
  - `+ New Collection` button to create custom user collections dynamically.
- **Dynamic Header & Real-time Metrics**:
  - Live category title based on current collection.
  - Stat pill badges: total movie count, average IMDb rating, and estimated watch time.
  - Sort dropdown: *Recently Added*, *Highest Rating*, *Lowest Rating*, *Title (A-Z)*, *Release Year (Newest/Oldest)*.
- **Dual Visual Modes**:
  - **Grid View (Cards)**: Responsive flow of movie cards featuring vibrant, unique linear-gradient poster art with bold white initials in the center (e.g., *IN*, *DU*, *PA*, *SR*, *TB*, *OP*), dark footers with star ratings (`★ 8.7`), release years, and status badge pills.
  - **Table View (Power User)**: Dark-mode tabular view with customized badge cells and star rating formatting.
  - Seamlessly toggle views with header buttons or shortcuts (`Ctrl+G` / `Ctrl+T`).
- **Command & Search Bar (Power User View)**:
  - Instant live filtering by title, genre, year, or status.
  - Power user commands: type `add movie`, `delete`, `filter sci-fi`, `filter action`, `filter watched`, etc.
- **Keyboard-First Navigation**:
  - `Ctrl + K` — Focus Command / Search bar
  - `Ctrl + N` — Add new movie
  - `Enter` — Edit selected movie (or execute command)
  - `Del` — Delete selected movie
  - `Ctrl + G` — Switch to Grid View
  - `Ctrl + T` — Switch to Table View
  - `Esc` — Clear search / Cancel
  - Physical keyboard shortcut map card docked at the bottom.
- **Dark Theme System**:
  - Deep dark charcoal palette (`#09090b` / `#121217`), border glow effects, dark modal dialogs, and styled alerts.

---

## 2. Project Structure

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
            │   ├── MainView.fxml             # CineVault dashboard FXML layout (Scene Builder ready)
            │   └── MovieDialog.fxml          # Add/Edit modal dialog FXML layout (Scene Builder ready)
            └── styles/
                └── application.css           # CineVault dark-mode CSS design system
```

---

## 3. How to Run the Application

### Option 1: One-Click Launch Script
Double-click `run.bat` in the project root folder.

### Option 2: Command Line (PowerShell)
```powershell
$env:JAVA_HOME = "C:\Users\Madiha_Rahman\.jdks\openjdk-26.0.2"
& "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" javafx:run
```

### Option 3: IntelliJ IDEA
1. Open the project folder in **IntelliJ IDEA**.
2. Run `com.student.movieapp.MainApp`.

---

## 4. How to Open in Scene Builder

Both FXML views are 100% compatible with Scene Builder:
- Double-click `open_in_scenebuilder.bat` to edit `MainView.fxml`.
- Double-click `open_dialog_in_scenebuilder.bat` to edit `MovieDialog.fxml`.
