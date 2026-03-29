package dev.sbs.simplifiedserver.version;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentMap;
import dev.sbs.api.collection.concurrent.ConcurrentSet;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.util.pattern.PathPattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Precomputed index of versioned paths, mapping each base path to its set of
 * available version numbers.
 *
 * <p>Built at startup from the registered handler mappings in
 * {@link ApiVersionRequestMappingHandlerMapping}. Used by
 * {@link ApiVersionInterceptor} and the error controller to detect invalid
 * or missing version prefixes.</p>
 */
@RequiredArgsConstructor
public class VersionRegistryService {

    private static final @NotNull Pattern VERSION_PREFIX = Pattern.compile("^/v(\\d+)(/.*)$");

    private final @NotNull ApiVersionRequestMappingHandlerMapping handlerMapping;
    private @NotNull ConcurrentMap<String, ConcurrentSet<Integer>> versionedPaths = Concurrent.newMap();

    @PostConstruct
    void buildIndex() {
        ConcurrentMap<String, ConcurrentSet<Integer>> index = Concurrent.newMap();

        for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            if (info.getPathPatternsCondition() == null) continue;

            for (PathPattern pattern : info.getPathPatternsCondition().getPatterns()) {
                Matcher matcher = VERSION_PREFIX.matcher(pattern.getPatternString());
                if (matcher.matches()) {
                    int version = Integer.parseInt(matcher.group(1));
                    String basePath = matcher.group(2);
                    index.computeIfAbsent(basePath, k -> Concurrent.newSet()).add(version);
                }
            }
        }

        this.versionedPaths = index;
    }

    /**
     * Returns the set of available version numbers for the given base path,
     * or {@code null} if the path has no versioned handlers.
     *
     * @param basePath the base path without version prefix (e.g., {@code /hello})
     * @return the available versions, or {@code null}
     */
    public @Nullable ConcurrentSet<Integer> getVersionsForPath(@NotNull String basePath) {
        return versionedPaths.get(basePath);
    }

    /**
     * Returns {@code true} if the given base path has versioned handlers registered.
     *
     * @param basePath the base path without version prefix
     * @return whether versioned handlers exist for the path
     */
    public boolean isVersionedPath(@NotNull String basePath) {
        return versionedPaths.containsKey(basePath);
    }

}
