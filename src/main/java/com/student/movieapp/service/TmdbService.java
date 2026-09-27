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

// Helper that talks to The Movie Database (TMDB) to find movie posters and info
public class TmdbService {

    // Web addresses for TMDB search and poster images
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/search/movie";
    private static final String TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w500";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpClient httpClient;
    private final String apiKey;

    // Set up our web client and load the secret API key
    public TmdbService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
        this.apiKey = loadApiKey();
    }

    // Find and read the secret API key from our config file
    private String loadApiKey() {
        // 1. Try reading from the app resources folder
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

        // 2. Try looking in the project folder directly
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

        // 3. Check system or computer environment settings
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

    // Check if an API key was found and isn't blank
    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    // Search for a movie by name and return the top match
    public Optional<TmdbMovieResult> searchMovie(String query) throws Exception {
        // Make sure the title isn't empty
        if (query == null || query.trim().isEmpty()) {
            return Optional.empty();
        }

        // Make sure the API key is ready to use
        if (!isApiKeyConfigured()) {
            throw new IllegalStateException("TMDB API key is not configured in config.properties.");
        }

        // Prepare the web address and send the search request
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

        // Read the response from TMDB
        JsonNode root = OBJECT_MAPPER.readTree(response.body());
        JsonNode results = root.path("results");
        if (!results.isArray() || results.isEmpty()) {
            return Optional.empty();
        }

        // Grab the first movie result from the list
        JsonNode top = results.get(0);
        String title = top.path("title").asText().trim();

        // Extract the movie poster link
        JsonNode posterPathNode = top.path("poster_path");
        String posterPath = posterPathNode.isTextual() ? posterPathNode.asText() : null;
        String posterUrl = (posterPath != null && !posterPath.isBlank()) ? (TMDB_IMAGE_BASE + posterPath) : "";

        // Extract the release year from the release date
        String releaseDate = top.path("release_date").asText();
        Integer releaseYear = null;
        if (releaseDate.length() >= 4) {
            try {
                releaseYear = Integer.parseInt(releaseDate.substring(0, 4));
            } catch (NumberFormatException ignored) {
            }
        }

        // Extract and round the rating to one decimal place
        Double rating = null;
        if (top.has("vote_average")) {
            double rawRating = top.path("vote_average").asDouble(0.0);
            rating = Math.round(rawRating * 10.0) / 10.0;
        }

        // Match TMDB genre numbers to our genre names
        String genre = null;
        JsonNode genreIds = top.path("genre_ids");
        if (genreIds.isArray() && !genreIds.isEmpty()) {
            genre = mapGenreId(genreIds.get(0).asInt());
        }

        // Package the movie info and return it
        return Optional.of(new TmdbMovieResult(title, releaseYear, rating, posterUrl, genre));
    }

    // Convert TMDB genre numbers into plain genre names
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
