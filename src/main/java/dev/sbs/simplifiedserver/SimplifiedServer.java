package dev.sbs.simplifiedserver;

import com.google.gson.Gson;
import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.serverapi.config.ServerConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot application entry point for the Simplified Server.
 *
 * <p>Provides a {@link Gson} bean via {@link MinecraftApi#getGson()} that the framework's
 * message converter configuration picks up automatically. Jackson auto-configuration
 * remains enabled so that SpringDoc can use it internally for OpenAPI spec generation.
 * Server tuning is driven by {@link ServerConfig}, which supplies all default properties
 * programmatically.</p>
 */
@SpringBootApplication(scanBasePackages = { "dev.sbs.simplifiedserver", "dev.sbs.serverapi" })
public class SimplifiedServer {

    @Bean
    public @NotNull Gson gson() {
        return MinecraftApi.getGson();
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SimplifiedServer.class);
        application.setDefaultProperties(ServerConfig.optimized().toProperties());
        application.run(args);
    }

}
