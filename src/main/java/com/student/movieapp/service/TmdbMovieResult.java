package com.student.movieapp.service;

// Holds movie details found online from TMDB
public record TmdbMovieResult(
        String title,
        Integer releaseYear,
        Double rating,
        String posterUrl,
        String genre
) {
}
