# Agent Directives: Locator Heads

Welcome to the Locator Heads repository! This document outlines the constraints, patterns, and workflows needed when utilizing an AI code assistant to contribute to this project.

## Source of Truth Architecture

- **Single Universal Branch:** The central branch (`feature/version-releases` running into `main`) compiles and supports ALL active Minecraft targets concurrently (1.21.10 - 26.1). Do **not** create separate repository branches for different Minecraft versions.
- **Root Directory Logic:** The codebase utilizes **Stonecutter 0.9**. The `chiseled/` directory contains all source files and the central `build.gradle.kts`. When `./gradlew build` is run, Stonecutter replicates this logic out to the different variants.
- **Conditional Syntax (Stitcher):** Handle all API/Minecraft shifts within the same file using Stonecutter comment macros (e.g., `//? if >=26.1` and `//? if <=1.21.11`). Do NOT use reflection or separate dynamic classloading for standard Minecraft mappings.

## Modifying Dependencies

- **Strict Interpolation:** NEVER hardcode `fabric-api` or `fabric-loader` string versions in `chiseled/build.gradle.kts`.
- **Property Overrides:** Locate the specific target in the `versions/` directory (e.g. `versions/1.21.11/gradle.properties`). You must specify the `fabric_api_version` and `fabric_loader_version` required for that target directly inside those property variants. Stonecutter dynamically interpolates these variables.

## Known Compilation Gotchas

- **Java Toolchains:** Because experimental snapshots (26.1+) demand Java 25 while legacy demands Java 21, `chiseled/build.gradle.kts` utilizes `toolchain.languageVersion.set(JavaLanguageVersion.of(25))`.
- **Foojay Plugin Error (`IBM_SEMERU`):** If Gradle auto-provisioning errors out downloading Java 25 stating `Class org.gradle.jvm.toolchain.JvmVendorSpec does not have member field '... IBM_SEMERU'`, the current version of the Foojay plugin fails against Gradle 9 constraints. In these scenarios, you may temporarily downgrade the code requirement to Java 21 just to parse the builds if native host versions do not align.
- **RemapJar Resolving:** When using `.kts` referencing `tasks.remapJar`, use a robust lookup like `tasks.findByName("remapJar")?.let { ... }` in `afterEvaluate` since Stonecutter config passes may not universally expose it.
