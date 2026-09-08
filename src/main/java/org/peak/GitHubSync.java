package org.peak;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;
import java.util.Properties;

public class GitHubSync {

    // The three files we sync
    private static final String[] FILES = {
            "assignments.csv",
            "categories.csv",
            "classes.csv",
            "completed.csv"
    };

    private static String token;
    private static String username;
    private static String repo;

    // HttpClient is Java's built-in HTTP tool (Java 11+) — no Maven dependency needed
    private static final HttpClient client = HttpClient.newHttpClient();

    // ─── LOAD CONFIG ──────────────────────────────────────────

    // Reads github.properties so the token never lives in code
    private static boolean loadConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("github.properties")) {
            props.load(fis);
            token    = props.getProperty("token");
            username = props.getProperty("username");
            repo     = props.getProperty("repo");

            if (token == null || username == null || repo == null) {
                System.out.println("Warning: github.properties is missing token, username, or repo.");
                return false;
            }
            return true;
        } catch (IOException e) {
            System.out.println("Warning: github.properties not found — sync disabled.");
            return false;
        }
    }

    // ─── PUBLIC: PULL ON STARTUP ──────────────────────────────

    // Downloads all CSV files from GitHub and overwrites local copies
    public static void pull() {
        if (!loadConfig()) return;

        System.out.println("Fetching latest data from GitHub...");
        boolean anyFailed = false;

        for (String filename : FILES) {
            try {
                String content = downloadFile(filename);
                if (content != null) {
                    // Overwrite the local file with whatever GitHub has
                    Files.writeString(Path.of(filename), content, StandardCharsets.UTF_8);
                }
                // If content is null the file doesn't exist on GitHub yet — that's fine on first run
            } catch (Exception e) {
                System.out.println("  Warning: could not fetch " + filename + " — " + e.getMessage());
                anyFailed = true;
            }
        }

        if (!anyFailed) {
            System.out.println("Data loaded from GitHub successfully.");
        }
    }

    // ─── PUBLIC: PUSH ON EXIT ─────────────────────────────────

    // Uploads all local CSV files to GitHub
    public static void push() {
        if (!loadConfig()) return;

        System.out.println("Saving data to GitHub...");
        boolean anyFailed = false;

        for (String filename : FILES) {
            File localFile = new File(filename);

            // If the file doesn't exist locally (e.g. no completed assignments yet), skip it
            if (!localFile.exists()) continue;

            try {
                uploadFile(filename);
            } catch (Exception e) {
                System.out.println("  Warning: could not upload " + filename + " — " + e.getMessage());
                anyFailed = true;
            }
        }

        if (!anyFailed) {
            System.out.println("Data saved to GitHub successfully.");
        }
    }

    // ─── DOWNLOAD ONE FILE ────────────────────────────────────

    // Returns the decoded file content as a String, or null if the file doesn't exist yet
    private static String downloadFile(String filename) throws Exception {
        String url = apiUrl(filename);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            return null; // File doesn't exist on GitHub yet — first run, that's fine
        }

        if (response.statusCode() != 200) {
            throw new Exception("HTTP " + response.statusCode());
        }

        // GitHub returns JSON — we need to pull out the "content" field manually
        // We're doing this without a JSON library to avoid adding Maven dependencies
        String body = response.body();
        String content = extractJsonField(body, "content");

        if (content == null) {
            throw new Exception("Could not read content field from GitHub response");
        }

        // GitHub Base64-encodes the content and adds newlines — strip them before decoding
        content = content.replace("\\n", "").replace("\n", "");
        byte[] decoded = Base64.getDecoder().decode(content);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    // ─── UPLOAD ONE FILE ──────────────────────────────────────

    private static void uploadFile(String filename) throws Exception {
        // Step 1: get the current SHA (required by GitHub to update an existing file)
        String sha = getFileSha(filename); // null if file doesn't exist on GitHub yet

        // Step 2: read and encode the local file
        String localContent = Files.readString(Path.of(filename), StandardCharsets.UTF_8);
        String encoded = Base64.getEncoder().encodeToString(
                localContent.getBytes(StandardCharsets.UTF_8)
        );

        // Step 3: build the JSON body
        // We build JSON manually to avoid needing a dependency like Gson
        String jsonBody;
        if (sha != null) {
            // Updating an existing file — must include SHA
            jsonBody = "{"
                    + "\"message\":\"update " + filename + "\","
                    + "\"content\":\"" + encoded + "\","
                    + "\"sha\":\"" + sha + "\""
                    + "}";
        } else {
            // Creating the file for the first time
            jsonBody = "{"
                    + "\"message\":\"create " + filename + "\","
                    + "\"content\":\"" + encoded + "\""
                    + "}";
        }

        // Step 4: send the PUT request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl(filename)))
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new Exception("HTTP " + response.statusCode() + " — " + response.body());
        }
    }

    // ─── GET SHA ──────────────────────────────────────────────

    // GitHub requires the current file SHA when updating a file
    // Returns null if the file doesn't exist yet on GitHub
    private static String getFileSha(String filename) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl(filename)))
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github.v3+json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) return null; // File doesn't exist yet

        if (response.statusCode() != 200) {
            throw new Exception("HTTP " + response.statusCode());
        }

        return extractJsonField(response.body(), "sha");
    }

    // ─── HELPERS ──────────────────────────────────────────────

    private static String apiUrl(String filename) {
        return "https://api.github.com/repos/" + username + "/" + repo + "/contents/" + filename;
    }

    // Pulls a string value out of a flat JSON response without a JSON library
    // Looks for "key":"value" and returns value
    // This is intentionally simple — it only needs to handle GitHub's flat response fields
    private static String extractJsonField(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;

        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;

        return json.substring(start, end);
    }
}