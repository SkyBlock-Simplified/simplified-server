package dev.sbs.simplifiedserver.controller;

import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.minecraftapi.client.hypixel.HypixelClient;
import dev.sbs.minecraftapi.client.hypixel.request.HypixelEndpoint;
import dev.sbs.minecraftapi.client.hypixel.response.resource.ResourceCollections;
import dev.sbs.minecraftapi.client.hypixel.response.resource.ResourceElection;
import dev.sbs.minecraftapi.client.hypixel.response.resource.ResourceItems;
import dev.sbs.minecraftapi.client.hypixel.response.resource.ResourceSkills;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * SkyBlock resource proxy endpoints under {@code /resources/}.
 *
 * <p>Provides publicly accessible SkyBlock resource definitions including skills,
 * collections, items, and election data. None of these endpoints require an API key.
 * Delegates to the {@link HypixelEndpoint} Feign client for upstream calls.</p>
 */
@Tag(name = "Resources", description = "SkyBlock resource endpoints (no API key required)")
@RestController
@RequestMapping("/resources")
public class ResourceController {

    private @NotNull HypixelEndpoint endpoint() {
        return MinecraftApi.getClient(HypixelClient.class).getEndpoint();
    }

    /**
     * Fetches SkyBlock skill definitions and leveling data. Does not require an
     * API key.
     *
     * @return the skill information
     */
    @Operation(summary = "Get skills", description = "Fetches SkyBlock skill definitions and leveling data. Does not require an API key.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/skills")
    public @NotNull ResourceSkills getSkills() {
        return endpoint().getSkills();
    }

    /**
     * Fetches SkyBlock collection definitions and tier data. Does not require an
     * API key.
     *
     * @return the collection information
     */
    @Operation(summary = "Get collections", description = "Fetches SkyBlock collection definitions and tier data. Does not require an API key.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/collections")
    public @NotNull ResourceCollections getCollections() {
        return endpoint().getCollections();
    }

    /**
     * Fetches SkyBlock item definitions. Does not require an API key.
     *
     * @return the item information
     */
    @Operation(summary = "Get items", description = "Fetches SkyBlock item definitions. Does not require an API key.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/items")
    public @NotNull ResourceItems getItems() {
        return endpoint().getItems();
    }

    /**
     * Fetches the current SkyBlock mayor and ongoing election data. Does not
     * require an API key.
     *
     * @return the election and mayor information
     */
    @Operation(summary = "Get election", description = "Fetches the current SkyBlock mayor and ongoing election data. Does not require an API key.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/election")
    public @NotNull ResourceElection getElection() {
        return endpoint().getElection();
    }

}
