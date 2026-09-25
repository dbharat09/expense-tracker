package com.bharat.expensetracker.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Liveness probe. Deliberately depends on nothing — in particular not on
 * {@code ExpenseService} or the Firestore client — so the health check itself
 * never touches Firestore and stays reachable regardless of backend state.
 * (Note: the Firestore bean itself still fails fast at startup when no
 * credentials are configured; see {@code FirebaseConfig}.)
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
