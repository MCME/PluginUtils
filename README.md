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
- Runtime: **Paper 26.2**, with the PluginUtils plugin installed on the server. The NMS helpers are
  tied to the Minecraft version they were compiled against; 2.0.2 and later target 26.2.

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

Releases are published to MCME's Maven repository as `com.mcmiddleearth:PluginUtils`; reading it
needs no credentials. The latest is **2.0.4**, and [CHANGELOG.md](CHANGELOG.md) lists what changed
in each release.

**Gradle**
```gradle
repositories {
    maven { url = 'https://repo.mcmiddleearth.com/releases' }
}
dependencies {
    compileOnly 'com.mcmiddleearth:PluginUtils:2.0.4'
    // Only if your tests load PluginUtils classes (e.g. with MockBukkit):
    testImplementation 'com.mcmiddleearth:PluginUtils:2.0.4'
}
```

**Maven**
```xml
<repositories>
    <repository>
        <id>mcme-releases</id>
        <url>https://repo.mcmiddleearth.com/releases</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.mcmiddleearth</groupId>
    <artifactId>PluginUtils</artifactId>
    <version>2.0.4</version>
    <scope>provided</scope>
</dependency>
```

**`plugin.yml`**
```yaml
depend: [PluginUtils]
```

Two things to know:

- **Build with JDK 25.** PluginUtils 2.0.x is compiled for Java 25. In Gradle that means a Java 25
  toolchain: a build that targets older bytecode cannot resolve it ("only compatible with JVM
  runtime version 25 or newer").
- **json-simple comes along.** PluginUtils' one dependency is json-simple 1.1.1, which the Paper
  server also ships. Since 2.0.4 it is declared in the published POM and Gradle module, so the
  declarations above put it on your compile and test classpaths but never in your jar: tests that
  load `MessageUtil` under MockBukkit work without adding json-simple yourself.

### JitPack

[JitPack] builds any tag, branch or commit on demand, as `com.github.MCME:PluginUtils:<version>`
from the `https://jitpack.io` repository. That is handy for trying an unreleased commit, or a branch
build such as `pluginutils-26.2-SNAPSHOT`; the first request for a version waits while JitPack
builds it. Prefer the release coordinates above for anything you deploy: JitPack takes the groupId
from the GitHub org, and build tools treat different groupIds as different libraries, so a build
that also pulls in `com.mcmiddleearth:PluginUtils` (directly or through another MCME library) ends
up with two copies on its classpath and no version resolution between them.

[JitPack]: https://jitpack.io/#MCME/PluginUtils

## Branches

- **`master`** — 1.9.x, Paper 1.21.x. Legacy Maven build that compiles against a local server jar,
  so it is not portable; releases 1.9.0 to 1.9.2 are on repo.mcmiddleearth.com.
- **`pluginutils-26.2`** — 2.0.x, Paper 26.x. Gradle + paperweight, built by CI on every push;
  pushing a release tag publishes that version to repo.mcmiddleearth.com.

The other branches are history: `development` fed 1.9.x into `master`, `1.13` dates from 2018, and
`publish-2.0.2` is where 2.0.2 was published from, because its tag predates the publishing setup.
