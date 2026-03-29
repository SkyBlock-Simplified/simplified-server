# Simplified Server

SkyBlock-specific Spring Boot REST server for the
[SkyBlock Simplified](https://github.com/SkyBlock-Simplified) ecosystem,
providing proxy endpoints for the Mojang API, Hypixel API, SkyBlock API, and
SkyBlock resource definitions. Built on top of the
[server-api](https://github.com/SkyBlock-Simplified/server-api) framework.

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [API Endpoints](#api-endpoints)
  - [Mojang](#mojang)
  - [Hypixel](#hypixel)
  - [SkyBlock](#skyblock)
  - [Resources](#resources)
- [Architecture](#architecture)
  - [Entry Point](#entry-point)
  - [Controllers](#controllers)
  - [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Dependencies](#dependencies)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Mojang API proxy** - Player profile lookups, username resolution, skin
  properties, and bulk username lookups with automatic IPv6 rotation to avoid
  rate limits
- **Hypixel API proxy** - Player data, guild lookups, online status, punishment
  statistics, player counts, and game information
- **SkyBlock API proxy** - Profiles, auctions, bazaar, museum, garden, news,
  fire sales, and ended auctions
- **SkyBlock resource proxy** - Skills, collections, items, and election data
  (no API key required)
- **OpenAPI documentation** - Auto-generated API spec at `/v3/api-docs` with
  Scalar UI at the root path
- **Production-ready** - Virtual threads, response compression, graceful
  shutdown, and security headers via `ServerConfig.optimized()`

## Getting Started

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| [JDK](https://adoptium.net/) | **21+** | Required (virtual threads) |
| [Gradle](https://gradle.org/) | **9.4+** | Included via wrapper (`./gradlew`) |

**Required environment variables** (for upstream API calls):

```
HYPIXEL_API_KEY         # Hypixel API key (required for most Hypixel/SkyBlock endpoints)
DATABASE_HOST           # MariaDB host
DATABASE_SCHEMA         # MariaDB schema
DATABASE_PORT           # MariaDB port
DATABASE_USER           # MariaDB user
DATABASE_PASSWORD       # MariaDB password
```

### Installation

This module depends on the [server-api](https://github.com/SkyBlock-Simplified/server-api)
and [minecraft-api](https://github.com/SkyBlock-Simplified/minecraft-api) modules,
declared as Maven coordinates. For local development, clone all repositories
side by side and use Gradle composite builds:

```bash
git clone https://github.com/SkyBlock-Simplified/api.git
git clone https://github.com/SkyBlock-Simplified/minecraft-api.git
git clone https://github.com/SkyBlock-Simplified/server-api.git
git clone https://github.com/SkyBlock-Simplified/simplified-server.git
cd simplified-server
```

Build the server:

```bash
./gradlew build
```

Run tests:

```bash
./gradlew test
```

Run the server:

```bash
java -jar build/libs/simplified-server-0.1.0.jar
```

<details>
<summary>Using as a dependency in another Gradle project</summary>

**JitPack** (for snapshot builds):

```kotlin
repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.github.SkyBlock-Simplified:simplified-server:master-SNAPSHOT")
}
```

**Composite build** (for local development):

```kotlin
// settings.gradle.kts
includeBuild("../simplified-server")

// build.gradle.kts
dependencies {
    implementation("dev.sbs:simplified-server:0.1.0")
}
```

</details>

## API Endpoints

### Mojang

Proxy endpoints under `/mojang/` with automatic IPv6 rotation to avoid rate
limits.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/mojang/user/{identifier}` | Lookup player profile by username or UUID |
| `GET` | `/mojang/username/{username}` | Resolve case-corrected username and UUID |
| `GET` | `/mojang/uuid/{uniqueId}` | Resolve username by UUID |
| `GET` | `/mojang/properties/{uniqueId}` | Fetch signed skin/cape texture data |
| `POST` | `/mojang/bulk` | Bulk username lookup (up to 10 usernames) |

### Hypixel

Proxy endpoints under `/hypixel/` for Hypixel network data.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/hypixel/player/{playerId}` | Fetch player data and game stats |
| `GET` | `/hypixel/guild/id/{guildId}` | Fetch guild by ID |
| `GET` | `/hypixel/guild/name/{guildName}` | Fetch guild by name |
| `GET` | `/hypixel/guild/player/{playerId}` | Fetch guild by player |
| `GET` | `/hypixel/status/{playerId}` | Fetch player online status |
| `GET` | `/hypixel/counts` | Fetch current player counts |
| `GET` | `/hypixel/punishmentstats` | Fetch punishment statistics |
| `GET` | `/hypixel/games` | Fetch game information (no API key required) |

### SkyBlock

Proxy endpoints under `/skyblock/` for SkyBlock-specific data.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/skyblock/profiles/{playerId}` | Fetch all SkyBlock profiles for a player |
| `GET` | `/skyblock/museum/{islandId}` | Fetch museum data for a profile |
| `GET` | `/skyblock/garden/{islandId}` | Fetch garden data for a profile |
| `GET` | `/skyblock/news` | Fetch SkyBlock news and announcements |
| `GET` | `/skyblock/bazaar` | Fetch all Bazaar products (no API key required) |
| `GET` | `/skyblock/auction/id/{auctionId}` | Fetch auction by ID |
| `GET` | `/skyblock/auction/profile/{islandId}` | Fetch auctions by profile |
| `GET` | `/skyblock/auction/player/{playerId}` | Fetch auctions by player |
| `GET` | `/skyblock/auctions` | Fetch active auctions, first page (no API key required) |
| `GET` | `/skyblock/auctions/{page}` | Fetch active auctions by page (no API key required) |
| `GET` | `/skyblock/auctions/ended` | Fetch recently ended auctions (no API key required) |
| `GET` | `/skyblock/firesales` | Fetch active/upcoming Fire Sales (no API key required) |

### Resources

Proxy endpoints under `/resources/` for SkyBlock resource definitions. None of
these endpoints require an API key.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/resources/skills` | Fetch skill definitions and leveling data |
| `GET` | `/resources/collections` | Fetch collection definitions and tier data |
| `GET` | `/resources/items` | Fetch item definitions |
| `GET` | `/resources/election` | Fetch current mayor and election data |

## Architecture

### Entry Point

`SimplifiedServer` is the Spring Boot application class. It provides a `Gson`
bean via `MinecraftApi.getGson()` that the framework's message converter
configuration picks up automatically. Jackson auto-configuration remains enabled
so SpringDoc can use it internally for OpenAPI spec generation. Server tuning is
driven by `ServerConfig.optimized()`, which supplies all default properties
programmatically.

```java
@SpringBootApplication(scanBasePackages = { "dev.sbs.simplifiedserver", "dev.sbs.serverapi" })
public class SimplifiedServer {

    @Bean
    public Gson gson() {
        return MinecraftApi.getGson();
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SimplifiedServer.class);
        application.setDefaultProperties(ServerConfig.optimized().toProperties());
        application.run(args);
    }
}
```

> [!IMPORTANT]
> The `scanBasePackages` array must include `dev.sbs.serverapi` for Spring to
> discover the framework's configuration beans, interceptors, and error handler.

### Controllers

All controllers delegate to upstream Feign clients from `minecraft-api`:

| Controller | Base Path | Upstream Client |
|------------|-----------|-----------------|
| `MojangController` | `/mojang/` | `MojangProxy` (IPv6 rotation pool) |
| `HypixelController` | `/hypixel/` | `HypixelClient` via `HypixelEndpoint` |
| `SkyBlockController` | `/skyblock/` | `HypixelClient` via `HypixelEndpoint` |
| `ResourceController` | `/resources/` | `HypixelClient` via `HypixelEndpoint` |

### API Documentation

- **Root path:** `GET /` redirects to the Scalar UI (`springdoc.use-root-path=true`)
- **OpenAPI spec:** `GET /v3/api-docs` returns the auto-generated OpenAPI 3.0 JSON
- **Controller annotations:** `@Tag` (class-level) and `@Operation` (method-level)
  from `io.swagger.v3.oas.annotations` enrich the generated documentation
- **Jackson coexistence:** Jackson auto-configuration is enabled (not excluded)
  so SpringDoc can use it internally. Gson remains the primary HTTP serializer
  because `GsonHttpMessageConverter` is registered first by `ServerWebConfig`

## Project Structure

```
simplified-server/
├── src/main/java/dev/sbs/simplifiedserver/
│   ├── SimplifiedServer.java          # Spring Boot entry point with Gson bean
│   ├── config/
│   │   └── OpenApiConfig.java         # OpenAPI metadata (title, description, version)
│   └── controller/
│       ├── MojangController.java      # Mojang API proxy (/mojang/)
│       ├── HypixelController.java     # Hypixel API proxy (/hypixel/)
│       ├── SkyBlockController.java    # SkyBlock API proxy (/skyblock/)
│       └── ResourceController.java    # SkyBlock resources (/resources/)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml          # Version catalog
```

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Server API | 0.1.0 | Spring server framework (versioning, auth, error handling, config) |
| Minecraft API | 0.1.0 | Mojang/Hypixel Feign clients and SkyBlock models |
| SpringDoc OpenAPI Scalar | 2.8.16 | OpenAPI spec generation and Scalar UI |
| Lombok | 1.18.36 | Boilerplate reduction |
| simplified-annotations | 1.0.4 | Custom annotation processing |
| JUnit 5 | 5.11.4 | Testing |
| Hamcrest | 2.2 | Test matchers |

> [!NOTE]
> `server-api` transitively provides `api:0.1.0` and `spring-boot-starter-web` -
> no need to declare them separately.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, code style
guidelines, and how to submit a pull request.

## License

This project is licensed under the **Apache License 2.0** - see
[LICENSE.md](LICENSE.md) for the full text.
