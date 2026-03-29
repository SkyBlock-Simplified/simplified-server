package dev.sbs.simplifiedserver.version;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that specifies the API version for a controller or handler method.
 *
 * <p>Maps to URL path segments (e.g., {@code @ApiVersion(1)} maps to {@code /v1/}).
 * When applied to a class, all handler methods in that class are prefixed with the
 * version path. Method-level annotations override class-level annotations.</p>
 *
 * <p>Multiple versions can be specified to allow a single handler to serve several
 * version prefixes (e.g., {@code @ApiVersion({1, 2})}).</p>
 */
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {

    /**
     * The API version numbers this handler supports.
     *
     * @return an array of integer version numbers
     */
    int[] value();

}
