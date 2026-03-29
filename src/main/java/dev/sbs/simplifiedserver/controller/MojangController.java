package dev.sbs.simplifiedserver.controller;

import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.minecraftapi.client.mojang.MojangProxy;
import dev.sbs.minecraftapi.client.mojang.request.MojangEndpoint;
import dev.sbs.minecraftapi.client.mojang.response.MojangMultiUsername;
import dev.sbs.minecraftapi.client.mojang.response.MojangProfile;
import dev.sbs.minecraftapi.client.mojang.response.MojangProperties;
import dev.sbs.minecraftapi.client.mojang.response.MojangUsername;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.UUID;

/**
 * Mojang API proxy endpoints under {@code /mojang/}.
 *
 * <p>Provides player profile lookups, username resolution, skin properties,
 * and bulk username lookups. Delegates to the {@link MojangProxy} for upstream
 * calls with automatic IPv6 rotation to avoid rate limits.</p>
 */
@Tag(name = "Mojang", description = "Mojang API proxy endpoints")
@RestController
@RequestMapping("/mojang")
public class MojangController {

    private @NotNull MojangProxy proxy() {
        return MinecraftApi.getMojangProxy();
    }

    private @NotNull MojangEndpoint endpoint() {
        return proxy().getEndpoint();
    }

    /**
     * Fetches a player's profile by username or UUID string.
     *
     * <p>Attempts to parse the identifier as a {@link UUID} first; if that fails,
     * treats it as a username. Returns the full profile including Base64-encoded
     * skin and cape texture data.</p>
     *
     * @param identifier a Minecraft username or UUID string
     * @return the Mojang profile with texture data
     */
    @Operation(summary = "Lookup player profile", description = "Fetches a player's profile by username or UUID string. Attempts to parse the identifier as a UUID first; if that fails, treats it as a username. Returns the full profile including Base64-encoded skin and cape texture data.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/user/{identifier}")
    public @NotNull MojangProfile getUser(@Parameter(description = "Minecraft username or UUID") @NotNull @PathVariable String identifier) {
        try {
            UUID uniqueId = UUID.fromString(identifier);
            return proxy().getMojangProfile(uniqueId);
        } catch (IllegalArgumentException ignored) {
            return proxy().getMojangProfile(identifier);
        }
    }

    /**
     * Fetches a player's unique id and case-corrected username by their
     * case-insensitive username.
     *
     * @param username the case-insensitive player username
     * @return the profile containing the player's unique id and username
     */
    @Operation(summary = "Resolve username", description = "Fetches a player's unique id and case-corrected username by their case-insensitive username.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/username/{username}")
    public @NotNull MojangUsername getUsername(@Parameter(description = "Case-insensitive Minecraft username") @NotNull @PathVariable String username) {
        return endpoint().getPlayer(username);
    }

    /**
     * Fetches a player's username and profile status by unique id.
     *
     * @param uniqueId the player's unique id
     * @return the profile containing the player's unique id and username
     */
    @Operation(summary = "Resolve UUID", description = "Fetches a player's username and profile status by unique id.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/uuid/{uniqueId}")
    public @NotNull MojangUsername getUniqueId(@Parameter(description = "Player UUID") @NotNull @PathVariable UUID uniqueId) {
        return endpoint().getPlayer(uniqueId);
    }

    /**
     * Fetches a player's profile properties, including Base64-encoded skin
     * and cape texture data with cryptographic signatures.
     *
     * @param uniqueId the player's unique id
     * @return the profile properties including signed texture data
     */
    @Operation(summary = "Fetch skin properties", description = "Fetches a player's profile properties, including Base64-encoded skin and cape texture data with cryptographic signatures.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/properties/{uniqueId}")
    public @NotNull MojangProperties getProperties(@Parameter(description = "Player UUID") @NotNull @PathVariable UUID uniqueId) {
        return endpoint().getProperties(uniqueId);
    }

    /**
     * Fetches multiple player profiles by username in bulk.
     *
     * <p>Accepts up to 10 case-insensitive usernames per request. Players that
     * do not exist are silently omitted from the response.</p>
     *
     * @param usernames the player usernames to look up
     * @return the bulk username lookup response containing matched profiles
     */
    @Operation(summary = "Bulk username lookup", description = "Fetches multiple player profiles by username in bulk. Accepts up to 10 case-insensitive usernames per request. Players that do not exist are silently omitted from the response.")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/bulk")
    public @NotNull MojangMultiUsername getMultipleUniqueIds(@RequestBody @NotNull Collection<String> usernames) {
        return endpoint().getMultipleUniqueIds(usernames);
    }

}
