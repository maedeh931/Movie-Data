package com.student.movieapp.model;

import javafx.beans.property.*;

// Represents a single movie in the collection
public class Movie {

    // Stored details that automatically update the user interface
    private final IntegerProperty id;
    private final StringProperty title;
    private final StringProperty genre;
    private final IntegerProperty releaseYear;
    private final DoubleProperty rating;
    private final StringProperty status;
    private final StringProperty dateAdded;
    private final StringProperty posterUrl;

    // Create a new movie with starting default values
    public Movie() {
        this(0, "", "", 0, 0.0, "Want to Watch", "", "");
    }

    // Create a movie with all its details filled in
    public Movie(int id, String title, String genre, int releaseYear, double rating, String status, String dateAdded, String posterUrl) {
        this.id = new SimpleIntegerProperty(id);
        this.title = new SimpleStringProperty(title);
        this.genre = new SimpleStringProperty(genre);
        this.releaseYear = new SimpleIntegerProperty(releaseYear);
        this.rating = new SimpleDoubleProperty(rating);
        this.status = new SimpleStringProperty(status);
        this.dateAdded = new SimpleStringProperty(dateAdded);
        this.posterUrl = new SimpleStringProperty(posterUrl);
    }

    // Database ID number
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // Movie title
    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    // Movie genre or list of genres
    public String getGenre() {
        return genre.get();
    }

    public void setGenre(String genre) {
        this.genre.set(genre);
    }

    public StringProperty genreProperty() {
        return genre;
    }

    // Year the movie came out
    public int getReleaseYear() {
        return releaseYear.get();
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear.set(releaseYear);
    }

    public IntegerProperty releaseYearProperty() {
        return releaseYear;
    }

    // Movie score rating from 0.0 to 10.0
    public double getRating() {
        return rating.get();
    }

    public void setRating(double rating) {
        this.rating.set(rating);
    }

    public DoubleProperty ratingProperty() {
        return rating;
    }

    // Watch status such as Watched or Want to Watch
    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }

    // Date the movie was saved
    public String getDateAdded() {
        return dateAdded.get();
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded.set(dateAdded);
    }

    public StringProperty dateAddedProperty() {
        return dateAdded;
    }

    // Web address for the poster image
    public String getPosterUrl() {
        return posterUrl.get();
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl.set(posterUrl);
    }

    public StringProperty posterUrlProperty() {
        return posterUrl;
    }

    // Simple text version of the movie details
    @Override
    public String toString() {
        return String.format("%s | %s | %d | %.1f | %s", getTitle(), getGenre(), getReleaseYear(), getRating(), getStatus());
    }
}
