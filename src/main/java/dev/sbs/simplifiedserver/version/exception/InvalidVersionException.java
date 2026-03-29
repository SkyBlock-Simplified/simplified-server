package dev.sbs.simplifiedserver.version.exception;

import dev.sbs.api.collection.concurrent.ConcurrentSet;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a request specifies an API version that does not exist for the target endpoint.
 */
public final class InvalidVersionException extends VersionException {

    /**
     * Constructs a new {@code InvalidVersionException}.
     *
     * @param requestedVersion the version number that was requested
     * @param basePath the base endpoint path without version prefix
     * @param availableVersions the set of versions registered for the path
     */
    public InvalidVersionException(int requestedVersion, @NotNull String basePath, @NotNull ConcurrentSet<Integer> availableVersions) {
        super(HttpStatus.NOT_FOUND, "API version %d does not exist for '%s'; available versions: %s", requestedVersion, basePath, availableVersions);
    }

}
