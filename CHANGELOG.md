# Changelog

Notable changes to PluginUtils, starting with the 2.0.x line (Paper 26.x, Gradle +
paperweight-userdev). The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Releases are tagged bare (`2.0.4`, not `v2.0.4`), and pushing a tag publishes that version to
repo.mcmiddleearth.com.

## [2.0.4] - 2026-09-23

No Java source changes since 2.0.3.

### Fixed

- The published POM and Gradle module now declare json-simple 1.1.1, with the junit 4 that
  json-simple's own POM drags in excluded. It was `compileOnly`, which is never published, so a
  consumer's MockBukkit tests could not load `MessageUtil`
  (`NoClassDefFoundError: org/json/simple/parser/ParseException`) without adding json-simple
  themselves. Consumers that declare PluginUtils `provided` / `compileOnly` receive it as
  `provided`: on their compile and test classpaths, never shaded. The server still supplies it
  at runtime.
- `/dev` usage now names its required plugin argument
  (`/dev <plugin> [true | false | <level> | r]`), and its description says what it does instead
  of "manage debub output".

## [2.0.3] - 2026-09-21

No Java changes: the compiled classes are byte-identical to 2.0.2.

### Fixed

- `plugin.yml` said `permission:` instead of `permissions:`, so `pluginutil.developer` was never
  registered and `/dev` refused everyone under LuckPerms.
- The `Dynmap` softdepend matched nothing; the plugin is named `dynmap`.
- Dropped `mappings-version:`, which is not a `plugin.yml` key. Paper reads the mappings
  namespace from the jar manifest.

### Changed

- Tag pushes publish to repo.mcmiddleearth.com as `com.mcmiddleearth:PluginUtils`, so consumers
  no longer need JitPack's `com.github.MCME:PluginUtils` coordinate.

## [2.0.2] - 2026-08-11

### Fixed

- `NoSuchMethodError: Entity.saveWithoutId` on a Paper 26.2 server. 2.0.0 and 2.0.1 were built
  against the 26.1.2 dev bundle; this targets `26.2.build.111-stable`, and entity NBT is now
  written and read through `ValueOutput` / `ValueInput`.

## [2.0.1] - 2026-08-11

### Fixed

- `plugin.yml` placeholders are substituted at build time. 2.0.0 shipped a literal
  `${project.artifactId}` as the plugin name, which Paper refuses to load.

## [2.0.0] - 2026-08-11 [YANKED]

First portable release: Gradle + paperweight-userdev instead of a local server jar, Paper 26.1.2,
JDK 25. It does not load; use 2.0.1 or later.

[2.0.4]: https://github.com/MCME/PluginUtils/compare/2.0.3...2.0.4
[2.0.3]: https://github.com/MCME/PluginUtils/compare/2.0.2...2.0.3
[2.0.2]: https://github.com/MCME/PluginUtils/compare/2.0.1...2.0.2
[2.0.1]: https://github.com/MCME/PluginUtils/compare/2.0.0...2.0.1
[2.0.0]: https://github.com/MCME/PluginUtils/releases/tag/2.0.0
