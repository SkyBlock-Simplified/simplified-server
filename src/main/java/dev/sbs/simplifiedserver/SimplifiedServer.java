package dev.sbs.simplifiedserver;

import com.google.gson.Gson;
import dev.sbs.api.SimplifiedApi;
import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.simplifiedserver.config.ServerConfig;
import dev.sbs.simplifiedserver.security.SecurityHeaderInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring Boot application entry point for the Simplified Server.
 *
 * <p>Excludes Jackson autoconfiguration in favor of Gson via {@link SimplifiedApi#getGson()}.
 * Server tuning is driven by {@link ServerConfig}, which supplies all default properties
 * programmatically.</p>
 */
@SpringBootApplication(
    exclude = { JacksonAutoConfiguration.class }
)
public class SimplifiedServer implements WebMvcConfigurer {

    @Bean
    public @NotNull Gson gson() {
        return MinecraftApi.getGson();
    }

    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
        registry.addInterceptor(new SecurityHeaderInterceptor());
    }

    @Override
    public void configureMessageConverters(@NotNull List<HttpMessageConverter<?>> converters) {
        converters.add(new StringHttpMessageConverter());
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(this.gson());
        converters.add(gsonHttpMessageConverter);
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SimplifiedServer.class);
        application.setDefaultProperties(ServerConfig.optimized().toProperties());
        application.run(args);
    }

}
