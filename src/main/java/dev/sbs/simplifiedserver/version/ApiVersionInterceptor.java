package dev.sbs.simplifiedserver.version;

import dev.sbs.api.collection.concurrent.ConcurrentSet;
import dev.sbs.simplifiedserver.version.exception.InvalidVersionException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link HandlerInterceptor} that validates version prefixes on resolved handlers.
 *
 * <p>Defense-in-depth for version validation. The primary invalid-version detection
 * happens in the error controller for unresolved paths (which never reach this
 * interceptor). This interceptor catches edge cases where a handler resolves but
 * the requested version is not in the versioned path registry.</p>
 */
@RequiredArgsConstructor
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final @NotNull Pattern VERSION_PREFIX = Pattern.compile("^/v(\\d+)(/.*)$");

    private final @NotNull VersionRegistryService versionRegistryService;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        if (!(handler instanceof HandlerMethod)) return true;

        Matcher matcher = VERSION_PREFIX.matcher(request.getRequestURI());
        if (!matcher.matches()) return true;

        int requestedVersion = Integer.parseInt(matcher.group(1));
        String basePath = matcher.group(2);
        ConcurrentSet<Integer> available = versionRegistryService.getVersionsForPath(basePath);

        if (available != null && !available.contains(requestedVersion))
            throw new InvalidVersionException(requestedVersion, basePath, available);

        return true;
    }

}
