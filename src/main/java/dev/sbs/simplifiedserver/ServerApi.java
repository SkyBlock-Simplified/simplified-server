package dev.sbs.simplifiedserver;

import api.simplified.hypixel.HypixelContract;
import api.simplified.hypixel.exception.HypixelApiException;
import com.google.gson.Gson;
import dev.sbs.mojangapi.MojangContract;
import dev.sbs.mojangapi.exception.MojangApiException;
import dev.sbs.mojangapi.request.MojangDomain;
import dev.sbs.sbsapi.SbsContract;
import dev.sbs.sbsapi.exception.SbsApiException;
import dev.simplified.client.Client;
import dev.simplified.client.ClientConfig;
import dev.simplified.client.Proxy;
import dev.simplified.gson.GsonSettings;
import dev.simplified.manager.KeyManager;
import dev.simplified.manager.Manager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Server-local service locator that replaces the former {@code MinecraftApi} static holder.
 * <p>
 * Owns the {@link Gson} and {@link GsonSettings} used by the server for contract I/O, a
 * {@link KeyManager} that supplies the Hypixel API key header on demand, and the {@link Client}
 * / {@link Proxy} instances for the Hypixel, SBS, and Mojang contracts. The Mojang
 * {@link Proxy} can be rebuilt with IPv6 source-address rotation via
 * {@link #setInet6NetworkPrefix(String)} once the runtime prefix is known. Persistence access
 * flows through {@code dev.sbs.skyblockdata.SkyBlockData} directly - this locator does not
 * own it.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerApi {

    @Getter private static final @NotNull GsonSettings gsonSettings = GsonSettings.defaults();
    @Getter private static final @NotNull Gson gson = gsonSettings.create();

    @Getter private static final @NotNull KeyManager keyManager = new KeyManager(
        (entry, key) -> key.equalsIgnoreCase(entry.getKey()),
        Manager.Mode.UPDATE
    );

    @Getter private static final @NotNull Client<HypixelContract> hypixelClient = Client.create(
        ClientConfig.builder(HypixelContract.class, gson)
            .withErrorDecoder(HypixelApiException::new)
            .withDynamicHeader("API-Key", keyManager.getSupplier("HYPIXEL_API_KEY"))
            .build()
    );

    @Getter private static final @NotNull Client<SbsContract> sbsClient = Client.create(
        ClientConfig.builder(SbsContract.class, gson)
            .withErrorDecoder(SbsApiException::new)
            .build()
    );

    private static volatile @NotNull Proxy<MojangContract> mojangProxy = Proxy.builder(
            ClientConfig.builder(MojangContract.class, gson)
                .withErrorDecoder(MojangApiException::new)
                .build()
        )
        .withAvailability(client -> !client.isRateLimited(MojangDomain.MINECRAFT_SERVICES))
        .build();

    /**
     * Returns the shared Mojang proxy, rebuilt with IPv6 rotation if
     * {@link #setInet6NetworkPrefix(String)} has been called.
     *
     * @return the shared Mojang proxy instance
     */
    public static @NotNull Proxy<MojangContract> getMojangProxy() {
        return mojangProxy;
    }

    /**
     * Replaces the registered Mojang {@link Proxy} with one that rotates outbound source
     * addresses across the given IPv6 CIDR prefix.
     * <p>
     * Intended to be called once at application startup once the runtime IPv6 prefix is known.
     *
     * @param cidrPrefix an IPv6 network prefix in CIDR notation (e.g. {@code "2000:444:33ff::/48"})
     */
    public static void setInet6NetworkPrefix(@NotNull String cidrPrefix) {
        mojangProxy = Proxy.builder(
                ClientConfig.builder(MojangContract.class, gson)
                    .withErrorDecoder(MojangApiException::new)
                    .build()
            )
            .withInet6Rotation(cidrPrefix)
            .withAvailability(client -> !client.isRateLimited(MojangDomain.MINECRAFT_SERVICES))
            .build();
    }

}
