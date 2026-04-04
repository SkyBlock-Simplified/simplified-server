# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

See the root [`CLAUDE.md`](../CLAUDE.md) for cross-cutting patterns.
See [`server-api/CLAUDE.md`](../server-api/CLAUDE.md) for the reusable Spring server framework (API versioning, API key auth, error handling, server config).

## Build & Test

```bash
# From repo root
./gradlew :simplified-server:build          # Build (includes shadowJar)
./gradlew :simplified-server:test           # Run all tests

# Fat JAR (shadowJar merged into build task)
./gradlew :simplified-server:shadowJar      # Output: build/libs/simplified-server-0.1.0.jar
```

## Module Overview

`simplified-server` is the SkyBlock-specific Spring Boot REST server. It depends on `server-api` (framework) and `minecraft-api` via Maven coordinates. The `server-api` framework provides API versioning, API key authentication, error handling, and server configuration; this module provides the concrete controllers, OpenAPI metadata, and application entry point.

Follows the same split pattern as `discord-api` (framework) vs `simplified-bot` (implementation).

### Entry Point

- **`SimplifiedServer`** - Spring Boot application. Provides a `Gson` bean via `MinecraftApi.getGson()` that `ServerWebConfig` (from `server-api`) picks up automatically for the `GsonHttpMessageConverter`. Jackson auto-configuration remains enabled for SpringDoc's internal OpenAPI spec generation. Uses `ServerConfig.optimized()` to supply all default properties programmatically. Scans both `dev.sbs.simplifiedserver` and `dev.sbs.serverapi` packages via `@SpringBootApplication(scanBasePackages = ...)`.

### Package Structure

**`config/`** - Application-specific configuration:
- `OpenApiConfig` - `@Configuration` defining the `OpenAPI` metadata bean (title, description, version) used by SpringDoc for spec generation at `/v3/api-docs` and rendered by the Scalar UI at the root path.

**`controller/`** - Spring MVC REST controllers proxying upstream APIs:
- `MojangController` - Mojang API proxy endpoints under `/mojang/`. Provides player profile lookup (by username or UUID), username resolution, UUID resolution, skin/cape properties, and bulk username lookup. Delegates to `MojangProxy` for upstream calls with automatic IPv6 rotation.
- `HypixelController` - Hypixel API proxy endpoints under `/hypixel/`. Provides player data, guild lookups (by ID, name, or player), online status, player counts, punishment statistics, and game information. Delegates to `HypixelEndpoint`.
- `SkyBlockController` - SkyBlock API proxy endpoints under `/skyblock/`. Provides profiles, auctions (by ID, profile, or player), active/ended auction listings, bazaar products, museum, garden, news, and fire sales. Delegates to `HypixelEndpoint`.
- `ResourceController` - SkyBlock resource proxy endpoints under `/resources/`. Provides skill definitions, collection definitions, item definitions, and election data. None require an API key. Delegates to `HypixelEndpoint`.

### Dependencies

- **`server-api`** - Reusable Spring server framework (API versioning, auth, error handling, config). Transitively provides `api` and `spring-boot-starter-web`.
- **`minecraft-api`** - Minecraft/Hypixel API client, Mojang proxy.
- **`springdoc-openapi-starter-webmvc-scalar`** - OpenAPI spec generation and Scalar UI (implementation-specific).

### Configuration

All properties are managed programmatically through `ServerConfig` (from `server-api`):
- `ServerConfig.builder()` - Full control with Spring Boot defaults
- `ServerConfig.optimized()` - Production preset (virtual threads, compression, graceful shutdown, reverse proxy support)
- `api.key.authentication.enabled` - Toggles API key security (default `true` in `ServerConfig`)
- `springdocEnabled` - Toggles SpringDoc OpenAPI spec generation and Scalar UI (default `true` in `ServerConfig`)

### API Documentation (SpringDoc + Scalar)

- **Dependency:** `springdoc-openapi-starter-webmvc-scalar` (version in `gradle/libs.versions.toml`)
- **Root path:** `GET /` redirects to the Scalar UI (`springdoc.use-root-path=true`)
- **OpenAPI spec:** `GET /v3/api-docs` returns the auto-generated OpenAPI 3.0 JSON
- **Jackson coexistence:** Jackson auto-configuration is enabled (not excluded) so SpringDoc can use it internally. Gson remains the primary HTTP serializer because `GsonHttpMessageConverter` is registered first by `ServerWebConfig`
- **Controller annotations:** `@Tag` (class-level) and `@Operation` (method-level) from `io.swagger.v3.oas.annotations` enrich the generated documentation

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **simplified-server** (73 symbols, 187 relationships, 2 execution flows). Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.

> If any GitNexus tool warns the index is stale, run `npx gitnexus analyze` in terminal first.

## Always Do

- **MUST run impact analysis before editing any symbol.** Before modifying a function, class, or method, run `gitnexus_impact({target: "symbolName", direction: "upstream"})` and report the blast radius (direct callers, affected processes, risk level) to the user.
- **MUST run `gitnexus_detect_changes()` before committing** to verify your changes only affect expected symbols and execution flows.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `gitnexus_query({query: "concept"})` to find execution flows instead of grepping. It returns process-grouped results ranked by relevance.
- When you need full context on a specific symbol — callers, callees, which execution flows it participates in — use `gitnexus_context({name: "symbolName"})`.

## When Debugging

1. `gitnexus_query({query: "<error or symptom>"})` — find execution flows related to the issue
2. `gitnexus_context({name: "<suspect function>"})` — see all callers, callees, and process participation
3. `READ gitnexus://repo/simplified-server/process/{processName}` — trace the full execution flow step by step
4. For regressions: `gitnexus_detect_changes({scope: "compare", base_ref: "main"})` — see what your branch changed

## When Refactoring

- **Renaming**: MUST use `gitnexus_rename({symbol_name: "old", new_name: "new", dry_run: true})` first. Review the preview — graph edits are safe, text_search edits need manual review. Then run with `dry_run: false`.
- **Extracting/Splitting**: MUST run `gitnexus_context({name: "target"})` to see all incoming/outgoing refs, then `gitnexus_impact({target: "target", direction: "upstream"})` to find all external callers before moving code.
- After any refactor: run `gitnexus_detect_changes({scope: "all"})` to verify only expected files changed.

## Never Do

- NEVER edit a function, class, or method without first running `gitnexus_impact` on it.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `gitnexus_rename` which understands the call graph.
- NEVER commit changes without running `gitnexus_detect_changes()` to check affected scope.

## Tools Quick Reference

| Tool | When to use | Command |
|------|-------------|---------|
| `query` | Find code by concept | `gitnexus_query({query: "auth validation"})` |
| `context` | 360-degree view of one symbol | `gitnexus_context({name: "validateUser"})` |
| `impact` | Blast radius before editing | `gitnexus_impact({target: "X", direction: "upstream"})` |
| `detect_changes` | Pre-commit scope check | `gitnexus_detect_changes({scope: "staged"})` |
| `rename` | Safe multi-file rename | `gitnexus_rename({symbol_name: "old", new_name: "new", dry_run: true})` |
| `cypher` | Custom graph queries | `gitnexus_cypher({query: "MATCH ..."})` |

## Impact Risk Levels

| Depth | Meaning | Action |
|-------|---------|--------|
| d=1 | WILL BREAK — direct callers/importers | MUST update these |
| d=2 | LIKELY AFFECTED — indirect deps | Should test |
| d=3 | MAY NEED TESTING — transitive | Test if critical path |

## Resources

| Resource | Use for |
|----------|---------|
| `gitnexus://repo/simplified-server/context` | Codebase overview, check index freshness |
| `gitnexus://repo/simplified-server/clusters` | All functional areas |
| `gitnexus://repo/simplified-server/processes` | All execution flows |
| `gitnexus://repo/simplified-server/process/{name}` | Step-by-step execution trace |

## Self-Check Before Finishing

Before completing any code modification task, verify:
1. `gitnexus_impact` was run for all modified symbols
2. No HIGH/CRITICAL risk warnings were ignored
3. `gitnexus_detect_changes()` confirms changes match expected scope
4. All d=1 (WILL BREAK) dependents were updated

## Keeping the Index Fresh

After committing code changes, the GitNexus index becomes stale. Re-run analyze to update it:

```bash
npx gitnexus analyze
```

If the index previously included embeddings, preserve them by adding `--embeddings`:

```bash
npx gitnexus analyze --embeddings
```

To check whether embeddings exist, inspect `.gitnexus/meta.json` — the `stats.embeddings` field shows the count (0 means no embeddings). **Running analyze without `--embeddings` will delete any previously generated embeddings.**

> Claude Code users: A PostToolUse hook handles this automatically after `git commit` and `git merge`.

## CLI

| Task | Read this skill file                                |
|------|-----------------------------------------------------|
| Understand architecture / "How does X work?" | `~/.claude/skills/gitnexus/gitnexus-exploring/SKILL.md` |
| Blast radius / "What breaks if I change X?" | `~/.claude/skills/gitnexus/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?" | `~/.claude/skills/gitnexus/gitnexus-debugging/SKILL.md` |
| Rename / extract / split / refactor | `~/.claude/skills/gitnexus/gitnexus-refactoring/SKILL.md` |
| Tools, resources, schema reference | `~/.claude/skills/gitnexus/gitnexus-guide/SKILL.md` |
| Index, status, clean, wiki CLI commands | `~/.claude/skills/gitnexus/gitnexus-cli/SKILL.md` |

<!-- gitnexus:end -->
