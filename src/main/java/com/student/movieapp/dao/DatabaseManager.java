package com.student.movieapp.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.student.movieapp.model.Movie;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data Access Object (DAO) for managing SQLite database CRUD operations for CineVault.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:cinevault.db";

    /**
     * DTO for Gson JSON deserialization since JavaFX Property fields in Movie
     * cannot be directly instantiated by Gson reflection.
     */
    public static class MovieDTO {
        public String title;
        public String genre;
        public int releaseYear;
        public double rating;
        public String status;
        public String dateAdded;
        public String posterUrl;
    }

    /**
     * Initializes the SQLite database, creates the movies table if it doesn't exist,
     * and seeds initial demo movies if table is empty.
     */
    public static void initializeDatabase() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS movies (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                genre TEXT NOT NULL,
                release_year INTEGER NOT NULL,
                rating REAL NOT NULL,
                status TEXT NOT NULL,
                date_added TEXT NOT NULL,
                poster_url TEXT
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);

            // Seed if empty
            if (isTableEmpty(conn)) {
                seedInitialData(conn);
            }
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static boolean isTableEmpty(Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) FROM movies;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return true;
    }

    /**
     * Pre-populates default demo records from JSON on first launch.
     */
    private static void seedInitialData(Connection conn) {
        String insertSQL = "INSERT INTO movies (title, genre, release_year, rating, status, date_added, poster_url) VALUES (?, ?, ?, ?, ?, ?, ?);";
        
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(DatabaseManager.class.getResourceAsStream("/data/movies.json")));
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
             
            Gson gson = new Gson();
            Type movieListType = new TypeToken<ArrayList<MovieDTO>>(){}.getType();
            List<MovieDTO> movies = gson.fromJson(reader, movieListType);
            
            for (MovieDTO dto : movies) {
                pstmt.setString(1, dto.title);
                pstmt.setString(2, dto.genre);
                pstmt.setInt(3, dto.releaseYear);
                pstmt.setDouble(4, dto.rating);
                pstmt.setString(5, dto.status);
                pstmt.setString(6, dto.dateAdded);
                pstmt.setString(7, dto.posterUrl == null ? "" : dto.posterUrl);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Failed to seed initial data from JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * READ: Retrieves all movies from the database ordered by ID descending.
     */
    public static List<Movie> getAllMovies() {
        List<Movie> list = new ArrayList<>();
        String query = "SELECT id, title, genre, release_year, rating, status, date_added, poster_url FROM movies ORDER BY id DESC;";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Movie(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("genre"),
                    rs.getInt("release_year"),
                    rs.getDouble("rating"),
                    rs.getString("status"),
                    rs.getString("date_added"),
                    rs.getString("poster_url")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch movies from database: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * CREATE: Inserts a new movie into the database and returns the generated ID.
     */
    public static int addMovie(Movie movie) {
        String sql = "INSERT INTO movies (title, genre, release_year, rating, status, date_added, poster_url) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, movie.getTitle());
            pstmt.setString(2, movie.getGenre());
            pstmt.setInt(3, movie.getReleaseYear());
            pstmt.setDouble(4, movie.getRating());
            pstmt.setString(5, movie.getStatus());
            pstmt.setString(6, movie.getDateAdded());
            pstmt.setString(7, movie.getPosterUrl() == null ? "" : movie.getPosterUrl());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        movie.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to insert movie: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * UPDATE: Updates an existing movie record in the database.
     */
    public static boolean updateMovie(Movie movie) {
        String sql = "UPDATE movies SET title = ?, genre = ?, release_year = ?, rating = ?, status = ?, date_added = ?, poster_url = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, movie.getTitle());
            pstmt.setString(2, movie.getGenre());
            pstmt.setInt(3, movie.getReleaseYear());
            pstmt.setDouble(4, movie.getRating());
            pstmt.setString(5, movie.getStatus());
            pstmt.setString(6, movie.getDateAdded());
            pstmt.setString(7, movie.getPosterUrl() == null ? "" : movie.getPosterUrl());
            pstmt.setInt(8, movie.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update movie: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * DELETE: Removes a movie record from the database by ID.
     */
    public static boolean deleteMovie(int id) {
        String sql = "DELETE FROM movies WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to delete movie: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
