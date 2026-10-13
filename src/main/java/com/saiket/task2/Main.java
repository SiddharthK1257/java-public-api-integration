package com.saiket.task2;

import java.util.Scanner;

/**
 * Main application class for SaiKet Systems Internship Task 2.
 * 
 * Demonstrates:
 * - Connecting to a public HTTPS REST API using java.net.http.HttpClient
 * - Handling network status codes, timeouts, and connectivity issues
 * - Parsing structured JSON data using Jackson
 * - Formatting and displaying both single-part and two-part jokes
 * - Interactive, user-friendly console UI suitable for internship evaluation
 */
public class Main {

    private static final String APP_VERSION = "1.0.0";
    private static final ApiService apiService = new ApiService();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        printBanner();

        while (running) {
            printMenu();
            System.out.print("[>] Enter your choice (1-7): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleFetchJoke("Programming", "Programming Joke");
                    break;
                case "2":
                    handleFetchJoke("Misc", "Miscellaneous Joke");
                    break;
                case "3":
                    handleFetchJoke("Pun", "Wordplay / Pun Joke");
                    break;
                case "4":
                    handleFetchJoke("Any", "General Joke (Any Safe Category)");
                    break;
                case "5":
                    handleTestInvalidEndpoint();
                    break;
                case "6":
                    handleTestMalformedJson();
                    break;
                case "7":
                    running = false;
                    printExitMessage();
                    break;
                default:
                    System.out.println("\n[!] INVALID INPUT: Please enter a valid option number between 1 and 7.\n");
                    break;
            }

            if (running) {
                System.out.println("\nPress [ENTER] to return to the main menu...");
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    /**
     * Fetches and displays a joke for the chosen category with safe mode.
     */
    private static void handleFetchJoke(String category, String displayCategory) {
        System.out.println("\n--------------------------------------------------------------------------");
        System.out.println("[INFO] Connecting to JokeAPI (HTTPS) to fetch a " + displayCategory + "...");
        System.out.println("       Target Endpoint : https://v2.jokeapi.dev/joke/" + category + "?safe-mode");
        System.out.println("       HTTP Method     : GET");
        System.out.println("       HTTP Client     : java.net.http.HttpClient");

        try {
            Joke joke = apiService.fetchJoke(category);
            System.out.println("[SUCCESS] HTTP 200 OK received! Payload parsed successfully.\n");
            displayJokeDetails(joke);
        } catch (ApiException e) {
            printErrorMessage("API Request Failed", e.getMessage(), e.getStatusCode());
        } catch (Exception e) {
            printErrorMessage("Unexpected Error", e.getMessage(), -1);
        }
    }

    /**
     * Demonstrates how the application handles non-existent or error-triggering API endpoints.
     */
    private static void handleTestInvalidEndpoint() {
        System.out.println("\n--------------------------------------------------------------------------");
        System.out.println("[TEST] Diagnostic Test: Requesting an invalid category endpoint...");
        System.out.println("       Target Endpoint : https://v2.jokeapi.dev/joke/InvalidCategoryXYZ");

        try {
            apiService.executeGetRequest("https://v2.jokeapi.dev/joke/InvalidCategoryXYZ");
            System.out.println("[!] Notice: Request succeeded unexpectedly.");
        } catch (ApiException e) {
            System.out.println("\n[PASSED] The application gracefully intercepted the API error:");
            System.out.println("         Error Message : " + e.getMessage());
            if (e.getStatusCode() != -1) {
                System.out.println("         HTTP Status   : " + e.getStatusCode());
            }
            System.out.println("         Result        : Handled gracefully without crash.");
        }
    }

    /**
     * Demonstrates how the application handles corrupt or malformed JSON payloads.
     */
    private static void handleTestMalformedJson() {
        System.out.println("\n--------------------------------------------------------------------------");
        System.out.println("[TEST] Diagnostic Test: Simulating malformed JSON payload parsing...");
        String badJson = "{ \"error\": false, \"category\": \"Programming\", \"type\": \"single\", unclosed_syntax... ";

        try {
            apiService.getJsonParser().parseJoke(badJson);
            System.out.println("[!] Notice: Corrupt JSON parsed unexpectedly.");
        } catch (Exception e) {
            System.out.println("\n[PASSED] JsonParserService successfully caught malformed data:");
            System.out.println("         Exception Type: " + e.getClass().getSimpleName());
            System.out.println("         Error Details : " + e.getMessage());
            System.out.println("         Result        : Intercepted safely without crashing the application.");
        }
    }

    /**
     * Displays joke details in a clean, professional ASCII card format.
     */
    private static void displayJokeDetails(Joke joke) {
        String border = "+========================================================================+";
        String divider = "|------------------------------------------------------------------------|";

        System.out.println(border);
        System.out.println("|                     SAIKET SYSTEMS - API DATA VIEW                     |");
        System.out.println(divider);
        System.out.printf("| %-16s : %-53s |\n", "Category", joke.getCategory());
        System.out.printf("| %-16s : %-53s |\n", "Joke ID", "#" + joke.getId());
        System.out.printf("| %-16s : %-53s |\n", "Format Type", joke.isTwoPart() ? "Two-Part (Setup & Delivery)" : "Single-Line");
        System.out.printf("| %-16s : %-53s |\n", "Language", joke.getLang());
        System.out.printf("| %-16s : %-53s |\n", "Content Safety", joke.isSafe() ? "Safe (Safe-Mode Enforced)" : "Standard");
        System.out.println(divider);

        if (joke.isTwoPart()) {
            System.out.println("| [SETUP]                                                                |");
            printWrappedLines(joke.getSetup());
            System.out.println("|                                                                        |");
            System.out.println("| [PUNCHLINE / DELIVERY]                                                 |");
            printWrappedLines(joke.getDelivery());
        } else {
            System.out.println("| [JOKE]                                                                 |");
            printWrappedLines(joke.getJoke());
        }

        System.out.println(border);
    }

    /**
     * Helper method to wrap text cleanly inside the ASCII box.
     */
    private static void printWrappedLines(String text) {
        if (text == null) {
            System.out.println("| (No content available)                                                 |");
            return;
        }

        String[] rawLines = text.split("\r?\n");
        for (String rawLine : rawLines) {
            int maxLen = 68;
            int start = 0;
            while (start < rawLine.length()) {
                int end = Math.min(start + maxLen, rawLine.length());
                String chunk = rawLine.substring(start, end);
                System.out.printf("|   %-68s |\n", chunk);
                start = end;
            }
        }
    }

    /**
     * Prints friendly error information with troubleshooting guidance.
     */
    private static void printErrorMessage(String title, String details, int statusCode) {
        System.out.println("\n+------------------------------------------------------------------------+");
        System.out.println("| [ERROR] " + title);
        System.out.println("+------------------------------------------------------------------------+");
        if (statusCode != -1) {
            System.out.println("  * HTTP Status Code : " + statusCode);
        }
        System.out.println("  * Details          : " + details);
        System.out.println("\n  [Troubleshooting Recommendations]");
        System.out.println("  1. Check that your device has an active Internet connection.");
        System.out.println("  2. Ensure your firewall or proxy does not restrict https://v2.jokeapi.dev");
        System.out.println("  3. Check if the public server is experiencing temporary maintenance.");
        System.out.println("+------------------------------------------------------------------------+");
    }

    /**
     * Prints the application startup banner.
     */
    private static void printBanner() {
        System.out.println("==========================================================================");
        System.out.println("               SAIKET SYSTEMS - INTERNSHIP TASK 2 (JAVA)                  ");
        System.out.println("                Fetch Data from a Public API and Display It               ");
        System.out.println("                       Version " + APP_VERSION + " | JDK 21+                      ");
        System.out.println("==========================================================================");
        System.out.println(" Description: A clean Java console application that securely communicates ");
        System.out.println(" with JokeAPI (REST/HTTPS) using java.net.http.HttpClient, deserializes  ");
        System.out.println(" JSON responses using Jackson, and presents formatted results cleanly.    ");
        System.out.println("==========================================================================\n");
    }

    /**
     * Prints the interactive console menu.
     */
    private static void printMenu() {
        System.out.println("\n=========================== MAIN MENU ===========================");
        System.out.println("  [1] Fetch a Programming Joke (Safe-Mode)");
        System.out.println("  [2] Fetch a Miscellaneous / General Joke");
        System.out.println("  [3] Fetch a Wordplay / Pun Joke");
        System.out.println("  [4] Fetch a Joke from Any Safe Category");
        System.out.println("  [5] Test Error Handling (Invalid API Endpoint)");
        System.out.println("  [6] Test Parser Error Handling (Malformed JSON Simulation)");
        System.out.println("  [7] Exit Application");
        System.out.println("=================================================================");
    }

    /**
     * Prints application exit message.
     */
    private static void printExitMessage() {
        System.out.println("\n==========================================================================");
        System.out.println(" Thank you for using the SaiKet Systems Task 2 API Fetcher!");
        System.out.println(" Application terminated cleanly. Have a great day!");
        System.out.println("==========================================================================\n");
    }
}
