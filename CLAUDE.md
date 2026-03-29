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

`simplified-server` is a standalone Spring Boot REST server. It depends on `api` and `minecraft-api` via Maven coordinates (not project references). No Spring Security dependency - authentication uses a custom interceptor.

### Entry Point

- **`SimplifiedServer`** - Spring Boot application. Excludes Jackson auto-configuration in favor of Gson via `SimplifiedApi.getGson()`. Configures `StringHttpMessageConverter` (for HTML error pages) and `GsonHttpMessageConverter` as message converters. Registers `SecurityHeaderInterceptor` for security response headers. Implements `WebMvcConfigurer` directly (no `@EnableWebMvc`, no `WebMvcConfigurationSupport`). Uses `ServerConfig.optimized()` to supply all default properties programmatically.

### Package Structure

**`config/`** - Server-wide configuration:
- `ServerConfig` - Immutable configuration class following the `ClassBuilder` pattern (like `JpaConfig`). Inner `Builder` with `@BuildFlag` validation, `Reflection.validateFlags()` in `build()`. Contains inner enums (`ShutdownMode`, `ForwardHeadersStrategy`, `LogLevel`) and a `MemorySize` value class for typed size properties. Static factories: `builder()` for full control, `optimized()` for a production-tuned preset. `toProperties()` converts fields to a `ConcurrentMap<String, Object>` for `SpringApplication.setDefaultProperties()`.

**`controller/`** - Spring MVC REST controllers:
- `MojangController` - Mojang API proxy endpoints under `/mojang/`. Single `getUser(String identifier)` endpoint handles both username and UUID by attempting `UUID.fromString()` first. Uses `MinecraftApi.getMojangProxy()` for upstream calls.

**`error/`** - Global error handling and HTML error page rendering:
- `ErrorController` - Global `@RestControllerAdvice` extending `ResponseEntityExceptionHandler`. Performs content negotiation via the `Accept` header: browsers (`text/html`) receive Cloudflare-style HTML error pages rendered by `ErrorPageRenderer`, while API clients receive `SbsErrorResponse` JSON. Overrides `handleExceptionInternal` to catch all Spring framework exceptions (404, 405, 400, etc.). Overrides `handleNoResourceFoundException` to detect version violations on 404s before falling through to a generic 404 - checks for invalid version prefixes (`InvalidVersionException`) and missing version prefixes on versioned paths (`MissingVersionException`). Single `@ExceptionHandler(ServerException.class)` handles all `SecurityException` and `VersionException` subtypes by reading `ex.getStatus()` for the HTTP status code and reason phrase. Additional handlers for `ApiException` (upstream status, marked as `ErrorSource.API`) and a catch-all `Exception` handler (500). Injected with `VersionRegistryService` via `@RequiredArgsConstructor`. No controller should catch exceptions locally; they propagate to this advice.
- `ErrorPageRenderer` - Non-instantiable utility class rendering Cloudflare-style HTML error pages. Loads CSS and HTML template from `src/main/resources/error/` once at class load time. Contains a `Placeholder` enum for named `{{TOKEN}}` substitution with XSS escaping via `HtmlUtils.htmlEscape()` on user-controlled values, and an `ErrorSource` enum (`CLIENT`, `SERVER`, `API`) controlling which status column shows the error indicator. Three status columns: Client / Server / API. Auto-detects source from status code (4xx = CLIENT, 5xx = SERVER) or accepts an explicit `ErrorSource` parameter for upstream API errors.

**`exception/`** - Server exception hierarchy:
- `ServerException` - Non-final root exception extending `RuntimeException` with an embedded `@Getter HttpStatus` field. Five constructors with `HttpStatus` as first parameter followed by cause/message variants. Root reversal in constructors 3 and 5. Enables `ErrorController` to handle all server exceptions with a single `@ExceptionHandler` that pulls status code and reason phrase from the exception itself.

**`src/main/resources/error/`** - HTML error page resources:
- `error-page.css` - Minified CSS from [donlon/cloudflare-error-page](https://github.com/donlon/cloudflare-error-page) (MIT license). Contains layout classes, responsive breakpoints, and SVG icons (browser, cloud, server, ok/error indicators) as inline data URIs.
- `error-page.html` - HTML template with named `{{PLACEHOLDER}}` tokens replaced by `ErrorPageRenderer`. Branding: "SBS" / "Server" center column, "You" / "Client" left, "Upstream" / "API" right. Footer shows Ray ID, IP reveal button, route, and "Powered by SkyBlock Simplified" link.

**`security/`** - API key authentication, authorization, and rate limiting:
- `SecurityHeaderInterceptor` - `HandlerInterceptor` that sets `X-Content-Type-Options: nosniff` on every response in `preHandle`. Prevents browsers from MIME-sniffing responses away from the declared `Content-Type`, mitigating reflected XSS via JSON endpoints.
- `ApiKey` - Represents an API key with permissions (role names and/or static permission strings), rate limit config (`maxRequests`/`windowInSeconds`), and sliding window counter state.
- `ApiKeyProtected` - TYPE and METHOD level annotation marking endpoints that require a valid API key. Optional `requiredPermissions` array specifies roles or static permissions (any-match semantics). Method-level takes precedence over class-level.
- `ApiKeyAuthenticationInterceptor` - `HandlerInterceptor` that extracts the API key from the `X-API-Key` header, validates the key, enforces rate limits, and checks permissions. Throws `MissingApiKeyException`, `InvalidApiKeyException`, `RateLimitExceededException`, or `InsufficientPermissionException` on failure - all handled by `ErrorController`. Resolves `@ApiKeyProtected` from method first, then class.
- `ApiKeyService` - Manages API key storage (`ConcurrentMap`), validation, rate limit checks, and permission resolution via `ApiKeyRoleHierarchy`. Currently uses hardcoded test keys.
- `ApiKeyRoleHierarchy` - Defines a linear role hierarchy (`DEVELOPER > SUPER_ADMIN > ADMIN > MODERATOR > HELPER > USER > LIMITED_ACCESS`). Higher roles inherit all lower role permissions. `getReachablePermissions()` expands assigned roles into the full reachable set.
- `ApiKeyConfig` - `@ConditionalOnProperty(name = "api.key.authentication.enabled", havingValue = "true")`. Defines all security beans (`ApiKeyRoleHierarchy`, `ApiKeyService`, `ApiKeyAuthenticationInterceptor`) and registers the interceptor on `/api/**` paths. Implements `WebMvcConfigurer`.
- `TestApiKeyController` - Example endpoints under `/api/` demonstrating `@ApiKeyProtected` with role and static permission requirements.
- `security/exception/` - `SecurityException` base class (non-final, extends `ServerException`, threads `HttpStatus` via direct pass) and four final leaf exceptions with internalized messages and no-arg constructors: `MissingApiKeyException` (`UNAUTHORIZED`, "Missing X-API-Key header"), `InvalidApiKeyException` (`UNAUTHORIZED`, "Invalid API key"), `RateLimitExceededException` (`TOO_MANY_REQUESTS`, "Rate limit exceeded"), `InsufficientPermissionException` (`FORBIDDEN`, "Insufficient permissions"). Thrown by the interceptor, caught by `ErrorController`'s single `ServerException` handler.

**`version/`** - URL-path-based API versioning (`/v1/endpoint`, `/v2/endpoint`):
- `ApiVersion` - TYPE and METHOD level annotation specifying supported version numbers (e.g., `@ApiVersion(1)`, `@ApiVersion({1, 2})`). Maps to URL path prefixes. No `@RequestMapping` meta-annotation.
- `ApiVersionCondition` - Custom `RequestCondition` for combine/compare purposes. Matching is handled entirely by path prefixes.
- `ApiVersionHandlerMapping` - Custom `RequestMappingHandlerMapping` that prepends `/v{N}` path prefixes to annotated handlers. When `@ApiVersion` includes version 1, also registers the handler at the base path (no prefix) as a v1 fallback. Resolves `@ApiVersion` from method first, then class. Supports `getCustomTypeCondition` for class-level annotations.
- `VersionRegistryService` - Precomputed index mapping base paths to their available version numbers. Built at startup via `@PostConstruct` from registered handler mappings. Used by `ApiVersionInterceptor` and `ErrorController` to detect invalid or missing version prefixes.
- `ApiVersionInterceptor` - `HandlerInterceptor` registered on `/**` for defense-in-depth version validation on resolved handlers. Throws `InvalidVersionException` when a version prefix is present but the version does not exist for the path.
- `ApiVersionConfig` - `@Configuration` implementing `WebMvcConfigurer`. Registers `ApiVersionHandlerMapping` as a `@Bean` with `setOrder(0)`, `VersionRegistryService`, and `ApiVersionInterceptor` on `/**`.
- `version/exception/` - `VersionException` base class (non-final, extends `ServerException`, threads `HttpStatus` via direct pass) and two final leaf exceptions with internalized format strings: `MissingVersionException` (`BAD_REQUEST`, constructor takes `endpoint` + `availableVersions`) and `InvalidVersionException` (`NOT_FOUND`, constructor takes `requestedVersion` + `basePath` + `availableVersions`).
- `TestVersionController` - Example versioned endpoints (`/v1/hello`, `/v2/hello`, `/default`) demonstrating URL path versioning.

### Configuration

All properties are managed programmatically through `ServerConfig`:
- `ServerConfig.builder()` - Full control with Spring Boot defaults
- `ServerConfig.optimized()` - Production preset (virtual threads, compression, graceful shutdown, reverse proxy support)
- `api.key.authentication.enabled` - Toggles API key security (default `true` in `ServerConfig`)
