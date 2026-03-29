package dev.sbs.simplifiedserver.version;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import java.util.Arrays;

/**
 * Custom {@link RequestCondition} that holds version numbers for combine and compare
 * purposes in URL-path-based API versioning.
 *
 * <p>Since the path prefix (e.g., {@code /v1}) is already prepended by
 * {@link ApiVersionRequestMappingHandlerMapping}, the matching condition always
 * returns {@code this} - routing is handled entirely by the path.</p>
 */
public class ApiVersionCondition implements RequestCondition<ApiVersionCondition> {

    private final int @NotNull [] apiVersions;

    /**
     * Constructs a new condition for the given version numbers.
     *
     * @param apiVersions the supported version numbers
     */
    public ApiVersionCondition(int @NotNull [] apiVersions) {
        this.apiVersions = Arrays.copyOf(apiVersions, apiVersions.length);
    }

    /**
     * Combines this condition with another, with method-level taking precedence
     * over class-level.
     *
     * @param other the method-level condition
     * @return the combined condition (method-level wins)
     */
    @Override
    public @NotNull ApiVersionCondition combine(@NotNull ApiVersionCondition other) {
        return new ApiVersionCondition(other.apiVersions);
    }

    /**
     * Returns this condition unconditionally since path-based routing handles version matching.
     *
     * @param request the current request
     * @return this condition
     */
    @Override
    public @Nullable ApiVersionCondition getMatchingCondition(@NotNull HttpServletRequest request) {
        return this;
    }

    /**
     * Compares conditions for specificity, preferring higher version numbers.
     *
     * @param other the other condition
     * @param request the current request
     * @return comparison result (higher version first)
     */
    @Override
    public int compareTo(@NotNull ApiVersionCondition other, @NotNull HttpServletRequest request) {
        if (this.apiVersions.length > 0 && other.apiVersions.length > 0)
            return Integer.compare(other.apiVersions[0], this.apiVersions[0]);

        return 0;
    }

    @Override
    public @NotNull String toString() {
        return "ApiVersionCondition [apiVersions=" + Arrays.toString(apiVersions) + "]";
    }

}
