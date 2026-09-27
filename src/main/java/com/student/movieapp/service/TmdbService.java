package com.student.movieapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

/**
 * Service to interact with The Movie Database (TMDB) API.
 * Safely loads the API key from config.properties and searches movies.
 */
public class TmdbService {

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/search/movie";
    private static final String TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w500";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpClient httpClient;
    private final String apiKey;

    public TmdbService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
        this.apiKey = loadApiKey();
    }

    /**
     * Loads the TMDB API key from config.properties or environment.
     */
    private String loadApiKey() {
        // 1. Try classpath resource
        try (InputStream in = getClass().getResourceAsStream("/config.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                String key = props.getProperty("tmdb.api.key");
                if (key != null && !key.trim().isEmpty() && !key.contains("YOUR_TMDB_API_KEY")) {
                    return key.trim();
                }
            }
        } catch (Exception ignored) {
        }

        // 2. Fallback to local file in project directory or resources folder
        Path[] candidatePaths = new Path[]{
                Path.of("src", "main", "resources", "config.properties"),
                Path.of("config.properties")
        };
        for (Path path : candidatePaths) {
            if (Files.exists(path)) {
                try (InputStream in = Files.newInputStream(path)) {
                    Properties props = new Properties();
                    props.load(in);
                    String key = props.getProperty("tmdb.api.key");
                    if (key != null && !key.trim().isEmpty() && !key.contains("YOUR_TMDB_API_KEY")) {
                        return key.trim();
                    }
                } catch (Exception ignored) {
                }
            }
        }

        // 3. Fallback to system property or environment variable
        String sysProp = System.getProperty("tmdb.api.key");
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp.trim();
        }
        String envProp = System.getenv("TMDB_API_KEY");
        if (envProp != null && !envProp.isBlank()) {
            return envProp.trim();
        }

        return "";
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Searches TMDB for a movie by title query and returns the top result if available.
     *
     * @param query Title or search term
     * @return Optional containing the top TmdbMovieResult, or empty if no matches
     * @throws Exception if network fails or API returns error
     */
    public Optional<TmdbMovieResult> searchMovie(String query) throws Exception {
        if (query == null || query.trim().isEmpty()) {
            return Optional.empty();
        }
        if (!isApiKeyConfigured()) {
            throw new IllegalStateException("TMDB API key is not configured in config.properties.");
        }

        String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        String url = TMDB_BASE_URL + "?api_key=" + apiKey + "&query=" + encodedQuery + "&include_adult=false";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) CineVault/1.0")
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("TMDB API returned HTTP status " + response.statusCode());
        }

        JsonNode root = OBJECT_MAPPER.readTree(response.body());
        JsonNode results = root.path("results");
        if (!results.isArray() || results.isEmpty()) {
            return Optional.empty();
        }

        JsonNode top = results.get(0);
        String title = top.path("title").asText().trim();
        JsonNode posterPathNode = top.path("poster_path");
        String posterPath = posterPathNode.isTextual() ? posterPathNode.asText() : null;
        String posterUrl = (posterPath != null && !posterPath.isBlank()) ? (TMDB_IMAGE_BASE + posterPath) : "";

        String releaseDate = top.path("release_date").asText();
        Integer releaseYear = null;
        if (releaseDate.length() >= 4) {
            try {
                releaseYear = Integer.parseInt(releaseDate.substring(0, 4));
            } catch (NumberFormatException ignored) {
            }
        }

        Double rating = null;
        if (top.has("vote_average")) {
            double rawRating = top.path("vote_average").asDouble(0.0);
            rating = Math.round(rawRating * 10.0) / 10.0;
        }

        String genre = null;
        JsonNode genreIds = top.path("genre_ids");
        if (genreIds.isArray() && !genreIds.isEmpty()) {
            genre = mapGenreId(genreIds.get(0).asInt());
        }

        return Optional.of(new TmdbMovieResult(title, releaseYear, rating, posterUrl, genre));
    }

    private String mapGenreId(int id) {
        return switch (id) {
            case 28 -> "Action";
            case 12 -> "Adventure";
            case 16 -> "Animation";
            case 35 -> "Comedy";
            case 80 -> "Crime";
            case 99 -> "Documentary";
            case 18 -> "Drama";
            case 27 -> "Horror";
            case 10749 -> "Romance";
            case 878 -> "Sci-Fi";
            case 53 -> "Thriller";
            default -> null;
        };
    }
}
