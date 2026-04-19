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

## Java Toolchains

- **26.1+** requires Java 25
- **1.21.x** requires Java 21
- This is handled via `//? if` macros in `build.gradle.kts` for both `toolchain.languageVersion` and `options.release`

## Known Gotchas

- **Foojay Plugin Error (`IBM_SEMERU`):** If Gradle errors downloading Java 25, the Foojay plugin may not support it on your system. Temporarily use Java 21 if needed.
- **RemapJar:** Use `tasks.findByName("remapJar")?.let { ... }` in `afterEvaluate` since Stonecutter config passes may not always expose it.

## Version Coverage

| Target | Hotfixes Covered | Key Changes |
|---|---|---|
| 1.21.7 | 1.21.8 | Rendering data extraction; block entity serialization |
| 1.21.9 | 1.21.10 | Render states; key binding categories |
| 1.21.11 | — | Registry gamerules; ResourceLocation→Identifier |
| 26.1 | 26.1.1, 26.1.2 | Java 25; `text()` API; package restructure |

A `versions/26.2/` staging directory exists for the upcoming snapshot but is **not** in the active build list.

## Build & Test

```bash
./gradlew build    # Compiles all 4 version jars
```
