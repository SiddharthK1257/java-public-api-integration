package com.saiket.task2;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

/**
 * Service responsible for communicating with the public JokeAPI over HTTPS.
 * 
 * Uses Java 11+ built-in java.net.http.HttpClient with configurable timeouts,
 * status code validation, and resilient error handling for network interruptions.
 */
public class ApiService {

    private static final String BASE_URL = "https://v2.jokeapi.dev/joke/";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final String USER_AGENT = "SaiKet-Systems-Task2-Java/1.0 (Internship Project)";

    private final HttpClient httpClient;
    private final JsonParserService jsonParser;

    /**
     * Constructs ApiService with configured HttpClient and JsonParserService.
     */
    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.jsonParser = new JsonParserService();
    }

    /**
     * Alternative constructor for dependency injection (useful in testing).
     */
    public ApiService(HttpClient httpClient, JsonParserService jsonParser) {
        this.httpClient = httpClient;
        this.jsonParser = jsonParser;
    }

    /**
     * Fetches a joke from JokeAPI for a given category with safe-mode enabled.
     * 
     * @param category The joke category (e.g., "Programming", "Pun", "Misc", "Any")
     * @return Joke object parsed from the API response
     * @throws ApiException When an HTTP or networking error occurs
     */
    public Joke fetchJoke(String category) throws ApiException {
        String encodedCategory = (category != null && !category.trim().isEmpty()) ? category.trim() : "Programming";
        // Enforce safe-mode to ensure professional, internship-appropriate content
        String endpointUrl = BASE_URL + encodedCategory + "?safe-mode";
        return executeGetRequest(endpointUrl);
    }

    /**
     * Executes an HTTP GET request to the given URL and parses the JSON response into a Joke object.
     * 
     * @param url The full target URL
     * @return Joke object parsed from response
     * @throws ApiException On connection failure, timeout, non-200 HTTP code, or parsing issue
     */
    public Joke executeGetRequest(String url) throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Accept", "application/json")
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            // Send HTTP request synchronously
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String responseBody = response.body();

            // HTTP 200 OK
            if (statusCode >= 200 && statusCode < 300) {
                Joke joke = jsonParser.parseJoke(responseBody);
                if (joke.isError()) {
                    throw new ApiException("JokeAPI returned a logical error: " + joke.getMessage() 
                            + (joke.getAdditionalInfo() != null ? " (" + joke.getAdditionalInfo() + ")" : ""));
                }
                return joke;
            } else {
                // Non-200 HTTP status code
                String errorMessage = "HTTP request failed with status code: " + statusCode;
                // Attempt to parse API error body if available
                try {
                    Joke errorJoke = jsonParser.parseJoke(responseBody);
                    if (errorJoke != null && errorJoke.getMessage() != null) {
                        errorMessage += " - " + errorJoke.getMessage();
                    }
                } catch (Exception ignored) {
                    // Body was not JSON or could not be parsed; keep default status message
                }
                throw new ApiException(errorMessage, statusCode);
            }

        } catch (HttpTimeoutException e) {
            throw new ApiException("Request timed out after " + REQUEST_TIMEOUT.toSeconds() 
                    + " seconds. The API server took too long to respond.", e);
        } catch (UnknownHostException e) {
            throw new ApiException("Unable to resolve host (" + e.getMessage() 
                    + "). Please verify your internet connection or DNS settings.", e);
        } catch (ConnectException e) {
            throw new ApiException("Connection refused: Failed to establish a connection to the API server.", e);
        } catch (IOException e) {
            throw new ApiException("Network I/O error occurred while communicating with the API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("The API request was interrupted.", e);
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid URL or request parameter: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a raw HTTP GET request and returns the response body and status code.
     * Useful for diagnostics or custom URL testing.
     * 
     * @param url The target URL
     * @return HttpResponse containing raw String body
     * @throws Exception On network or timeout error
     */
    public HttpResponse<String> fetchRawResponse(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public JsonParserService getJsonParser() {
        return jsonParser;
    }
}
