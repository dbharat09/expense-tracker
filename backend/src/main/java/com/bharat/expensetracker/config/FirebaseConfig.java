package com.bharat.expensetracker.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.NoCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;

/**
 * Creates the Firestore client.
 *
 * Credentials resolution order:
 *   1. FIREBASE_SERVICE_ACCOUNT env var — the full service-account JSON.
 *   2. GOOGLE_APPLICATION_CREDENTIALS env var — path to the service-account JSON file.
 *   3. FIRESTORE_EMULATOR_HOST env var — local emulator (no real credentials needed).
 *
 * Fails fast at startup with a clear message when none of the above is set.
 */
@Configuration
public class FirebaseConfig {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Bean
    public Firestore firestore() throws IOException {
        String serviceAccountJson = env("FIREBASE_SERVICE_ACCOUNT");
        String credPath = env("GOOGLE_APPLICATION_CREDENTIALS");
        String emulatorHost = env("FIRESTORE_EMULATOR_HOST");

        GoogleCredentials credentials;
        String projectId;

        if (notBlank(serviceAccountJson)) {
            credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8)));
            projectId = extractProjectId(serviceAccountJson);
        } else if (notBlank(credPath)) {
            try (InputStream in = Files.newInputStream(Paths.get(credPath))) {
                credentials = GoogleCredentials.fromStream(in);
            }
            projectId = env("FIREBASE_PROJECT_ID");
        } else if (notBlank(emulatorHost)) {
            String emulatorProjectId = envOrDefault("FIREBASE_PROJECT_ID", "expense-tracker-demo");
            return FirestoreOptions.newBuilder()
                    .setProjectId(emulatorProjectId)
                    .setEmulatorHost(emulatorHost)
                    .setCredentials(NoCredentials.getInstance())
                    .build()
                    .getService();
        } else {
            throw new IllegalStateException(
                    "No Firebase credentials configured. Set one of: "
                    + "FIREBASE_SERVICE_ACCOUNT (full service-account JSON), "
                    + "GOOGLE_APPLICATION_CREDENTIALS (path to service-account JSON file), or "
                    + "FIRESTORE_EMULATOR_HOST (e.g. localhost:8080) for local emulator testing.");
        }

        FirestoreOptions.Builder builder = FirestoreOptions.newBuilder().setCredentials(credentials);
        if (notBlank(projectId)) {
            builder.setProjectId(projectId);
        }
        return builder.build().getService();
    }

    private String extractProjectId(String serviceAccountJson) {
        try {
            Map<?, ?> map = MAPPER.readValue(serviceAccountJson, Map.class);
            Object pid = map.get("project_id");
            return pid != null ? pid.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String env(String name) {
        return System.getenv(name);
    }

    private static String envOrDefault(String name, String def) {
        String v = System.getenv(name);
        return notBlank(v) ? v : def;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
