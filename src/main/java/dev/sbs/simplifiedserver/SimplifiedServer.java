package dev.sbs.simplifiedserver;

import com.google.gson.Gson;
import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.serverapi.config.ServerConfig;
import dev.simplified.util.SystemUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot application entry point for the Simplified Server.
 *
 * <p>Provides a {@link Gson} bean via {@link MinecraftApi#getGson()} that the framework's
 * message converter configuration picks up automatically.
 *
 * <p>Server tuning is driven by {@link ServerConfig}, which supplies all default properties
 * programmatically.</p>
 */
@SpringBootApplication(scanBasePackages = { "dev.sbs.simplifiedserver", "dev.sbs.serverapi" })
public class SimplifiedServer {

    @Bean
    public @NotNull Gson gson() {
        return MinecraftApi.getGson();
    }

    public static void main(String[] args) {
        MinecraftApi.getKeyManager().add(SystemUtil.getEnvPair("HYPIXEL_API_KEY"));
        SystemUtil.getEnv("INET6_NETWORK_PREFIX").ifPresent(MinecraftApi.getMojangProxy()::setInet6NetworkPrefix);
        SpringApplication application = new SpringApplication(SimplifiedServer.class);
        application.setDefaultProperties(
            ServerConfig.optimized()
                .build()
                .toProperties()
        );
        application.run(args);
    }

}
