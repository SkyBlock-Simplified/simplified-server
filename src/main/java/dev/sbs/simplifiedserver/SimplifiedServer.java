package dev.sbs.simplifiedserver;

import com.google.gson.Gson;
import dev.sbs.api.SimplifiedApi;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@SpringBootApplication(
    exclude = { JacksonAutoConfiguration.class }
)
@EnableWebMvc
public class SimplifiedServer implements WebMvcConfigurer {

    @Bean
    public @NotNull Gson gson() {
        return SimplifiedApi.getGson();
    }

    @Override
    public void configureMessageConverters(@NotNull List<HttpMessageConverter<?>> converters) {
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(this.gson());
        converters.add(gsonHttpMessageConverter);
    }

    public static void main(String[] args) {
        SpringApplication.run(SimplifiedServer.class, args);
    }

}
