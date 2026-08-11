# PluginUtils

Shared utility library for MCME's Paper plugins. It is deployed on the server as its own
plugin, and other MCME plugins compile against it and declare it as a dependency (they do
**not** bundle it).

## What's in it

- **`plotStoring`** — the MCME schematic/plot format (`MCMEPlotFormat`, `IStoragePlot`,
  `StoragePlotSnapshot`): save and restore block regions (with tile entities, biomes, entities)
  as compact NBT. Used by Animations, Architect, etc.
- **`nms`** — thin NMS/CraftBukkit access helpers (`AccessNBT`, `AccessWorld`, `AccessServer`,
  `AccessInventory`, `NBTTagBuilder`, …).
- **`region`** — region math (`CuboidRegion`, `PrismoidRegion`), including WorldEdit interop.
- **`message`** — chat/`FancyMessage` helpers.
- **`confirmation`**, **`developer`** — confirmation prompts and small developer utilities.
- Top-level helpers: `LegacyMaterialUtil` (pre-1.13 id/data → `BlockData`), `WEUtil` (WorldEdit),
  `DynmapUtil` (Dynmap).

## Requirements

- **JDK 25** to build.
- Runtime: **Paper 26.1.2+**, with the PluginUtils plugin installed on the server.

## Building

The build uses [paperweight-userdev], which resolves the CraftBukkit/NMS classes from PaperMC's
published dev-bundle — there is **no hard-coded server jar and nothing to place by hand**, so it
builds on any machine and in CI.

```bash
./gradlew build
```

The first build downloads and decompiles the Paper server (a few minutes); it's cached afterwards.
The plugin jar is written to `build/libs/`.

[paperweight-userdev]: https://github.com/PaperMC/paperweight

## Using it in a plugin

PluginUtils is a **runtime plugin dependency** — compile against it (`compileOnly` / `provided`),
declare it in your `plugin.yml`, and let the server provide it. Do not shade it in.

Add it via [JitPack] (no credentials needed — the repo is public):

**Gradle**
```gradle
repositories {
    maven { url = 'https://jitpack.io' }
}
dependencies {
    compileOnly 'com.github.MCME:PluginUtils:VERSION'
}
```

**Maven**
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.MCME</groupId>
    <artifactId>PluginUtils</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```

**`plugin.yml`**
```yaml
depend: [PluginUtils]
```

Pick `VERSION`:
- a **release tag** (recommended for anything deployed), e.g. `2.0.0`;
- a **branch build** for testing the latest, e.g. `pluginutils-26.2-gradle-SNAPSHOT`;
- a **commit hash** to pin exactly.

The first time a given version is requested, JitPack builds it from source (the same
paperweight build as above), then caches it.

[JitPack]: https://jitpack.io/#MCME/PluginUtils

## Branches

- **`master`** — 1.9.x, Paper 1.21.x. Legacy Maven build (compiles against a local server jar; not
  portable — use JitPack releases instead of building it yourself).
- **`pluginutils-26.2` / `pluginutils-26.2-gradle`** — 2.0.x, Paper 26.x. Gradle + paperweight,
  portable and CI/JitPack-buildable.
