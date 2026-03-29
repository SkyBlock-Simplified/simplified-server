package dev.sbs.simplifiedserver.error;

import dev.sbs.api.client.exception.ApiException;
import dev.sbs.api.collection.concurrent.ConcurrentSet;
import dev.sbs.minecraftapi.client.sbs.exception.SbsErrorResponse;
import dev.sbs.simplifiedserver.exception.ServerException;
import dev.sbs.simplifiedserver.version.VersionRegistryService;
import dev.sbs.simplifiedserver.version.exception.InvalidVersionException;
import dev.sbs.simplifiedserver.version.exception.MissingVersionException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Global exception handler producing consistent error responses for all errors.
 *
 * <p>Performs content negotiation via the {@code Accept} header: browsers receiving
 * {@code text/html} get a Cloudflare-style HTML error page rendered by
 * {@link ErrorPageRenderer}, while API clients get {@link SbsErrorResponse} JSON.</p>
 */
@RequiredArgsConstructor
@RestControllerAdvice
public class ErrorController extends ResponseEntityExceptionHandler {

    private static final @NotNull Pattern VERSION_PREFIX = Pattern.compile("^/v(\\d+)(/.*)$");

    private final @NotNull VersionRegistryService versionRegistryService;

    @Override
    protected @NotNull ResponseEntity<Object> handleExceptionInternal(
            @NotNull Exception ex,
            Object body,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode statusCode,
            @NotNull WebRequest request) {
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        int code = statusCode.value();
        String reason = ex.getMessage() != null ? ex.getMessage() : HttpStatus.valueOf(code).getReasonPhrase();

        if (acceptsHtml(servletRequest)) {
            HttpHeaders htmlHeaders = new HttpHeaders(headers);
            htmlHeaders.setContentType(MediaType.TEXT_HTML);
            String html = ErrorPageRenderer.render(
                code,
                HttpStatus.valueOf(code).getReasonPhrase(),
                reason,
                route(servletRequest),
                servletRequest.getRemoteAddr(),
                servletRequest.getHeader("Cf-Ray")
            );
            return new ResponseEntity<>(html, htmlHeaders, statusCode);
        }

        SbsErrorResponse error = buildError(statusCode, ex.getMessage(), request);
        return new ResponseEntity<>(error, headers, statusCode);
    }

    @Override
    protected @NotNull ResponseEntity<Object> handleNoResourceFoundException(
            @NotNull NoResourceFoundException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {
        String requestUri = ((ServletWebRequest) request).getRequest().getRequestURI();

        Matcher versionMatcher = VERSION_PREFIX.matcher(requestUri);
        if (versionMatcher.matches()) {
            int requestedVersion = Integer.parseInt(versionMatcher.group(1));
            String basePath = versionMatcher.group(2);
            ConcurrentSet<Integer> available = versionRegistryService.getVersionsForPath(basePath);
            if (available != null)
                throw new InvalidVersionException(requestedVersion, basePath, available);
        }

        ConcurrentSet<Integer> available = versionRegistryService.getVersionsForPath(requestUri);
        if (available != null && !available.isEmpty())
            throw new MissingVersionException(requestUri, available);

        return handleExceptionInternal(ex, null, headers, status, request);
    }

    @ExceptionHandler(ServerException.class)
    public @NotNull ResponseEntity<?> handleServerException(
            @NotNull ServerException ex,
            @NotNull HttpServletRequest request) {
        HttpStatus status = ex.getStatus();

        if (acceptsHtml(request))
            return htmlResponse(status.value(), status.getReasonPhrase(), ex.getMessage(), request);

        return ResponseEntity.status(status).body(SbsErrorResponse.of(
            status.value(),
            status.getReasonPhrase(),
            ex.getMessage(),
            route(request)
        ));
    }

    @ExceptionHandler(ApiException.class)
    public @NotNull ResponseEntity<?> handleApiException(
            @NotNull ApiException ex,
            @NotNull HttpServletRequest request) {
        int code = ex.getStatus().getCode();
        String reason = ex.getResponse().getReason();

        if (acceptsHtml(request))
            return htmlResponse(code, HttpStatus.valueOf(code).getReasonPhrase(), reason, request, ErrorPageRenderer.ErrorSource.API);

        return ResponseEntity.status(code).body(SbsErrorResponse.of(
            code,
            HttpStatus.valueOf(code).getReasonPhrase(),
            reason,
            route(request)
        ));
    }

    @ExceptionHandler(Exception.class)
    public @NotNull ResponseEntity<?> handleAll(
            @NotNull Exception ex,
            @NotNull HttpServletRequest request) {
        if (acceptsHtml(request))
            return htmlResponse(500, "Internal Server Error", "An unexpected error occurred", request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SbsErrorResponse.of(
            500,
            "Internal Server Error",
            "An unexpected error occurred",
            route(request)
        ));
    }

    private static boolean acceptsHtml(@NotNull HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.contains("text/html");
    }

    private static @NotNull ResponseEntity<String> htmlResponse(int code, @NotNull String title, @NotNull String reason, @NotNull HttpServletRequest request) {
        return ResponseEntity.status(code)
            .contentType(MediaType.TEXT_HTML)
            .body(ErrorPageRenderer.render(code, title, reason, route(request), request.getRemoteAddr(), request.getHeader("Cf-Ray")));
    }

    private static @NotNull ResponseEntity<String> htmlResponse(int code, @NotNull String title, @NotNull String reason,
                                                                 @NotNull HttpServletRequest request, @NotNull ErrorPageRenderer.ErrorSource source) {
        return ResponseEntity.status(code)
            .contentType(MediaType.TEXT_HTML)
            .body(ErrorPageRenderer.render(code, title, reason, route(request), request.getRemoteAddr(), request.getHeader("Cf-Ray"), source));
    }

    private @NotNull SbsErrorResponse buildError(@NotNull HttpStatusCode statusCode, String reason, @NotNull WebRequest request) {
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        return SbsErrorResponse.of(
            statusCode.value(),
            HttpStatus.valueOf(statusCode.value()).getReasonPhrase(),
            reason != null ? reason : HttpStatus.valueOf(statusCode.value()).getReasonPhrase(),
            route(servletRequest)
        );
    }

    private static @NotNull String route(@NotNull HttpServletRequest request) {
        return request.getMethod() + " " + HtmlUtils.htmlEscape(request.getRequestURI());
    }

}
