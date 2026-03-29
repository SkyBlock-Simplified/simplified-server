# Contributing to Simplified Server

Thank you for your interest in contributing! This document explains how to get
started, what to expect during the review process, and the conventions this
project follows.

## Table of Contents

- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Development Setup](#development-setup)
- [Making Changes](#making-changes)
  - [Branching Strategy](#branching-strategy)
  - [Code Style](#code-style)
  - [Commit Messages](#commit-messages)
  - [Testing](#testing)
- [Submitting a Pull Request](#submitting-a-pull-request)
- [Reporting Issues](#reporting-issues)
- [Project Architecture](#project-architecture)
- [Legal](#legal)

## Getting Started

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| [JDK](https://adoptium.net/) | **21+** | Required (virtual threads) |
| [Git](https://git-scm.com/) | 2.x+ | For cloning and contributing |
| [IntelliJ IDEA](https://www.jetbrains.com/idea/) | Latest | Recommended IDE (Lombok and Gradle support built-in) |

For running the server locally:

| Requirement | Notes |
|-------------|-------|
| Hypixel API key | Required for most Hypixel/SkyBlock endpoints |
| MariaDB or Docker | For database-backed features |
| Environment variables | `HYPIXEL_API_KEY`, `DATABASE_HOST`, `DATABASE_SCHEMA`, `DATABASE_PORT`, `DATABASE_USER`, `DATABASE_PASSWORD` |

### Development Setup

1. **Fork and clone the repository**

   [Fork the repository](https://github.com/SkyBlock-Simplified/simplified-server/fork),
   then clone your fork:

   ```bash
   git clone https://github.com/<your-username>/simplified-server.git
   cd simplified-server
   ```

2. **Clone dependency modules alongside** (for local development)

   This module depends on [server-api](https://github.com/SkyBlock-Simplified/server-api)
   (`dev.sbs:server-api:0.1.0`), [minecraft-api](https://github.com/SkyBlock-Simplified/minecraft-api)
   (`dev.sbs:minecraft-api:0.1.0`), and transitively on
   [api](https://github.com/SkyBlock-Simplified/api) (`dev.sbs:api:0.1.0`).
   Clone them alongside this repository for local development with Gradle
   composite builds.

   ```bash
   cd ..
   git clone https://github.com/SkyBlock-Simplified/api.git
   git clone https://github.com/SkyBlock-Simplified/minecraft-api.git
   git clone https://github.com/SkyBlock-Simplified/server-api.git
   ```

3. **Build the project**

   The Gradle wrapper is included - no separate Gradle installation is needed.

   ```bash
   cd simplified-server
   ./gradlew build
   ```

4. **Open in IntelliJ IDEA**

   Open the project root as a Gradle project. IntelliJ will automatically
   detect the `build.gradle.kts` and import dependencies. Ensure the Lombok
   plugin is installed and annotation processing is enabled
   (`Settings > Build > Compiler > Annotation Processors`).

5. **Verify the setup**

   ```bash
   ./gradlew test
   ```

## Making Changes

### Branching Strategy

- Create a feature branch from `master` for your work.
- Use a descriptive branch name: `fix/mojang-uuid-parsing`,
  `feat/skyblock-bingo-endpoint`, `docs/endpoint-examples`.

```bash
git checkout -b feat/my-feature master
```

### Code Style

#### General

- **Spring conventions** - Follow standard Spring Boot patterns for
  `@RestController`, `@RequestMapping`, `@Configuration`, and `@Bean`.
- **Collections** - Always use `Concurrent.newList()`, `Concurrent.newMap()`,
  `Concurrent.newSet()` instead of `new ArrayList`, `new HashMap`, etc.
- **Annotations** - Use `@NotNull` / `@Nullable` from `org.jetbrains.annotations`
  on all public method parameters and return types.
- **Lombok** - Use `@Getter`, `@RequiredArgsConstructor`, `@Log4j2`, etc.
  where appropriate. The logger field is non-static
  (`lombok.log.fieldIsStatic = false`).
- **OpenAPI annotations** - Annotate controllers with `@Tag` (class-level) and
  `@Operation` / `@Parameter` (method-level) from `io.swagger.v3.oas.annotations`.

#### Braces

- Omit curly braces when the `if` body is a single line.
- Use curly braces when the body wraps across multiple lines.

#### Javadoc

- **Class level** - Noun phrase describing what the type is.
- **Method level** - Active verb, third person singular, describing what the
  method does.
- **Tags** - Always include `@param`, `@return`, `@throws` on public methods.
  Tag descriptions are lowercase sentence fragments with no trailing period.
  Single space after the param name (no column alignment).
- **Punctuation** - Only use single hyphens (` - `) as separators. Never em
  dashes, `&mdash;`, or double hyphens.
- Never use `@author` or `@since`.

#### Controllers

- Each controller class maps to a single upstream API domain (Mojang, Hypixel,
  SkyBlock, Resources).
- Use `@ResponseStatus(HttpStatus.OK)` on all endpoint methods.
- Delegate to `minecraft-api` Feign clients via private helper methods
  (`endpoint()`, `proxy()`) rather than injecting Spring beans.
- Group related endpoints under a shared `@RequestMapping` base path.

### Commit Messages

Write clear, concise commit messages that describe *what* changed and *why*.

```
Add garden endpoint to SkyBlock controller

Proxies the Hypixel SkyBlock garden API for fetching garden data by
profile ID.
```

- Use the imperative mood ("Add", "Fix", "Update", not "Added", "Fixes").
- Keep the subject line under 72 characters.
- Add a body when the *why* isn't obvious from the subject.

### Testing

Tests use JUnit 5 (Jupiter):

```bash
./gradlew test
```

- Add tests for new functionality where practical.
- Many endpoints depend on live upstream APIs and database state, so not all
  controller methods can be unit tested in isolation. Integration tests that
  require a running server or external services should be clearly documented.

## Submitting a Pull Request

1. **Push your branch** to your fork.

   ```bash
   git push origin feat/my-feature
   ```

2. **Open a Pull Request** against the `master` branch of
   [SkyBlock-Simplified/simplified-server](https://github.com/SkyBlock-Simplified/simplified-server).

3. **In the PR description**, include:
   - A summary of the changes and the motivation behind them.
   - Steps to test or verify the changes.
   - Whether the change adds new API endpoints or modifies existing ones.

4. **Respond to review feedback.** PRs may go through one or more rounds of
   review before being merged.

### What gets reviewed

- Correctness of endpoint mappings and upstream client delegation.
- OpenAPI annotation completeness (`@Tag`, `@Operation`, `@Parameter`).
- Impact on the public API surface. New endpoints or breaking changes to
  existing endpoints should be discussed in the issue tracker before
  implementation.
- Adherence to the controller patterns established by existing controllers.
- Compatibility with the `server-api` framework (error handling, security
  headers, message converters).

## Reporting Issues

Use [GitHub Issues](https://github.com/SkyBlock-Simplified/simplified-server/issues)
to report bugs or request features.

When reporting a bug, include:

- **Java version** (`java --version`)
- **Spring Boot version** (check `gradle/libs.versions.toml`)
- **Operating system**
- **Full error stacktrace** (if applicable)
- **Request URL and response** (if applicable)
- **Steps to reproduce**
- **Expected vs. actual behavior**

## Project Architecture

A brief overview to help you find your way around the codebase:

```
src/main/java/dev/sbs/simplifiedserver/
├── SimplifiedServer.java              # Spring Boot entry point with Gson bean
├── config/
│   └── OpenApiConfig.java             # OpenAPI metadata (title, description, version)
└── controller/
    ├── MojangController.java          # /mojang/ - profile, username, UUID, properties, bulk
    ├── HypixelController.java         # /hypixel/ - player, guild, status, counts, games
    ├── SkyBlockController.java        # /skyblock/ - profiles, auctions, bazaar, museum, garden
    └── ResourceController.java        # /resources/ - skills, collections, items, election
```

### Key extension points

- **New controller** - Add a `@RestController` in `controller/` with `@Tag`
  and `@Operation` annotations. Delegate to upstream `minecraft-api` Feign
  clients.
- **Custom error body** - Override `ErrorController.buildErrorBody()` from
  `server-api` to return a project-specific JSON error response type.
- **Custom Gson** - The `Gson` `@Bean` in `SimplifiedServer` controls the
  serializer used for all JSON responses. Modify it to customize serialization.
- **Server tuning** - Replace `ServerConfig.optimized()` with
  `ServerConfig.builder()` for fine-grained control over Tomcat, compression,
  HTTP/2, and other server settings.

## Legal

By submitting a pull request, you agree that your contributions are licensed
under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0),
the same license that covers this project.
