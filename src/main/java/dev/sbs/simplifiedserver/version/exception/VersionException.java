package dev.sbs.simplifiedserver.version.exception;

import dev.sbs.simplifiedserver.exception.ServerException;
import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;

/**
 * Thrown when an API version constraint is violated.
 *
 * @see MissingVersionException
 * @see InvalidVersionException
 */
public class VersionException extends ServerException {

    /**
     * Constructs a new {@code VersionException} with the specified status and cause.
     *
     * @param status the HTTP status code for the error response
     * @param cause the underlying throwable that caused this exception
     */
    public VersionException(@NotNull HttpStatus status, @NotNull Throwable cause) {
        super(status, cause);
    }

    /**
     * Constructs a new {@code VersionException} with the specified status and detail message.
     *
     * @param status the HTTP status code for the error response
     * @param message the detail message
     */
    public VersionException(@NotNull HttpStatus status, @NotNull String message) {
        super(status, message);
    }

    /**
     * Constructs a new {@code VersionException} with the specified status, cause, and detail message.
     *
     * @param status the HTTP status code for the error response
     * @param cause the underlying throwable that caused this exception
     * @param message the detail message
     */
    public VersionException(@NotNull HttpStatus status, @NotNull Throwable cause, @NotNull String message) {
        super(status, cause, message);
    }

    /**
     * Constructs a new {@code VersionException} with the specified status and a formatted detail message.
     *
     * @param status the HTTP status code for the error response
     * @param message the format string
     * @param args the format arguments
     */
    public VersionException(@NotNull HttpStatus status, @NotNull @PrintFormat String message, @Nullable Object... args) {
        super(status, message, args);
    }

    /**
     * Constructs a new {@code VersionException} with the specified status, cause, and a formatted detail message.
     *
     * @param status the HTTP status code for the error response
     * @param cause the underlying throwable that caused this exception
     * @param message the format string
     * @param args the format arguments
     */
    public VersionException(@NotNull HttpStatus status, @NotNull Throwable cause, @NotNull @PrintFormat String message, @Nullable Object... args) {
        super(status, cause, message, args);
    }

}
