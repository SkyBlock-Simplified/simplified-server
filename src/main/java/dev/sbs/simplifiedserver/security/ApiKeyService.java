package dev.sbs.simplifiedserver.security;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Manages API key storage, validation, rate limiting, and permission resolution.
 *
 * <p>Currently uses hardcoded test keys. The permission check delegates to
 * {@link ApiKeyRoleHierarchy} for role expansion before matching.</p>
 */
@RequiredArgsConstructor
public class ApiKeyService {

    private final @NotNull ConcurrentMap<String, ApiKey> apiKeys = Concurrent.newMap();
    private final @NotNull ApiKeyRoleHierarchy hierarchyService;

    {
        apiKeys.put("dev-key-777", new ApiKey("dev-key-777",
            Concurrent.newSet(Concurrent.newList("DEVELOPER")), 100, 60));

        apiKeys.put("mod-key-555", new ApiKey("mod-key-555",
            Concurrent.newSet(Concurrent.newList("MODERATOR")), 50, 60));

        apiKeys.put("service-key-123", new ApiKey("service-key-123",
            Concurrent.newSet(Concurrent.newList("USER", "RESTART")), 10, 60));
    }

    /**
     * Checks whether the given key string corresponds to a registered API key.
     *
     * @param apiKey the key string to validate
     * @return {@code true} if the key is registered
     */
    public boolean isValidApiKey(@NotNull String apiKey) {
        return apiKeys.containsKey(apiKey);
    }

    /**
     * Checks whether the given API key has exceeded its rate limit.
     *
     * @param apiKey the key string to check
     * @return {@code true} if the key is rate-limited and the request should be rejected
     */
    public boolean isRateLimited(@NotNull String apiKey) {
        ApiKey key = apiKeys.get(apiKey);
        return key != null && !key.allowRequest();
    }

    /**
     * Checks whether the given API key satisfies at least one of the required permissions,
     * accounting for role hierarchy expansion.
     *
     * @param apiKey the key string to check
     * @param requiredPermissions the permissions required (any-match semantics)
     * @return {@code true} if the key has at least one of the required permissions
     */
    public boolean hasPermission(@NotNull String apiKey, @NotNull String[] requiredPermissions) {
        ApiKey key = apiKeys.get(apiKey);
        if (key == null) return false;

        if (requiredPermissions.length == 0)
            return true;

        Set<String> reachable = hierarchyService.getReachablePermissions(key.getPermissions());

        for (String required : requiredPermissions) {
            if (reachable.contains(required))
                return true;
        }

        return false;
    }

}
