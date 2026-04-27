package dev.sbs.simplifiedserver.controller;

import api.simplified.hypixel.HypixelContract;
import api.simplified.hypixel.response.resource.ResourceCollections;
import api.simplified.hypixel.response.resource.ResourceElection;
import api.simplified.hypixel.response.resource.ResourceItems;
import api.simplified.hypixel.response.resource.ResourceSkills;
import dev.sbs.simplifiedserver.ServerApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * SkyBlock resource proxy endpoints under {@code /resources/}.
 *
 * <p>Provides SkyBlock resource definitions including skills, collections, items, and
 * election data. Delegates to the {@link HypixelContract} Feign client for upstream calls.</p>
 */
@Tag(name = "Resources", description = "SkyBlock resource endpoints")
@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/resources")
public class ResourceController {

    private @NotNull HypixelContract contract() {
        return ServerApi.getHypixelClient().getContract();
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
        return contract().getSkills();
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
        return contract().getCollections();
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
        return contract().getItems();
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
        return contract().getElection();
    }

}
