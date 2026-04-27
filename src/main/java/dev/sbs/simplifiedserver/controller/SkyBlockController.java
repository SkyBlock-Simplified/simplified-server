package dev.sbs.simplifiedserver.controller;

import api.simplified.hypixel.HypixelContract;
import api.simplified.hypixel.response.skyblock.SkyBlockAuctionResponse;
import api.simplified.hypixel.response.skyblock.SkyBlockAuctions;
import api.simplified.hypixel.response.skyblock.SkyBlockAuctionsEnded;
import api.simplified.hypixel.response.skyblock.SkyBlockBazaar;
import api.simplified.hypixel.response.skyblock.SkyBlockFireSaleResponse;
import api.simplified.hypixel.response.skyblock.SkyBlockGardenResponse;
import api.simplified.hypixel.response.skyblock.SkyBlockMuseumResponse;
import api.simplified.hypixel.response.skyblock.SkyBlockNews;
import api.simplified.hypixel.response.skyblock.SkyBlockProfiles;
import dev.sbs.simplifiedserver.ServerApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * SkyBlock API proxy endpoints under {@code /skyblock/}.
 *
 * <p>Provides SkyBlock-specific data including profiles, auctions, bazaar,
 * museum, garden, news, and fire sales. Delegates to the {@link HypixelContract}
 * Feign client for upstream calls.</p>
 */
@Tag(name = "SkyBlock", description = "SkyBlock API proxy endpoints")
@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/skyblock")
public class SkyBlockController {

    private @NotNull HypixelContract contract() {
        return ServerApi.getHypixelClient().getContract();
    }

    /**
     * Fetches all SkyBlock profiles for the given player, including skills,
     * collections, stats, and objectives.
     *
     * <p>The data returned may vary depending on each player's in-game API settings.</p>
     *
     * @param playerId the player's unique id
     * @return the profiles response
     */
    @Operation(summary = "Get profiles", description = "Fetches all SkyBlock profiles for the given player, including skills, collections, stats, and objectives. The data returned may vary depending on each player's in-game API settings.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/profiles/{playerId}")
    public @NotNull SkyBlockProfiles getProfiles(@Parameter(description = "Player UUID") @NotNull @PathVariable UUID playerId) {
        return contract().getProfiles(playerId);
    }

    /**
     * Fetches museum data for all members of the given SkyBlock profile.
     *
     * <p>The data returned may vary depending on each player's in-game API settings.</p>
     *
     * @param islandId the SkyBlock profile id
     * @return the museum data response
     */
    @Operation(summary = "Get museum", description = "Fetches museum data for all members of the given SkyBlock profile. The data returned may vary depending on each player's in-game API settings.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/museum/{islandId}")
    public @NotNull SkyBlockMuseumResponse getMuseum(@Parameter(description = "SkyBlock profile ID") @NotNull @PathVariable UUID islandId) {
        return contract().getMuseum(islandId);
    }

    /**
     * Fetches garden data for the given SkyBlock profile.
     *
     * @param islandId the SkyBlock profile id
     * @return the garden data response
     */
    @Operation(summary = "Get garden", description = "Fetches garden data for the given SkyBlock profile.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/garden/{islandId}")
    public @NotNull SkyBlockGardenResponse getGarden(@Parameter(description = "SkyBlock profile ID") @NotNull @PathVariable UUID islandId) {
        return contract().getGarden(islandId);
    }

    /**
     * Fetches recent SkyBlock news and announcements.
     *
     * @return the news articles
     */
    @Operation(summary = "Get news", description = "Fetches recent SkyBlock news and announcements.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/news")
    public @NotNull SkyBlockNews getNews() {
        return contract().getNews();
    }

    /**
     * Fetches all Bazaar products with their sell summary, buy summary, and
     * quick status. Does not require an API key.
     *
     * @return the bazaar product data
     */
    @Operation(summary = "Get bazaar", description = "Fetches all Bazaar products with their sell summary, buy summary, and quick status. Does not require an API key.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/bazaar")
    public @NotNull SkyBlockBazaar getBazaar() {
        return contract().getBazaar();
    }

    /**
     * Fetches a specific auction by its auction id.
     *
     * @param auctionId the auction's unique id
     * @return the auction response
     */
    @Operation(summary = "Get auction by ID", description = "Fetches a specific auction by its auction id.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/auction/id/{auctionId}")
    public @NotNull SkyBlockAuctionResponse getAuctionById(@Parameter(description = "Auction UUID") @NotNull @PathVariable UUID auctionId) {
        return contract().getAuctionById(auctionId);
    }

    /**
     * Fetches all auctions belonging to the given SkyBlock profile.
     *
     * @param islandId the SkyBlock profile id
     * @return the auction response
     */
    @Operation(summary = "Get auctions by profile", description = "Fetches all auctions belonging to the given SkyBlock profile.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/auction/profile/{islandId}")
    public @NotNull SkyBlockAuctionResponse getAuctionByIsland(@Parameter(description = "SkyBlock profile ID") @NotNull @PathVariable UUID islandId) {
        return contract().getAuctionByIsland(islandId);
    }

    /**
     * Fetches all auctions created by the given player.
     *
     * @param playerId the player's unique id
     * @return the auction response
     */
    @Operation(summary = "Get auctions by player", description = "Fetches all auctions created by the given player.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/auction/player/{playerId}")
    public @NotNull SkyBlockAuctionResponse getAuctionByPlayer(@Parameter(description = "Player UUID") @NotNull @PathVariable UUID playerId) {
        return contract().getAuctionByPlayer(playerId);
    }

    /**
     * Fetches all currently active auctions, sorted by most recently updated
     * and paginated. Does not require an API key.
     *
     * @return the first page of active auctions
     */
    @Operation(summary = "Get active auctions", description = "Fetches all currently active auctions, sorted by most recently updated and paginated. Does not require an API key.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/auctions")
    public @NotNull SkyBlockAuctions getAuctions() {
        return contract().getAuctions();
    }

    /**
     * Fetches a specific page of currently active auctions, sorted by most
     * recently updated. Does not require an API key.
     *
     * @param page the zero-based page number
     * @return the requested page of active auctions
     */
    @Operation(summary = "Get active auctions by page", description = "Fetches a specific page of currently active auctions, sorted by most recently updated. Does not require an API key.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/auctions/{page}")
    public @NotNull SkyBlockAuctions getAuctions(@Parameter(description = "Zero-based page number") @NotNull @PathVariable Integer page) {
        return contract().getAuctions(page);
    }

    /**
     * Fetches auctions that ended within the last 60 seconds. Does not require
     * an API key.
     *
     * @return the recently ended auctions
     */
    @Operation(summary = "Get ended auctions", description = "Fetches auctions that ended within the last 60 seconds. Does not require an API key.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/auctions/ended")
    public @NotNull SkyBlockAuctionsEnded getEndedAuctions() {
        return contract().getEndedAuctions();
    }

    /**
     * Fetches the currently active or upcoming SkyBlock Fire Sales. Does not
     * require an API key.
     *
     * @return the fire sale data
     */
    @Operation(summary = "Get fire sales", description = "Fetches the currently active or upcoming SkyBlock Fire Sales. Does not require an API key.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/firesales")
    public @NotNull SkyBlockFireSaleResponse getFireSales() {
        return contract().getFireSales();
    }

}
