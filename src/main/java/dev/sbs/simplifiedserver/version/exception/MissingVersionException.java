package dev.sbs.simplifiedserver.version.exception;

import dev.sbs.api.collection.concurrent.ConcurrentSet;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a request targets a multi-versioned endpoint without specifying a version
 * prefix and no version 1 handler exists for the path.
 */
public final class MissingVersionException extends VersionException {

    /**
     * Constructs a new {@code MissingVersionException}.
     *
     * @param endpoint the requested endpoint path
     * @param availableVersions the set of versions registered for the path
     */
    public MissingVersionException(@NotNull String endpoint, @NotNull ConcurrentSet<Integer> availableVersions) {
        super(HttpStatus.BAD_REQUEST, "Endpoint '%s' requires a version prefix; available versions: %s", endpoint, availableVersions);
    }

}
