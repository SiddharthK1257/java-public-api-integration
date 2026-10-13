package com.saiket.task2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Model class representing a Joke retrieved from JokeAPI (https://v2.jokeapi.dev/).
 * 
 * Supports both single-part jokes and two-part (setup + delivery) jokes,
 * as well as API error responses and content safety flags.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Joke {

    @JsonProperty("error")
    private boolean error;

    @JsonProperty("category")
    private String category;

    @JsonProperty("type")
    private String type; // "single" or "twopart"

    @JsonProperty("joke")
    private String joke; // Used when type is "single"

    @JsonProperty("setup")
    private String setup; // Used when type is "twopart"

    @JsonProperty("delivery")
    private String delivery; // Used when type is "twopart"

    @JsonProperty("flags")
    private Map<String, Boolean> flags;

    @JsonProperty("id")
    private int id;

    @JsonProperty("safe")
    private boolean safe;

    @JsonProperty("lang")
    private String lang;

    // Error response attributes if error is true
    @JsonProperty("message")
    private String message;

    @JsonProperty("additionalInfo")
    private String additionalInfo;

    @JsonProperty("code")
    private Integer code;

    /**
     * Default no-arg constructor required by Jackson databind.
     */
    public Joke() {
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getCategory() {
        return category != null ? category : "Unknown";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type != null ? type : "unknown";
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJoke() {
        return joke;
    }

    public void setJoke(String joke) {
        this.joke = joke;
    }

    public String getSetup() {
        return setup;
    }

    public void setSetup(String setup) {
        this.setup = setup;
    }

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    public Map<String, Boolean> getFlags() {
        return flags;
    }

    public void setFlags(Map<String, Boolean> flags) {
        this.flags = flags;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSafe() {
        return safe;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    public String getLang() {
        return lang != null ? lang.toUpperCase() : "EN";
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * Checks if this joke is a two-part joke (setup + punchline).
     * @return true if type is "twopart", false otherwise.
     */
    public boolean isTwoPart() {
        return "twopart".equalsIgnoreCase(this.type);
    }

    /**
     * Checks if this joke is a single-liner joke.
     * @return true if type is "single", false otherwise.
     */
    public boolean isSingle() {
        return "single".equalsIgnoreCase(this.type);
    }

    @Override
    public String toString() {
        if (error) {
            return "API Error [" + code + "]: " + message + (additionalInfo != null ? " (" + additionalInfo + ")" : "");
        }
        if (isTwoPart()) {
            return "[Two-Part Joke] Setup: " + setup + " | Delivery: " + delivery;
        } else {
            return "[Single Joke] " + joke;
        }
    }
}
