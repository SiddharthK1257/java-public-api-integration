package com.saiket.task2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service responsible for parsing and processing JSON data using the Jackson library.
 * 
 * Provides robust error handling for malformed JSON, missing fields, or empty responses.
 */
public class JsonParserService {

    private final ObjectMapper objectMapper;

    /**
     * Initializes the Jackson ObjectMapper with safety features enabled:
     * - Does not fail on unknown JSON properties.
     * - Handles null values cleanly.
     */
    public JsonParserService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Parses a raw JSON string into a structured Joke model object.
     * 
     * @param jsonResponse The JSON string received from the API
     * @return A Joke object containing parsed joke data or error details
     * @throws IllegalArgumentException If the input JSON is null or empty
     * @throws JsonProcessingException If the JSON is malformed or invalid
     */
    public Joke parseJoke(String jsonResponse) throws JsonProcessingException {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot parse empty or null JSON response.");
        }

        try {
            return objectMapper.readValue(jsonResponse, Joke.class);
        } catch (JsonProcessingException e) {
            System.err.println("[PARSER ERROR] Failed to parse JSON response: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Formats any raw JSON string into a human-readable indented format.
     * Useful for debugging or inspecting API payloads.
     * 
     * @param json The compact JSON string
     * @return Pretty-printed JSON string, or the raw string if parsing fails
     */
    public String prettyPrintJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "";
        }
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            return json; // Fallback to raw string if formatting fails
        }
    }
}
