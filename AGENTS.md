# Agent Directives: Locator Heads

Welcome to the Locator Heads repository! This document outlines constraints and patterns for AI code assistants working on this project.

## Architecture

- **Single Branch, All Versions:** The codebase compiles and supports 4 Minecraft targets concurrently: 1.21.7, 1.21.9, 1.21.11, and 26.1. Do **not** create separate branches for different versions.
- **Source Location:** All code lives in `src/`. The `versions/` directories contain only `gradle.properties` for per-version dependency pinning.
- **Stonecutter 0.9.1** manages multi-version compilation. API differences are handled with `//? if` comment macros at compile-time.

## Code Rules

- **Stitcher Syntax Required:** Handle all API/Minecraft shifts using `//? if >=26.1` and `//? if <=1.21.11` comment macros. Do NOT use reflection or `Class.forName()` to check versions at runtime.
- **No Hardcoded Dependencies:** Never hardcode `fabric-api` or `fabric-loader` versions in `build.gradle.kts`. They come from `versions/*/gradle.properties` via Stonecutter property injection.
- **Use `sc.current` API:** In `build.gradle.kts`, use `sc.current.version` for the current target version string and `sc.current.parsed` for version comparisons. Do not read `STONECUTTER_ACTIVE` from the environment.
- **Minimize Code Divergence:** Prefer code that works across all versions. Only introduce `//? if` macros where a compile error forces it. Do NOT copy old code patterns just because they shipped — verify what actually works.
- **Consolidate Repeated Macros:** If the same `//? if` condition would appear at multiple call sites for the same API change, write a `@Unique` helper method that bridges the difference once, then call the helper everywhere.

## Three API Boundaries

All macro conditions in this codebase fall into exactly three boundaries. **Do not guess** — verify against the actual mappings (see Research section below).

### Boundary 1: `>=1.21.9` vs `<=1.21.7`

- **Cause**: `com.mojang.authlib` upgraded from 6.x (regular class) to 7.x (Java Record). Also, `method_70870` gained two new parameters.
- `GameProfile.getName()` → `GameProfile.name()`
- `GameProfile.getId()` → `GameProfile.id()`
- `getSkin().texture()` → `getSkin().body().texturePath()`
- `PartialTickSupplier` does NOT exist in 1.21.7
- `method_70870` signature: 4 params in 1.21.7, 6 params in 1.21.9+

### Boundary 2: `>=1.21.11` vs `<=1.21.10`

- **Cause**: Mojang renamed `ResourceLocation` → `Identifier`.
- Affects import statements, field types, and mixin target descriptors.

### Boundary 3: `>=26.1` vs `<=1.21.11`

- **Cause**: Rendering pipeline restructure; class names are no longer obfuscated.
- `GuiGraphics` → `GuiGraphicsExtractor`
- `drawString()` → `text()`
- `render()` → `extractRenderState()`
- Mixin method targets: `"method_70870"` → `"*"` (wildcard)

## Java Toolchains

- **26.1+** requires Java 25
- **1.21.x** requires Java 21
- This is handled via `//? if` macros in `build.gradle.kts` for both `toolchain.languageVersion` and `options.release`

## Known Gotchas

- **Foojay Plugin Error (`IBM_SEMERU`):** If Gradle errors downloading Java 25, the Foojay plugin may not support it on your system. Temporarily use Java 21 if needed.
- **RemapJar:** Use `tasks.findByName("remapJar")?.let { ... }` in `afterEvaluate` since Stonecutter config passes may not always expose it.

## Version Coverage

| Target | Hotfixes Covered | authlib | Key Changes |
|---|---|---|---|
| 1.21.7 | 1.21.8 | 6.0.58 (class) | `getName()`/`getId()`; `getSkin().texture()`; 4-param `method_70870` |
| 1.21.9 | 1.21.10 | 7.0.61 (Record) | `name()`/`id()`; `getSkin().body().texturePath()`; `PartialTickSupplier`; 6-param `method_70870` |
| 1.21.11 | — | 7.0.61 (Record) | `ResourceLocation`→`Identifier` |
| 26.1 | 26.1.1, 26.1.2 | 7.0.63 (Record) | Java 25; `GuiGraphicsExtractor`; `text()` API; non-obfuscated names |

A `versions/26.2/` staging directory exists for the upcoming snapshot but is **not** in the active build list.

## Research: How to Verify API Changes

**Before guessing macro boundaries, verify against actual data.** The two most reliable methods are:

### 1. Git History (Primary Source)

This repo has working, shipped code for every API era in its commit history:

```bash
# See all tagged releases
git tag

# View the mixin at a specific version
git show <commit>:src/main/java/haage/mixin/LocatorBarRendererMixin.java

# Search for specific API usage patterns across versions
git show <commit>:src/main/java/haage/mixin/LocatorBarRendererMixin.java | Select-String "getSkin|getName|texture"
```

Key commits:
- `970d54b` (1.0.0) — MC 1.21.7 (authlib 6.x)
- `49b2cb0` (1.21.9 update) — MC 1.21.9 (authlib 7.x)
- `7e3b7a8` (2.2.0) — MC 26.1 (GuiGraphicsExtractor)

### 2. Mappings Inspection

Check the Loom mappings cache to see actual method signatures and class names:

```bash
# Find the mappings file for a version
Get-ChildItem "$env:USERPROFILE\.gradle\caches\fabric-loom\<version>" -Recurse -Filter "mappings.tiny"

# Search for a class or method
Get-Content <mappings.tiny path> | Select-String "PlayerSkin"
Get-Content <mappings.tiny path> | Select-String "method_70870"
```

You can also check dependency JARs directly:

```bash
# Inspect authlib's GameProfile API
javap -classpath <authlib.jar path> com.mojang.authlib.GameProfile
```

### 3. Minecraft-Dev MCP Server (if available)

If a `minecraft-dev` MCP server is configured, use these tools to look up mappings and decompiled source:

- `get_minecraft_source` — Decompile a specific class for a given MC version
- `find_mapping` — Translate symbol names between mapping systems (official, intermediary, yarn, mojmap)
- `search_minecraft_code` — Search for classes, methods, or fields in decompiled source
- `compare_versions` — Diff two MC versions to find class/registry changes

Example: to verify what methods `PlayerSkin` has in 1.21.7 vs 1.21.11:
```
get_minecraft_source(version="1.21.7", className="net.minecraft.client.resources.PlayerSkin", mapping="mojmap")
```

## Reference Documentation

- [Stonecutter Docs](https://stonecutter.kikugie.dev/) — Comment macro syntax, version comparison semantics
- [Stonecutter Stitcher Syntax](https://stonecutter.kikugie.dev/stonecutter/guide/comments) — `//? if`, closed scope `{ //?}`, `elif`, `else`
- [SpongePowered Mixin Wiki](https://github.com/SpongePowered/Mixin/wiki) — `@Inject`, `@Redirect`, `@At` targets, method descriptors
- [Fabric Wiki](https://fabricmc.net/wiki/start) — Fabric Loader, Loom, mod structure
- [Mojang Mappings](https://minecraft.wiki/w/Obfuscation_map) — This project uses official Mojang mappings (not Yarn)

## Build & Test

```bash
./gradlew build    # Compiles all 4 version jars
```
