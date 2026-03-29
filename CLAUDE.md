# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

See the root [`CLAUDE.md`](../CLAUDE.md) for multi-module build commands, environment variables, cross-cutting patterns, and dependency details.

## Build & Test

```bash
# From repo root
./gradlew :simplified-server:build          # Build (includes shadowJar)
./gradlew :simplified-server:test           # Run all tests

# Fat JAR (shadowJar merged into build task)
./gradlew :simplified-server:shadowJar      # Output: build/libs/simplified-server-0.1.0.jar
```

## Module Overview

`simplified-server` is a standalone Spring Boot REST server. It depends on `api` and `minecraft-api` via Maven coordinates (not project references).

### Entry Point

- **`SimplifiedServer`** - Spring Boot application. Excludes Jackson auto-configuration in favor of Gson via `SimplifiedApi.getGson()`. Runs on port 8080 (configurable in `application.properties`).

### Package Structure

**`controller/`** - Spring MVC REST controllers:
- `ErrorController` - Global `@ControllerAdvice` for exception handling. Returns 422 for missing path variables, delegates other errors to a default error view.
- `MojangController` - Mojang API proxy endpoints under `/mojang/`. Provides player profile lookup by username or UUID, username resolution, and skin properties. Uses `MinecraftApi.getMojangProxy()` for upstream calls.
