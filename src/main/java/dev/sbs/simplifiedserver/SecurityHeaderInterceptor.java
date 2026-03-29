package dev.sbs.simplifiedserver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Adds security response headers to every request.
 *
 * <p>Sets {@code X-Content-Type-Options: nosniff} to prevent browsers from
 * MIME-sniffing the response away from the declared {@code Content-Type}.</p>
 */
public class SecurityHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        return true;
    }

}
