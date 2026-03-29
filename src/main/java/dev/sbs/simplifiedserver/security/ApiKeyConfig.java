package dev.sbs.simplifiedserver.security;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Conditional configuration that registers API key security beans and the
 * authentication interceptor on {@code /**} paths.
 *
 * <p>Only active when {@code api.key.authentication.enabled} is {@code true}.
 * Centralizes all security-related bean definitions so they are created or
 * omitted as a unit.</p>
 */
@Configuration
@ConditionalOnProperty(name = "api.key.authentication.enabled", havingValue = "true")
public class ApiKeyConfig implements WebMvcConfigurer {

    @Bean
    public @NotNull ApiKeyRoleHierarchy roleHierarchyService() {
        return new ApiKeyRoleHierarchy();
    }

    @Bean
    public @NotNull ApiKeyService apiKeyService(@NotNull ApiKeyRoleHierarchy roleHierarchyService) {
        return new ApiKeyService(roleHierarchyService);
    }

    @Bean
    public @NotNull ApiKeyAuthenticationInterceptor apiKeyAuthenticationInterceptor(@NotNull ApiKeyService apiKeyService) {
        return new ApiKeyAuthenticationInterceptor(apiKeyService);
    }

    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
        registry.addInterceptor(apiKeyAuthenticationInterceptor(apiKeyService(roleHierarchyService()))).addPathPatterns("/**");
    }

}
