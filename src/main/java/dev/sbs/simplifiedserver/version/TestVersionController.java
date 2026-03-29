package dev.sbs.simplifiedserver.version;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example controller demonstrating URL-path-based API versioning with {@link ApiVersion}.
 *
 * <p>Versioned endpoints are accessible at {@code /v{N}/path} (e.g., {@code /v1/hello}).
 * The {@code /default} endpoint is unversioned and always accessible at its base path.</p>
 */
@RestController
public class TestVersionController {

    @ApiVersion(1)
    @GetMapping("/hello")
    public @NotNull ResponseEntity<String> getHelloV1() {
        return ResponseEntity.ok("Hello from API v1!");
    }

    @ApiVersion(2)
    @GetMapping("/hello")
    public @NotNull ResponseEntity<String> getHelloV2() {
        return ResponseEntity.ok("Hello from API v2!");
    }

    @ApiVersion(3)
    @GetMapping("/hello")
    public @NotNull ResponseEntity<String> getHelloV3() {
        return ResponseEntity.ok("Hello from API v3!");
    }

    @ApiVersion(1)
    @GetMapping("/data")
    public @NotNull ResponseEntity<String> getDataV1() {
        return ResponseEntity.ok("Data for API v1: { id: 1, name: 'Item One' }");
    }

    @ApiVersion(2)
    @GetMapping("/data")
    public @NotNull ResponseEntity<String> getDataV2() {
        return ResponseEntity.ok("Data for API v2: { itemId: 1, itemName: 'Item One Updated' }");
    }

    @GetMapping("/default")
    public @NotNull ResponseEntity<String> getDefaultHello() {
        return ResponseEntity.ok("Hello from default (unversioned) endpoint!");
    }

}
