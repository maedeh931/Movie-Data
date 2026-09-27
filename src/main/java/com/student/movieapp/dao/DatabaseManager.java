package com.student.movieapp.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.student.movieapp.model.Movie;

import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data Access Object (DAO) for managing SQLite database CRUD operations for CineVault.
 * Supports relational many-to-many genres via 'genres' and 'movie_genres' tables.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:cinevault.db";

    private static final String[] DEFAULT_GENRES = {
            "Action", "Adventure", "Animation", "Comedy", "Crime",
            "Documentary", "Drama", "Horror", "Romance", "Sci-Fi", "Thriller"
    };

    /**
     * DTO for Jackson JSON deserialization.
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
     * Initializes the SQLite database:
     * 1. Creates movies table, genres table, and movie_genres join table.
     * 2. Seeds default genres if not present.
     * 3. Seeds initial movies if movies table is empty.
     * 4. Migrates existing movie records to movie_genres if needed.
     */
    public static void initializeDatabase() {
        String createMoviesTableSQL = """
            CREATE TABLE IF NOT EXISTS movies (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                genre TEXT,
                release_year INTEGER NOT NULL,
                rating REAL NOT NULL,
                status TEXT NOT NULL,
                date_added TEXT NOT NULL,
                poster_url TEXT
            );
        """;

        String createGenresTableSQL = """
            CREATE TABLE IF NOT EXISTS genres (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE COLLATE NOCASE
            );
        """;

        String createMovieGenresTableSQL = """
            CREATE TABLE IF NOT EXISTS movie_genres (
                movie_id INTEGER NOT NULL,
                genre_id INTEGER NOT NULL,
                PRIMARY KEY (movie_id, genre_id),
                FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
                FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createMoviesTableSQL);
            stmt.execute(createGenresTableSQL);
            stmt.execute(createMovieGenresTableSQL);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_movie_genres_movie ON movie_genres(movie_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_movie_genres_genre ON movie_genres(genre_id);");

            // Seed default genre dictionary
            seedDefaultGenres(conn);

            // Seed initial movies if table is empty
            if (isTableEmpty(conn)) {
                seedInitialData(conn);
            } else {
                // Migrate any existing movies that do not have rows in movie_genres
                migrateExistingMovieGenres(conn);
            }
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
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
     * Seeds common genres into the genres table if they do not exist.
     */
    private static void seedDefaultGenres(Connection conn) throws SQLException {
        String sql = "INSERT OR IGNORE INTO genres (name) VALUES (?);";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String genre : DEFAULT_GENRES) {
                pstmt.setString(1, genre);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    /**
     * Migrates legacy movies that have text in `genre` but no rows in `movie_genres`.
     */
    private static void migrateExistingMovieGenres(Connection conn) {
        String query = """
            SELECT m.id, m.genre FROM movies m
            WHERE m.genre IS NOT NULL AND TRIM(m.genre) != ''
              AND NOT EXISTS (SELECT 1 FROM movie_genres mg WHERE mg.movie_id = m.id);
        """;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            List<int[]> migrations = new ArrayList<>();
            List<String> genreStrings = new ArrayList<>();
            while (rs.next()) {
                migrations.add(new int[]{rs.getInt("id")});
                genreStrings.add(rs.getString("genre"));
            }

            for (int i = 0; i < migrations.size(); i++) {
                int movieId = migrations.get(i)[0];
                String genreStr = genreStrings.get(i);
                syncMovieGenres(conn, movieId, genreStr);
            }
        } catch (SQLException e) {
            System.err.println("Migration of existing movie genres failed: " + e.getMessage());
        }
    }

    /**
     * Pre-populates default demo records from JSON on first launch.
     */
    private static void seedInitialData(Connection conn) {
        String insertSQL = "INSERT INTO movies (title, genre, release_year, rating, status, date_added, poster_url) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (Reader reader = new InputStreamReader(Objects.requireNonNull(DatabaseManager.class.getResourceAsStream("/data/movies.json")));
             PreparedStatement pstmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {

            ObjectMapper mapper = new ObjectMapper();
            List<MovieDTO> movies = mapper.readValue(reader, new TypeReference<List<MovieDTO>>() {});

            for (MovieDTO dto : movies) {
                pstmt.setString(1, dto.title);
                pstmt.setString(2, dto.genre);
                pstmt.setInt(3, dto.releaseYear);
                pstmt.setDouble(4, dto.rating);
                pstmt.setString(5, dto.status);
                pstmt.setString(6, dto.dateAdded);
                pstmt.setString(7, dto.posterUrl == null ? "" : dto.posterUrl);
                pstmt.executeUpdate();

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int movieId = keys.getInt(1);
                        syncMovieGenres(conn, movieId, dto.genre);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to seed initial data from JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Looks up or inserts a genre by name and returns its ID.
     */
    public static int getOrCreateGenreId(Connection conn, String genreName) throws SQLException {
        if (genreName == null || genreName.trim().isEmpty()) {
            return -1;
        }
        String trimmed = genreName.trim();

        String insertSql = "INSERT OR IGNORE INTO genres (name) VALUES (?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, trimmed);
            pstmt.executeUpdate();
        }

        String selectSql = "SELECT id FROM genres WHERE LOWER(name) = LOWER(?);";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, trimmed);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Synchronizes a movie's linked genres in the movie_genres table.
     * Supports comma-separated genres (e.g. "Action, Sci-Fi").
     */
    public static void syncMovieGenres(Connection conn, int movieId, String genreStr) throws SQLException {
        // Delete existing links
        String deleteSql = "DELETE FROM movie_genres WHERE movie_id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setInt(1, movieId);
            pstmt.executeUpdate();
        }

        if (genreStr == null || genreStr.trim().isEmpty()) {
            return;
        }

        String insertJoinSql = "INSERT OR IGNORE INTO movie_genres (movie_id, genre_id) VALUES (?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertJoinSql)) {
            for (String part : genreStr.split(",")) {
                String clean = part.trim();
                if (!clean.isEmpty()) {
                    int genreId = getOrCreateGenreId(conn, clean);
                    if (genreId > 0) {
                        pstmt.setInt(1, movieId);
                        pstmt.setInt(2, genreId);
                        pstmt.addBatch();
                    }
                }
            }
            pstmt.executeBatch();
        }
    }

    /**
     * READ: Retrieves all movies from the database ordered by ID descending,
     * aggregating genres from the movie_genres join table.
     */
    public static List<Movie> getAllMovies() {
        List<Movie> list = new ArrayList<>();
        String query = """
            SELECT m.id, m.title, m.genre AS fallback_genre, m.release_year, m.rating,
                   m.status, m.date_added, m.poster_url,
                   GROUP_CONCAT(g.name, ', ') AS joined_genres
            FROM movies m
            LEFT JOIN movie_genres mg ON m.id = mg.movie_id
            LEFT JOIN genres g ON mg.genre_id = g.id
            GROUP BY m.id
            ORDER BY m.id DESC;
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String joined = rs.getString("joined_genres");
                String fallback = rs.getString("fallback_genre");
                String resolvedGenre = (joined != null && !joined.isBlank()) ? joined : (fallback != null ? fallback : "");

                list.add(new Movie(
                    rs.getInt("id"),
                    rs.getString("title"),
                    resolvedGenre,
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
     * CREATE: Inserts a new movie into the database, synchronizes genres with movie_genres,
     * and returns the generated ID.
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
                        syncMovieGenres(conn, id, movie.getGenre());
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
     * UPDATE: Updates an existing movie record in the database and updates its genre relationships.
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

            boolean updated = pstmt.executeUpdate() > 0;
            if (updated) {
                syncMovieGenres(conn, movie.getId(), movie.getGenre());
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("Failed to update movie: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * DELETE: Removes a movie record and its join table links from the database by ID.
     */
    public static boolean deleteMovie(int id) {
        try (Connection conn = getConnection()) {
            // Delete join table links
            String deleteJoinSql = "DELETE FROM movie_genres WHERE movie_id = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteJoinSql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }

            // Delete movie record
            String sql = "DELETE FROM movies WHERE id = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Failed to delete movie: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all genre names from the database sorted alphabetically.
     */
    public static List<String> getAllGenres() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM genres ORDER BY name ASC;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch genres: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all genre names linked to a specific movie.
     */
    public static List<String> getGenresForMovie(int movieId) {
        List<String> list = new ArrayList<>();
        String sql = """
            SELECT g.name FROM genres g
            JOIN movie_genres mg ON g.id = mg.genre_id
            WHERE mg.movie_id = ?
            ORDER BY g.name ASC;
        """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, movieId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch genres for movie: " + e.getMessage());
        }
        return list;
    }
}
