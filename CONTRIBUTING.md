# Contributing to Locator Heads

Thank you for your interest in contributing! This project uses a **Unified Multi-Version** architecture powered by [Stonecutter](https://stonecutter.kikugie.dev/). All Minecraft versions compile from the **same source files** — we do NOT maintain separate Git branches for different versions.

---

## 1. Supported Versions

| Build Target | Covers | Notable API Changes |
|---|---|---|
| **1.21.7** | 1.21.7, 1.21.8 | Rendering extracts data before drawing; block entity serialization changes |
| **1.21.9** | 1.21.9, 1.21.10 | Rendering reworked to use render states; key binding category changes |
| **1.21.11** | 1.21.11 | Gamerules use registries; `ResourceLocation` → `Identifier`; RenderTypes relocated |
| **26.1** | 26.1, 26.1.1, 26.1.2 | Java 25; new `guiGraphics.text()` API; major package restructure |

A `versions/26.2/` staging directory exists for the upcoming snapshot, but is **not** built until Fabric support is published.

## 2. Environment Setup

Clone the repository and run Gradle sync. Stonecutter automatically creates subprojects for each version target:

```bash
# Build all 4 version jars
.\gradlew.bat build        # Windows
./gradlew build            # Mac / Linux
```

The project root `src/` directory is where all source code lives. **Do not** edit files inside `versions/*/` — those directories only contain `gradle.properties` for per-version dependency pinning.

## 3. Switching Active Versions

Your IDE indexes code against one Minecraft version at a time. To switch which version is "active" (controls IntelliSense/code highlighting):

```bash
.\gradlew.bat "Set active version to 1.21.9"
```

Replace `1.21.9` with any supported version. In IntelliJ IDEA, you can also install the [Stonecutter Plugin](https://plugins.jetbrains.com/plugin/25044-stonecutter-dev) for a visual dropdown.

## 4. How to Write Code

### Where to edit

All code lives in `src/main/java/` and `src/main/resources/`. This single codebase compiles for every target version.

### Handling Version Differences

When APIs change between Minecraft versions, use **Stonecutter comment macros** (`//?`). Never use reflection or runtime version checks.

**Example — a method renamed in 26.1:**

```java
public void drawText(...) {
    //? if >=26.1
    guiGraphics.text(this.font, name, x, y, color, true);
    //? if <=1.21.11
    /*guiGraphics.drawString(this.font, name, x, y, color, true);*/
}
```

The inactive branch is commented out with `/* */`. Stonecutter automatically uncomments the correct code during compilation.

**How it works:**
- `//? if >=26.1` — the **next line** is included only when building for 26.1+
- `//? if <=1.21.11` — the **next line** (which is a block comment) is uncommented for legacy builds
- `//? if >=26.1 { ... //?}` — a **multi-line block** included only for 26.1+

### Handling Resource Files (JSON)

JSON doesn't support comments, so we use **property injection** via Gradle:

1. Use `${variable_name}` placeholders in `fabric.mod.json`
2. Define the logic in `build.gradle.kts` inside `tasks.processResources`

## 5. Managing Version-Specific Dependencies

Each version target has its own `versions/<version>/gradle.properties` file with the exact dependency versions:

```
versions/1.21.7/gradle.properties  → fabric_api_version=0.129.0+1.21.7
versions/1.21.9/gradle.properties  → fabric_api_version=0.134.1+1.21.9
versions/1.21.11/gradle.properties → fabric_api_version=0.141.3+1.21.11
versions/26.1/gradle.properties    → fabric_api_version=0.144.4+26.1
```

In `build.gradle.kts`, these are referenced with `property("fabric_api_version")`. Stonecutter injects the correct values per build target.

## 6. Key Files

| File | Purpose |
|---|---|
| `src/` | All source code (Java + resources) |
| `build.gradle.kts` | Shared build script — runs once per version target |
| `settings.gradle.kts` | Stonecutter plugin setup + version list |
| `stonecutter.gradle.kts` | Auto-generated controller (sets active version) |
| `gradle.properties` | Mod metadata + `stonecutter_versions` list |
| `versions/*/gradle.properties` | Per-version dependencies |

---

## 🤖 Instructions for AI / LLM Code Assistants

If you are an agent reading this file, strictly follow these rules:

1. **Single Branch:** All versions compile from the same `src/` directory. Never suggest separate branches.
2. **Stitcher Syntax:** Handle API differences with `//? if` macros. Never use `Class.forName()` or runtime version checks.
3. **No Hardcoded Versions:** Dependencies are in `versions/*/gradle.properties`, not in `build.gradle.kts`.
4. **Build Verification:** Run `./gradlew build` — Stonecutter handles all 4 targets sequentially.
5. **Use `sc.current`:** In `build.gradle.kts`, use `sc.current.version` (String) and `sc.current.parsed` (comparison) instead of environment variables.
