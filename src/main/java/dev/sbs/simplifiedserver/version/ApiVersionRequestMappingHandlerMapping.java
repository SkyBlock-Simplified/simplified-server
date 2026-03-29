package dev.sbs.simplifiedserver.version;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * Custom {@link RequestMappingHandlerMapping} that prepends version path prefixes
 * (e.g., {@code /v1}) to handler methods annotated with {@link ApiVersion}.
 *
 * <p>Resolves {@link ApiVersion} from the method first, then falls back to the
 * declaring class. The version prefix is combined with the existing request mapping
 * paths to produce versioned URLs (e.g., {@code /v1/hello}).</p>
 */
public class ApiVersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    @Override
    protected @Nullable RequestMappingInfo getMappingForMethod(@NotNull Method method, @NotNull Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
        if (info == null) return null;

        ApiVersion apiVersion = AnnotationUtils.findAnnotation(method, ApiVersion.class);
        if (apiVersion == null)
            apiVersion = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);

        if (apiVersion != null) {
            int[] versions = apiVersion.value();
            boolean hasV1 = false;
            for (int v : versions)
                if (v == 1) { hasV1 = true; break; }

            int prefixCount = versions.length + (hasV1 ? 1 : 0);
            String[] prefixes = new String[prefixCount];
            for (int i = 0; i < versions.length; i++)
                prefixes[i] = "/v" + versions[i];
            if (hasV1)
                prefixes[versions.length] = "";

            RequestMappingInfo versionInfo = RequestMappingInfo.paths(prefixes).build();
            return versionInfo.combine(info);
        }

        return info;
    }

    @Override
    protected @Nullable RequestCondition<?> getCustomMethodCondition(@NotNull Method method) {
        ApiVersion apiVersion = AnnotationUtils.findAnnotation(method, ApiVersion.class);
        return (apiVersion != null) ? new ApiVersionCondition(apiVersion.value()) : null;
    }

    @Override
    protected @Nullable RequestCondition<?> getCustomTypeCondition(@NotNull Class<?> handlerType) {
        ApiVersion apiVersion = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
        return (apiVersion != null) ? new ApiVersionCondition(apiVersion.value()) : null;
    }

}
