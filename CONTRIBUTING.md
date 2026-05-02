# Contributing to Locator Heads

Thank you for your interest in contributing! This project uses a **Unified Multi-Version** architecture powered by [Stonecutter](https://stonecutter.kikugie.dev/). All Minecraft versions compile from the **same source files** — we do NOT maintain separate Git branches for different versions.

> **For AI / LLM agents:** See [`AGENTS.md`](AGENTS.md) for machine-actionable directives, precise API boundaries, and research methodology.

---

## 1. Quick Start

```bash
git clone https://github.com/Haage001/locator-heads.git
cd locator-heads

# Build all 4 version jars
.\gradlew.bat build        # Windows
./gradlew build            # Mac / Linux
```

The build produces one jar per Minecraft version targeted in `versions/*/build/libs/`.

## 2. Supported Versions

| Build Target | Covers Minecraft Versions |
|---|---|
| **1.21.7** | 1.21.7, 1.21.8 |
| **1.21.9** | 1.21.9, 1.21.10 |
| **1.21.11** | 1.21.11 |
| **26.1** | 26.1, 26.1.1, 26.1.2 |

For detailed API changes at each version boundary, see the [Version Coverage table in AGENTS.md](AGENTS.md#version-coverage).

## 3. Project Structure

| Path | Purpose |
|---|---|
| `src/` | All source code (Java + resources) — the **only** place to edit code |
| `build.gradle.kts` | Shared build script — runs once per version target |
| `settings.gradle.kts` | Stonecutter plugin setup + version list |
| `stonecutter.gradle.kts` | Auto-generated controller (sets active version) |
| `gradle.properties` | Mod metadata + `stonecutter_versions` list |
| `versions/*/gradle.properties` | Per-version dependency pinning (do NOT edit code here) |

## 4. Switching Active Versions

Your IDE indexes code against one Minecraft version at a time. To switch which version is "active" (controls IntelliSense/code highlighting):

```bash
.\gradlew.bat "Set active version to 1.21.9"
```

Replace `1.21.9` with any supported version. In IntelliJ IDEA, you can also install the [Stonecutter Plugin](https://plugins.jetbrains.com/plugin/25044-stonecutter-dev) for a visual dropdown.

## 5. How to Write Version-Specific Code

When APIs change between Minecraft versions, use **Stonecutter comment macros** (`//?`). Never use reflection or runtime version checks.

### Example — a method renamed in 26.1:

```java
public void drawText(...) {
    //? if >=26.1
    guiGraphics.text(this.font, name, x, y, color, true);
    //? if <=1.21.11
    /*guiGraphics.drawString(this.font, name, x, y, color, true);*/
}
```

The inactive branch is commented out with `/* */`. Stonecutter automatically uncomments the correct code during compilation.

### Macro syntax cheat sheet

| Syntax | Scope |
|---|---|
| `//? if >=26.1` | Next **single line** only |
| `//? if >=26.1 { ... //?}` | **Multi-line block** |
| `//? if >=26.1 { ... //?} else { ... //?}` | **If/else block** |

For the full Stonecutter syntax reference, see the [Stonecutter documentation](https://stonecutter.kikugie.dev/stonecutter/guide/comments).

### What causes macros? (Important when adding a new MC version)

Every macro in this codebase exists because Mojang changed an API between versions. The current boundaries are documented in detail in the [Three API Boundaries section of AGENTS.md](AGENTS.md#three-api-boundaries), but **new boundaries will appear** when future Minecraft versions ship breaking changes.

When adding support for a new version (e.g., 26.2), watch for these common sources of breakage:

1. **Class/method renames** — Mojang periodically renames classes (e.g., `ResourceLocation` → `Identifier` in 1.21.11) or methods (e.g., `drawString()` → `text()` in 26.1). These require import and call-site macros.
2. **Third-party library upgrades** — `com.mojang.authlib` is bundled with Minecraft but versioned independently. When it upgraded from 6.x to 7.x, `GameProfile` changed from a class to a Java Record, breaking `getName()` → `name()`. Check `javap` on the authlib JAR.
3. **Method signature changes** — Mojang sometimes adds or removes parameters from internal methods that mixins target. Use `mappings.tiny` to verify parameter counts.
4. **Rendering pipeline restructure** — 26.1 replaced `GuiGraphics` with `GuiGraphicsExtractor` and changed mixin targets from obfuscated names to wildcards.

**Don't guess** — verify against actual data. The [Research section of AGENTS.md](AGENTS.md#research-how-to-verify-api-changes) explains how to use git history, mappings files, and `javap` to confirm what changed.

### Reducing Macro Repetition

If you find yourself repeating the same `//? if` macro at every call site for the same API change, **write a helper method instead**. Put the conditional logic in one place, then call your helper everywhere:

```java
// One-time bridge — the only place the macro lives
@Unique
private String locatorHeads$getProfileName(GameProfile profile) {
    //? if >=1.21.9
    return profile.name();
    //? if <=1.21.7
    /*return profile.getName();*/
}

// Used everywhere else without macros
String name = locatorHeads$getProfileName(playerInfo.getProfile());
```

This pattern is already used in `LocatorHeadsModMenuIntegration.java`, where `translatable()` and `literal()` wrap `Component.translatable()` / `Component.literal()` — if Mojang ever renames `Component`, only the two helper methods need updating.

## 6. Managing Dependencies

Each version target has its own `versions/<version>/gradle.properties` with pinned dependency versions. **Never** hardcode versions in `build.gradle.kts`.

```
versions/1.21.7/gradle.properties  → fabric_api_version=0.129.0+1.21.7
versions/26.1/gradle.properties    → fabric_api_version=0.144.4+26.1
```

In `build.gradle.kts`, these are referenced with `property("fabric_api_version")`. Stonecutter injects the correct values per build target.

## 7. Handling Resource Files (JSON)

JSON doesn't support comments, so we use **property injection** via Gradle:

1. Use `${variable_name}` placeholders in `fabric.mod.json`
2. Define the logic in `build.gradle.kts` inside `tasks.processResources`

## 8. Learning Resources

If you're new to Fabric modding or the tools used in this project:

- [Fabric Wiki — Getting Started](https://fabricmc.net/wiki/start) — Setting up a Fabric mod development environment
- [Fabric Wiki — Mixins](https://fabricmc.net/wiki/tutorial:mixin_introduction) — Introduction to Mixin injection
- [SpongePowered Mixin Wiki](https://github.com/SpongePowered/Mixin/wiki) — Detailed Mixin documentation
- [Stonecutter Documentation](https://stonecutter.kikugie.dev/) — Multi-version compilation with comment macros
- [Fabric Loom](https://fabricmc.net/wiki/documentation:fabric_loom) — The Gradle plugin that handles mappings, remapping, and mod packaging
- [Mojang Mappings (Mojmap)](https://minecraft.wiki/w/Obfuscation_map) — This project uses official Mojang mappings, not Yarn
