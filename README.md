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
- **`message`** — chat/`FancyMessage` helpers, sent as Adventure components so clicks and tooltips
  work on 26.2; paged lists end with a `[‹ Prev] Page 2/3 [Next ›]` line.
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

paperweight-userdev is pinned to a released version (`2.0.0-beta.23` in `build.gradle`), not its
moving `2.0.0-SNAPSHOT`. On 2026-09-28 a new snapshot began to require Gradle 9.7.1, and this
project's wrapper is Gradle 9.0.0, so every clean build (CI, JitPack) failed while machines with a
cached snapshot kept working. Releases from `2.0.0-beta.24` on need Gradle 9.7.1 as well, so upgrade
the plugin and the Gradle wrapper together.

[paperweight-userdev]: https://github.com/PaperMC/paperweight

## Using it in a plugin

PluginUtils is a **runtime plugin dependency** — compile against it (`compileOnly` / `provided`),
declare it in your `plugin.yml`, and let the server provide it. Do not shade it in.

Releases are published to MCME's Maven repository as `com.mcmiddleearth:PluginUtils`; reading it
needs no credentials. The latest is **2.0.5**, and [CHANGELOG.md](CHANGELOG.md) lists what changed
in each release.

**Gradle**
```gradle
repositories {
    maven { url = 'https://repo.mcmiddleearth.com/releases' }
}
dependencies {
    compileOnly 'com.mcmiddleearth:PluginUtils:2.0.5'
    // Only if your tests load PluginUtils classes (e.g. with MockBukkit):
    testImplementation 'com.mcmiddleearth:PluginUtils:2.0.5'
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
    <version>2.0.5</version>
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
build such as `development-SNAPSHOT`; the first request for a version waits while JitPack
builds it. Prefer the release coordinates above for anything you deploy: JitPack takes the groupId
from the GitHub org, and build tools treat different groupIds as different libraries, so a build
that also pulls in `com.mcmiddleearth:PluginUtils` (directly or through another MCME library) ends
up with two copies on its classpath and no version resolution between them.

[JitPack]: https://jitpack.io/#MCME/PluginUtils

## Branches

- **`master`** — releases: 2.0.x, Paper 26.x. It only moves when a version is released, through a
  pull request from `development`; the release commit is tagged (`2.0.5`), and pushing the tag
  publishes that version to repo.mcmiddleearth.com.
- **`development`** — where the work happens, through pull requests. CI builds every pull request
  and every push.

The other branches are history: `pluginutils-26.2` is where 2.0.x was ported, up to 2.0.5;
`publish-2.0.2` is where 2.0.2 was published from, because its tag predates the publishing setup;
and `1.13` dates from 2018. The 1.9.x line (Paper 1.21.x, a legacy Maven build that compiles
against a local server jar, so it is not portable) ends at commit `f17ced6`; its releases 1.9.0
to 1.9.2 are on repo.mcmiddleearth.com.

## Releasing

1. On a branch from `development`, set `version` in `build.gradle` to the release (say `2.0.6`),
   move the notes under the CHANGELOG's `[Unreleased]` into a `## [2.0.6] - <date>` section with its
   compare link, and update the version in this README (the "latest" line and the Gradle and Maven
   snippets). Commit it as `release: PluginUtils 2.0.6 — <what changed>` and open a pull request
   into `development`.
2. When CI is green and the pull request is merged, tag the release commit (annotated and bare:
   `2.0.6`) and push the tag. CI then publishes that version to repo.mcmiddleearth.com. The
   `releases` repository refuses to overwrite a version, so each version publishes only once: check
   the published POM, module and jar afterwards.
3. Open a pull request from `development` into `master`.
4. On `development`, start the next version: `version = '2.0.7-SNAPSHOT'`, committed as
   `chore: begin 2.0.7-SNAPSHOT`.
