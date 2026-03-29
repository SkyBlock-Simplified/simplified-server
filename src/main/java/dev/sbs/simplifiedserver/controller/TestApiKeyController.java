package dev.sbs.simplifiedserver.controller;

import dev.sbs.simplifiedserver.security.ApiKeyProtected;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example controller demonstrating {@link ApiKeyProtected} with role and static
 * permission requirements under the {@code /api/} path prefix.
 */
@RestController
@RequestMapping("/api")
public class TestApiKeyController {

    @GetMapping("/admin-panel")
    @ApiKeyProtected(requiredPermissions = {"ADMIN"})
    public @NotNull ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Welcome, Administrator.");
    }

    @PostMapping("/restart")
    @ApiKeyProtected(requiredPermissions = {"RESTART", "DEVELOPER"})
    public @NotNull ResponseEntity<String> restartService() {
        return ResponseEntity.ok("Service is restarting...");
    }

    @GetMapping("/basic")
    @ApiKeyProtected(requiredPermissions = {"USER"})
    public @NotNull ResponseEntity<String> basicAccess() {
        return ResponseEntity.ok("Basic user access granted.");
    }

}
