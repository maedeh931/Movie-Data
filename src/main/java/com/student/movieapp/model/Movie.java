package com.student.movieapp.model;

import javafx.beans.property.*;

/**
 * Model representing a Movie entity in the Movie Watchlist Manager.
 * Uses JavaFX properties for reactive UI data binding with TableView.
 */
public class Movie {
    private final IntegerProperty id;
    private final StringProperty title;
    private final StringProperty genre;
    private final IntegerProperty releaseYear;
    private final DoubleProperty rating;
    private final StringProperty status;
    private final StringProperty dateAdded;

    /**
     * Default constructor for empty initialization.
     */
    public Movie() {
        this(0, "", "", 0, 0.0, "Want to Watch", "");
    }

    /**
     * Full constructor.
     *
     * @param id          Unique movie ID
     * @param title       Movie title
     * @param genre       Movie genre (e.g. Sci-Fi, Action, Drama)
     * @param releaseYear Release year (e.g. 2014)
     * @param rating      IMDb / personal rating (e.g. 8.7)
     * @param status      Watch status ("Want to Watch", "Watching", "Watched")
     * @param dateAdded   Date string when the movie was added
     */
    public Movie(int id, String title, String genre, int releaseYear, double rating, String status, String dateAdded) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.genre = new SimpleStringProperty(genre);
        this.releaseYear = new SimpleIntegerProperty(releaseYear);
        this.rating = new SimpleDoubleProperty(rating);
        this.status = new SimpleStringProperty(status);
        this.dateAdded = new SimpleStringProperty(dateAdded);
    }

    // --- ID Property ---
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // --- Title Property ---
    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    // --- Genre Property ---
    public String getGenre() {
        return genre.get();
    }

    public void setGenre(String genre) {
        this.genre.set(genre);
    }

    public StringProperty genreProperty() {
        return genre;
    }

    // --- Release Year Property ---
    public int getReleaseYear() {
        return releaseYear.get();
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear.set(releaseYear);
    }

    public IntegerProperty releaseYearProperty() {
        return releaseYear;
    }

    // --- Rating Property ---
    public double getRating() {
        return rating.get();
    }

    public void setRating(double rating) {
        this.rating.set(rating);
    }

    public DoubleProperty ratingProperty() {
        return rating;
    }

    // --- Status Property ---
    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }

    // --- Date Added Property ---
    public String getDateAdded() {
        return dateAdded.get();
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded.set(dateAdded);
    }

    public StringProperty dateAddedProperty() {
        return dateAdded;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %d | %.1f | %s", getTitle(), getGenre(), getReleaseYear(), getRating(), getStatus());
    }
}
