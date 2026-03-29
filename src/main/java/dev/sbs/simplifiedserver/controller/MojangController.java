package dev.sbs.simplifiedserver.controller;

import dev.sbs.minecraftapi.MinecraftApi;
import dev.sbs.minecraftapi.client.mojang.exception.MojangApiException;
import dev.sbs.minecraftapi.client.mojang.response.MojangProfile;
import dev.sbs.minecraftapi.client.mojang.response.MojangProperties;
import dev.sbs.minecraftapi.client.mojang.response.MojangUsername;
import dev.sbs.minecraftapi.client.sbs.exception.SbsErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.UUID;

@RestController
@RequestMapping("/mojang")
public class MojangController {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/user/{username}")
    public @NotNull MojangProfile getUser(@PathVariable String username) {
        // Check null parameter
        MissingPathVariableException mpvex;

        NoHandlerFoundException nhfex;
        NoResourceFoundException nrferx;
        ResponseEntity<?> resp;

        return MinecraftApi.getMojangProxy().getMojangProfile(username);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/user/{uniqueId}")
    public @NotNull MojangProfile getUser(@PathVariable UUID uniqueId) {
        return MinecraftApi.getMojangProxy().getMojangProfile(uniqueId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/username/{username}")
    public @NotNull MojangUsername getUsername(@PathVariable String username) {
        return MinecraftApi.getMojangProxy().getEndpoint().getPlayer(username);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/properties/{uniqueId}")
    public @NotNull MojangProperties getUsername(@PathVariable UUID uniqueId) {
        return MinecraftApi.getMojangProxy().getEndpoint().getProperties(uniqueId);
    }

    @ExceptionHandler(
        value = MojangApiException.class,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @NotNull ResponseEntity<SbsErrorResponse> handleMissingPathVariable(@NotNull MojangApiException mojangApiException) {
        return ResponseEntity.status(mojangApiException.getStatus().getCode())
            .contentType(MediaType.APPLICATION_JSON)
            .body(mojangApiException.getResponse());
    }

}
