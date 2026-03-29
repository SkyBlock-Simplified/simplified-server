package dev.sbs.simplifiedserver.version;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the {@link ApiVersionRequestMappingHandlerMapping} as a high-priority
 * bean, the {@link VersionRegistryService} for precomputed version index lookups,
 * and the {@link ApiVersionInterceptor} for version validation on resolved handlers.
 */
@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    @Bean
    public @NotNull ApiVersionRequestMappingHandlerMapping apiVersionRequestMappingHandlerMapping() {
        ApiVersionRequestMappingHandlerMapping mapping = new ApiVersionRequestMappingHandlerMapping();
        mapping.setOrder(0);
        return mapping;
    }

    @Bean
    public @NotNull VersionRegistryService versionRegistryService(@NotNull ApiVersionRequestMappingHandlerMapping handlerMapping) {
        return new VersionRegistryService(handlerMapping);
    }

    @Bean
    public @NotNull ApiVersionInterceptor apiVersionInterceptor(@NotNull VersionRegistryService versionRegistryService) {
        return new ApiVersionInterceptor(versionRegistryService);
    }

    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
        registry.addInterceptor(apiVersionInterceptor(versionRegistryService(apiVersionRequestMappingHandlerMapping()))).addPathPatterns("/**");
    }

}
