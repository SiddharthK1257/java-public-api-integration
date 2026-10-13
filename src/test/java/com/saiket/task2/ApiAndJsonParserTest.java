package com.saiket.task2;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and integration tests for SaiKet Systems Task 2.
 */
public class ApiAndJsonParserTest {

    private JsonParserService parserService;
    private ApiService apiService;

    @BeforeEach
    public void setUp() {
        parserService = new JsonParserService();
        apiService = new ApiService();
    }

    @Test
    @DisplayName("Should successfully parse single-line joke JSON")
    public void testParseSingleJoke() throws JsonProcessingException {
        String mockJson = """
        {
            "error": false,
            "category": "Programming",
            "type": "single",
            "joke": "There are 10 types of people: those who understand binary and those who don't.",
            "flags": {"nsfw": false, "religious": false, "political": false, "racist": false, "sexist": false, "explicit": false},
            "id": 42,
            "safe": true,
            "lang": "en"
        }
        """;

        Joke joke = parserService.parseJoke(mockJson);
        assertNotNull(joke, "Parsed Joke should not be null");
        assertFalse(joke.isError(), "Joke should not have error flag");
        assertEquals("Programming", joke.getCategory());
        assertEquals("single", joke.getType());
        assertTrue(joke.isSingle());
        assertFalse(joke.isTwoPart());
        assertEquals(42, joke.getId());
        assertTrue(joke.isSafe());
        assertTrue(joke.getJoke().contains("10 types of people"));
    }

    @Test
    @DisplayName("Should successfully parse two-part joke JSON")
    public void testParseTwoPartJoke() throws JsonProcessingException {
        String mockJson = """
        {
            "error": false,
            "category": "Programming",
            "type": "twopart",
            "setup": "Why do programmers prefer dark mode?",
            "delivery": "Because light attracts bugs!",
            "flags": {"nsfw": false, "religious": false, "political": false, "racist": false, "sexist": false, "explicit": false},
            "id": 105,
            "safe": true,
            "lang": "en"
        }
        """;

        Joke joke = parserService.parseJoke(mockJson);
        assertNotNull(joke);
        assertFalse(joke.isError());
        assertEquals("twopart", joke.getType());
        assertTrue(joke.isTwoPart());
        assertFalse(joke.isSingle());
        assertEquals("Why do programmers prefer dark mode?", joke.getSetup());
        assertEquals("Because light attracts bugs!", joke.getDelivery());
        assertEquals(105, joke.getId());
    }

    @Test
    @DisplayName("Should parse API error response object correctly")
    public void testParseErrorResponse() throws JsonProcessingException {
        String mockErrorJson = """
        {
            "error": true,
            "internalError": false,
            "code": 106,
            "message": "No matching joke found",
            "causedBy": ["No jokes were found that match your provided filter(s)."],
            "additionalInfo": "The specified category is invalid.",
            "timestamp": 1791857067623
        }
        """;

        Joke joke = parserService.parseJoke(mockErrorJson);
        assertNotNull(joke);
        assertTrue(joke.isError(), "Should recognize error: true");
        assertEquals(106, joke.getCode());
        assertEquals("No matching joke found", joke.getMessage());
        assertEquals("The specified category is invalid.", joke.getAdditionalInfo());
    }

    @Test
    @DisplayName("Should throw JsonProcessingException when given malformed JSON")
    public void testMalformedJsonHandling() {
        String corruptJson = "{ error: false, category: unquoted_invalid_json ";
        assertThrows(JsonProcessingException.class, () -> {
            parserService.parseJoke(corruptJson);
        });
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when given empty or null string")
    public void testEmptyJsonHandling() {
        assertThrows(IllegalArgumentException.class, () -> {
            parserService.parseJoke("");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            parserService.parseJoke(null);
        });
    }

    @Test
    @DisplayName("Live API test: Fetch real programming joke from JokeAPI")
    public void testLiveApiFetchProgrammingJoke() {
        assertDoesNotThrow(() -> {
            Joke joke = apiService.fetchJoke("Programming");
            assertNotNull(joke);
            assertFalse(joke.isError(), "Live API should return error: false");
            assertNotNull(joke.getCategory(), "Category should not be null");
            assertTrue(joke.getId() > 0, "ID should be positive integer");
            if (joke.isTwoPart()) {
                assertNotNull(joke.getSetup());
                assertNotNull(joke.getDelivery());
            } else {
                assertNotNull(joke.getJoke());
            }
        });
    }

    @Test
    @DisplayName("Live API test: Graceful handling of invalid endpoint")
    public void testLiveApiInvalidEndpointHandling() {
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiService.executeGetRequest("https://v2.jokeapi.dev/joke/DefinitelyNonExistentCategory9999");
        });
        assertNotNull(exception.getMessage(), "Exception message should provide clear details");
    }
}
