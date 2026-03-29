package dev.sbs.simplifiedserver.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Global exception handler producing consistent {@link SbsErrorResponse} JSON for all errors.
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
    public @NotNull ResponseEntity<SbsErrorResponse> handleServerException(
            @NotNull ServerException ex,
            @NotNull HttpServletRequest request) {
        HttpStatus status = ex.getStatus();
        return ResponseEntity.status(status).body(SbsErrorResponse.of(
            status.value(),
            status.getReasonPhrase(),
            ex.getMessage(),
            route(request)
        ));
    }

    @ExceptionHandler(ApiException.class)
    public @NotNull ResponseEntity<SbsErrorResponse> handleApiException(
            @NotNull ApiException ex,
            @NotNull HttpServletRequest request) {
        int code = ex.getStatus().getCode();
        return ResponseEntity.status(code).body(SbsErrorResponse.of(
            code,
            HttpStatus.valueOf(code).getReasonPhrase(),
            ex.getResponse().getReason(),
            route(request)
        ));
    }

    @ExceptionHandler(Exception.class)
    public @NotNull ResponseEntity<SbsErrorResponse> handleAll(
            @NotNull Exception ex,
            @NotNull HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SbsErrorResponse.of(
            500,
            "Internal Server Error",
            "An unexpected error occurred",
            route(request)
        ));
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
        return request.getMethod() + " " + request.getRequestURI();
    }

}
