# Changelog

Notable changes to PluginUtils, starting with the 2.0.x line (Paper 26.x, Gradle +
paperweight-userdev). The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Releases are tagged bare (`2.0.4`, not `v2.0.4`), and pushing a tag publishes that version to
repo.mcmiddleearth.com.

## [Unreleased]

### Fixed

- A jar built on Windows differed from the one CI builds: `processResources` fills in `plugin.yml`
  line by line, and Gradle writes each line with the operating system's line separator, so a
  Windows build carried CRLF. The build now writes LF everywhere, and both give the same bytes.

## [2.0.5] - 2026-10-02

The public API is unchanged: plugins built against 2.0.2 or later need no rebuild.

### Fixed

- Clicks and tooltips on `FancyMessage`s work again on Minecraft 26.2. The messages were sent
  through `/tellraw` as JSON with the `clickEvent` and `hoverEvent` fields that Minecraft renamed
  in 1.21.5, and 26.2 ignores the old names: every message kept its text and colours but lost its
  click and its tooltip (MCME-Architect's flint block info, for one). `FancyMessage.send` now
  builds an Adventure component with the same colours, `§` codes, hex colours and formats, and
  sends it to the player directly.
- `MessageUtil.sendRawMessage` reads its JSON with Adventure and sends it to the player instead of
  dispatching `/tellraw` from the console, so JSON with the old field names keeps its clicks and
  tooltips too. JSON it cannot read is logged as a warning and not sent.
- A `#` in a tooltip or in message text is a colour only when six hex digits follow it. Otherwise
  it is text, so a tooltip such as "[#page]" no longer breaks its message (as on MCME-Architect's
  help pages), and "Rank #1" keeps the characters after the `#`; a `#` near the end of the text
  threw, in 2.0.4 too. In tooltips, `§5` and `&5` are dark purple, `&n` underlines, a `&` or `§`
  that starts no code stays text instead of swallowing the next character, and `\&` is a plain
  `&`.
- `TitleUtil`'s fallback, for when `Player.sendTitle` fails, sends the title with Adventure instead
  of dispatching `/title` from the console.
- A jar built locally right after a version bump could carry the previous version in its
  `plugin.yml`: `processResources` did not treat the version as an input, so Gradle skipped it as
  up to date. Published jars were never affected; CI and JitPack build from a clean checkout.
- The build works on a clean machine again. It used paperweight-userdev's moving
  `2.0.0-SNAPSHOT`, which began to require Gradle 9.7.1 on 2026-09-28, so CI, JitPack and any other
  fresh checkout on this project's Gradle 9.0.0 failed before compiling anything. It now pins the
  released `2.0.0-beta.23`. Builds that reused Gradle's cache were not affected.

### Changed

- Paged lists (`MessageUtil.sendFancyListMessage` and `sendFancyFileListMessage`) end with one
  line, `[‹ Prev] Page 2/3 [Next ›]`, as the chat menus of MCME's newer plugins do: aqua buttons
  that run the list command for the page before or after, and the page in gray. The header no
  longer ends with " [page 2/3]", and the "---^ page up ^---" and "---v page down v--" lines are
  gone. A list of one page has no such line.

What differs from sending through `/tellraw`:

- Messages can be sent from any thread; the console could only run `/tellraw` on the main thread.
- A typed `\n` in message text is still a line break. In a tooltip it stays as typed, as before;
  `hoverFormat` makes the line breaks there.
- Run and suggest commands lose the characters Minecraft does not allow in chat (`§`, control
  characters and DEL), as the client's chat box does too; Paper would drop the whole message.
  Copied text is kept as it is.
- A click on a web address Minecraft cannot open copies the address to the clipboard instead,
  with a warning in the log, in `FancyMessage` and `sendRawMessage` alike. Adventure refuses an
  address that is not a URI (one with a space, say), and a player's client refuses a message
  with one that is neither http nor https (such as `ftp://`).
- An empty tooltip is left out: the part shows no tooltip of its own, so in a message without a
  prefix it shows the first part's, as any part without a tooltip does.
- `sendRawMessage` does not fill in selector, score or NBT components, which `/tellraw` did.

### Added

- Tests, which `./gradlew build` runs with JUnit on MockBukkit.

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
JDK 25. It does not load, and its tag was withdrawn; use 2.0.1 or later.

[Unreleased]: https://github.com/MCME/PluginUtils/compare/2.0.5...HEAD
[2.0.5]: https://github.com/MCME/PluginUtils/compare/2.0.4...2.0.5
[2.0.4]: https://github.com/MCME/PluginUtils/compare/2.0.3...2.0.4
[2.0.3]: https://github.com/MCME/PluginUtils/compare/2.0.2...2.0.3
[2.0.2]: https://github.com/MCME/PluginUtils/compare/2.0.1...2.0.2
[2.0.1]: https://github.com/MCME/PluginUtils/compare/f13f3bf8b693d2178f725b13eefe3ce1f7cb1112...2.0.1
[2.0.0]: https://github.com/MCME/PluginUtils/commit/f13f3bf8b693d2178f725b13eefe3ce1f7cb1112
