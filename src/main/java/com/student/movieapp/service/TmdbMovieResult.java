package com.student.movieapp.service;

/**
 * Data holder for TMDB movie search results.
 */
public record TmdbMovieResult(
        String title,
        Integer releaseYear,
        Double rating,
        String posterUrl,
        String genre
) {
}
