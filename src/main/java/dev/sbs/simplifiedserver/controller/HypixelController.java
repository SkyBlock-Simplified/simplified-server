package dev.sbs.simplifiedserver.controller;

import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.minecraftapi.client.hypixel.HypixelClient;
import dev.sbs.minecraftapi.client.hypixel.request.HypixelEndpoint;
import dev.sbs.minecraftapi.client.hypixel.response.hypixel.HypixelCounts;
import dev.sbs.minecraftapi.client.hypixel.response.hypixel.HypixelGuildResponse;
import dev.sbs.minecraftapi.client.hypixel.response.hypixel.HypixelPlayerResponse;
import dev.sbs.minecraftapi.client.hypixel.response.hypixel.HypixelPunishmentStats;
import dev.sbs.minecraftapi.client.hypixel.response.hypixel.HypixelStatus;
import dev.sbs.minecraftapi.client.hypixel.response.resource.ResourceGames;
import dev.sbs.serverapi.security.ApiKeyProtected;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Hypixel API proxy endpoints under {@code /hypixel/}.
 *
 * <p>Provides player data, guild lookups, online status, punishment statistics,
 * player counts, and game information. Delegates to the {@link HypixelEndpoint}
 * Feign client for upstream calls.</p>
 */
@Tag(name = "Hypixel", description = "Hypixel API proxy endpoints")
@RestController
@ApiKeyProtected
@RequestMapping("/hypixel")
public class HypixelController {

    private @NotNull HypixelEndpoint endpoint() {
        return MinecraftApi.getClient(HypixelClient.class).getEndpoint();
    }

    /**
     * Fetches the current player counts for all Hypixel games.
     *
     * @return the current player count data
     */
    @Operation(summary = "Get player counts", description = "Fetches the current player counts for all Hypixel games.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/counts")
    public @NotNull HypixelCounts getCounts() {
        return endpoint().getCounts();
    }

    /**
     * Fetches a guild by its guild id.
     *
     * @param guildId the guild id
     * @return the guild response
     */
    @Operation(summary = "Get guild by ID", description = "Fetches a guild by its guild id.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/guild/id/{guildId}")
    public @NotNull HypixelGuildResponse getGuildById(@Parameter(description = "Guild ID") @NotNull @PathVariable String guildId) {
        return endpoint().getGuildById(guildId);
    }

    /**
     * Fetches a guild by its name.
     *
     * @param guildName the guild name
     * @return the guild response
     */
    @Operation(summary = "Get guild by name", description = "Fetches a guild by its name.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/guild/name/{guildName}")
    public @NotNull HypixelGuildResponse getGuildByName(@Parameter(description = "Guild name") @NotNull @PathVariable String guildName) {
        return endpoint().getGuildByName(guildName);
    }

    /**
     * Fetches the guild that a player belongs to.
     *
     * @param playerId the player's unique id
     * @return the guild response
     */
    @Operation(summary = "Get guild by player", description = "Fetches the guild that a player belongs to.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/guild/player/{playerId}")
    public @NotNull HypixelGuildResponse getGuildByPlayer(@Parameter(description = "Player UUID") @NotNull @PathVariable UUID playerId) {
        return endpoint().getGuildByPlayer(playerId);
    }

    /**
     * Fetches the data and game stats of a specific player.
     *
     * @param playerId the player's unique id
     * @return the player data response
     */
    @Operation(summary = "Get player", description = "Fetches the data and game stats of a specific player.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/player/{playerId}")
    public @NotNull HypixelPlayerResponse getPlayer(@Parameter(description = "Player UUID") @NotNull @PathVariable UUID playerId) {
        return endpoint().getPlayer(playerId);
    }

    /**
     * Fetches the network-wide punishment statistics.
     *
     * @return the punishment statistics
     */
    @Operation(summary = "Get punishment stats", description = "Fetches the network-wide punishment statistics.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/punishmentstats")
    public @NotNull HypixelPunishmentStats getPunishmentStats() {
        return endpoint().getPunishmentStats();
    }

    /**
     * Fetches the current online status of a specific player.
     *
     * @param playerId the player's unique id
     * @return the player's online status
     */
    @Operation(summary = "Get player status", description = "Fetches the current online status of a specific player.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/status/{playerId}")
    public @NotNull HypixelStatus getStatus(@Parameter(description = "Player UUID") @NotNull @PathVariable UUID playerId) {
        return endpoint().getStatus(playerId);
    }

    /**
     * Fetches information about all Hypixel games. Does not require an API key.
     *
     * @return the game information
     */
    @Operation(summary = "Get games", description = "Fetches information about all Hypixel games. Does not require an API key.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/games")
    public @NotNull ResourceGames getGames() {
        return endpoint().getGames();
    }

}
