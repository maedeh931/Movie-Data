package com.student.movieapp.dao;

import com.student.movieapp.model.Movie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing SQLite database CRUD operations for CineVault.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:cinevault.db";

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
                date_added TEXT NOT NULL
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
     * Pre-populates default demo records on first launch.
     */
    private static void seedInitialData(Connection conn) throws SQLException {
        String insertSQL = "INSERT INTO movies (title, genre, release_year, rating, status, date_added) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            Object[][] seed = {
                {"Interstellar", "Sci-Fi", 2014, 8.7, "Watched", "04/09/26"},
                {"Dune", "Sci-Fi", 2021, 8.0, "Want to Watch", "02/09/26"},
                {"Dune: Part Two", "Sci-Fi", 2024, 8.6, "Watched", "05/09/26"},
                {"Parasite", "Drama", 2019, 8.5, "Watched", "01/09/26"},
                {"The Shawshank Redemption", "Drama", 1994, 9.3, "Watched", "25/08/26"},
                {"The Batman", "Action", 2022, 7.8, "Watched", "27/08/26"},
                {"Oppenheimer", "Drama", 2023, 8.9, "Watched", "06/09/26"},
                {"Inception", "Sci-Fi", 2010, 8.8, "Watched", "03/09/26"},
                {"The Dark Knight", "Action", 2008, 9.0, "Watched", "28/08/26"},
                {"Blade Runner 2049", "Sci-Fi", 2017, 8.0, "Want to Watch", "29/08/26"},
                {"Whiplash", "Drama", 2014, 8.5, "Watched", "30/08/26"},
                {"Spider-Man: Across the Spider-Verse", "Animation", 2023, 8.7, "Watched", "31/08/26"},
                {"Pulp Fiction", "Crime", 1994, 8.9, "Watched", "20/08/26"},
                {"Everything Everywhere All at Once", "Sci-Fi", 2022, 7.8, "Want to Watch", "21/08/26"},
                {"Spirited Away", "Animation", 2001, 8.6, "Watched", "22/08/26"},
                {"La La Land", "Romance", 2016, 8.0, "Want to Watch", "23/08/26"},
                {"Fight Club", "Drama", 1999, 8.8, "Watched", "18/08/26"},
                {"The Matrix", "Sci-Fi", 1999, 8.7, "Watched", "19/08/26"},
                {"Gladiator", "Action", 2000, 8.5, "Watched", "17/08/26"},
                {"The Grand Budapest Hotel", "Comedy", 2014, 8.1, "Want to Watch", "16/08/26"},
                {"Alien", "Horror", 1979, 8.5, "Watched", "15/08/26"},
                {"The Prestige", "Drama", 2006, 8.5, "Watched", "14/08/26"},
                {"Coco", "Animation", 2017, 8.4, "Watched", "13/08/26"},
                {"Arrival", "Sci-Fi", 2016, 7.9, "Watched", "12/08/26"}
            };

            for (Object[] row : seed) {
                pstmt.setString(1, (String) row[0]);
                pstmt.setString(2, (String) row[1]);
                pstmt.setInt(3, (Integer) row[2]);
                pstmt.setDouble(4, (Double) row[3]);
                pstmt.setString(5, (String) row[4]);
                pstmt.setString(6, (String) row[5]);
                pstmt.executeUpdate();
            }
        }
    }

    /**
     * READ: Retrieves all movies from the database ordered by ID descending.
     */
    public static List<Movie> getAllMovies() {
        List<Movie> list = new ArrayList<>();
        String query = "SELECT id, title, genre, release_year, rating, status, date_added FROM movies ORDER BY id DESC;";

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
                    rs.getString("date_added")
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
        String sql = "INSERT INTO movies (title, genre, release_year, rating, status, date_added) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, movie.getTitle());
            pstmt.setString(2, movie.getGenre());
            pstmt.setInt(3, movie.getReleaseYear());
            pstmt.setDouble(4, movie.getRating());
            pstmt.setString(5, movie.getStatus());
            pstmt.setString(6, movie.getDateAdded());

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
        String sql = "UPDATE movies SET title = ?, genre = ?, release_year = ?, rating = ?, status = ?, date_added = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, movie.getTitle());
            pstmt.setString(2, movie.getGenre());
            pstmt.setInt(3, movie.getReleaseYear());
            pstmt.setDouble(4, movie.getRating());
            pstmt.setString(5, movie.getStatus());
            pstmt.setString(6, movie.getDateAdded());
            pstmt.setInt(7, movie.getId());

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
