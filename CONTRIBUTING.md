# Contributing to Locator Heads

Thank you for your interest in contributing! This project uses a **Unified Multi-Version** architecture powered by [Stonecutter](https://stonecutter.kikugie.dev/). This means we do not maintain separate Git branches for different Minecraft versions (e.g., `1.21.10`, `1.21.11`, `26.1`). Instead, all contributions must happen on the main branch within a unified codebase.

This guide outlines exactly how to work with the codebase across any IDE (VS Code, Eclipse, IntelliJ) and provides explicit instructions for LLMs and AI Code Assistants.

---

## 1. Environment Setup (Any IDE)

When you first clone the repository, **do not attempt to build the root package directly**.

You must trigger the Stonecutter multi-version generator to initialize the workspace:

```bash
# Windows
.\gradlew.bat chiseledSetup

# Mac / Linux
./gradlew chiseledSetup
```

This extracts the codebase into targeted Minecraft version profiles.

## 2. Switching Active Versions

If you want your IDE indexer (IntelliSense/LSP) to resolve the Java code against a specific version (for example, mapping 1.21.10 Minecraft APIs instead of 26.1 APIs), use the Gradle `chiseledSetActive` task:

```bash
.\gradlew.bat chiseledSetActive -Pstonecutter.active="1.21.10"
```

_Note: If you use IntelliJ IDEA, you can optionally install the Stonecutter Plugin to get a visual dropdown menu, but the CLI command above universally works in VS Code, Cursor, and vim!_

## 3. How to Write Code ( The `chiseled` Directory )

All of your actual code changes must happen inside the `chiseled/src/` directory. **Do not** modify code inside any of the generated version directories (e.g., `versions/1.21.10/src`); those are ephemeral output folders and your changes will be overwritten!

### Handling Version Differences

Because this single codebase compiles for multiple Minecraft versions, APIs inevitably shift. We handle these changes using **Stitcher Block Comments** (`//?`).

**Never use Java reflection or pseudo-version variables to handle cross-version rendering.** Instead, write the explicit code for each version alongside each other, separating them with `//?` conditions. The compiler will strip out the irrelevant code during the build process.

**Example of an API change:**

```java
public void drawText(...) {
    //? if >=26.1
    guiGraphics.text(this.font, name, x, y, color, true);
    //? if <=1.21.11
    /*guiGraphics.drawString(this.font, name, x, y, color, true);*/
}
```

_Notice how the code for the inactive versions is commented out using standard block `/_ ... _/` syntax. Stonecutter will automatically uncomment the correct block during compilation based on the target version._

```kotlin
// In build.gradle.kts:
dependencies {
    //? if >=26.1
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
    //? if <=1.21.11
    /*id("net.fabricmc.fabric-loom-remap") version "1.15-SNAPSHOT"*/
}
```

## 4. Managing Version-Specific Dependencies
Because the actual version string of the Fabric API artifact changes for each of these Minecraft updates, you must manage these changing versions using Gradle properties.

In `chiseled/build.gradle.kts`, you define the dependency dynamically by referencing a project property:
```kotlin
implementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_api_version")}")
```

Inside each of Stonecutter's version-specific directories (e.g., `versions/1.21.11/` and `versions/26.1/`), you maintain a separate `gradle.properties` file that targets that compilation step.
Inside that file, define the exact `fabric_api_version` string required for that particular Minecraft version (for example, `0.139.4+1.21.11` for the 1.21.11 environment). Stonecutter automatically injects that property context into the subproject build!

---

## 🤖 Instructions for AI / LLM Code Assistants

If you are an agent reading this file to assist the user, you must strictly adhere to the following rules when altering this repository:

1. **Rule of One Branch:** Do not suggest checking out legacy tags or branches to backport logic. `1.21.10` and `26.1` compile from the exact same file in `chiseled/`.
2. **Rule of Stitcher Syntax:** When translating logic from legacy mappings (Yarn) to Mojmap, or handling rendering shifts (like `GuiGraphics`), you must encapsulate the branching logic in standard `//? if` Stonecutter preprocessor syntax.
3. **No Dynamic Classloading:** Avoid injecting `Class.forName()` or `FabricLoader.getInstance().isModLoaded()` specifically to check the Minecraft version state. Handle Minecraft version fragmentation explicitly at compile-time using Stonecutter.
4. **Execution Bound**: Run tests using `./gradlew build`—Stonecutter handles executing Loom sequentially across the bounds silently.
