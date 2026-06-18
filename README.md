# FarPlayers — Stonecraft multi-loader mod template

This directory is **not a finished mod**. It is the reusable skeleton that `/mc-mod` copies and
specializes by substituting placeholders and renaming the Java package. Build correctness is
guaranteed for the 1.21.11 nodes (Fabric + NeoForge) under JDK 21.

## Stack (pinned)

| Component                  | Version                          |
|----------------------------|----------------------------------|
| Stonecutter                | `0.9.4` (split-buildscript)      |
| Gradle                     | `9.4.0` (wrapper committed)      |
| Fabric Loom                | `1.16.3`                         |
| NeoForge moddev            | `2.0.141`                        |
| mod-publish-plugin         | `0.8.4`                          |
| JDK (build)                | `21` for <26.1, `25` for >=26.1  |

No Architectury, no Forgix. Loader differences are handled with Stonecutter source gating
(`//? if fabric { }`, `//? if neoforge { }`, `//? if >=26.1 { } else { }`).

## Version matrix

| Node                 | MC       | Mappings                         | Buildable here |
|----------------------|----------|----------------------------------|----------------|
| `1.21.11-fabric`     | 1.21.11  | Yarn `1.21.11+build.4`           | yes (JDK 21)   |
| `1.21.11-neoforge`   | 1.21.11  | NeoForge `21.11.42`              | yes (JDK 21)   |
| `26.1.2-fabric`      | 26.1.2   | `mappings/mojang-26x-identity.jar` | needs JDK 25 |
| `26.1.2-neoforge`    | 26.1.2   | NeoForge `26.1.2.70-beta`        | needs JDK 25   |

Active / VCS node: `1.21.11-fabric`.

### JDK 21 vs 25 for 26.x

- MC 26.x requires **JDK 25**; 1.21.11 requires **JDK 21**. The build's Java toolchain picks the
  right compile JDK from `org.gradle.java.installations.paths` (in `gradle.properties`) based on
  `stonecutter.eval(mcVersion, ">=26.1")`.
- **Loom quirk:** Fabric Loom requires *Gradle itself* to run on the MC's Java version. So the
  `26.1.2-fabric` node must be invoked with `JAVA_HOME` pointing at a JDK 25. NeoForge moddev only
  needs the toolchain, so `26.1.2-neoforge` builds with Gradle on JDK 21.

```bash
# 1.21.11 (JDK 21):
JAVA_HOME=<jdk21> ./gradlew :1.21.11-fabric:build :1.21.11-neoforge:build --no-daemon
# 26.1.2 fabric needs Gradle on JDK 25:
JAVA_HOME=<jdk25> ./gradlew :26.1.2-fabric:build --no-daemon
# 26.1.2 neoforge:
JAVA_HOME=<jdk21> ./gradlew :26.1.2-neoforge:build --no-daemon
```

> Edit `org.gradle.java.installations.paths` in `gradle.properties` to match your machine's JDKs.

## Placeholders (substituted by /mc-mod)

These appear in **file contents only** — never in folder names.

| Placeholder          | Meaning / example                                   |
|----------------------|-----------------------------------------------------|
| `farplayers`         | lowercase mod id, e.g. `swordhitbox`                |
| `FarPlayers`       | display name, e.g. `Sword Hitbox`                   |
| `Renders other players up to 32 chunks away regardless of your terrain render distance, so distant players stay visible even over unloaded chunks. Configurable distance. Anonymous opt-out telemetry, on by default.`| one-line description                                |
| `fr.zeffut`          | Maven group / package root, e.g. `fr.zeffut`        |
| `Zeffut`         | author name                                         |
| `fp_`   | telemetry event prefix for mod-specific events      |
| `phc_zdMj4p5wo8EvfVApjb2EbfUHJ76zgYGM5wAGz5YJC359`    | PostHog project API key                             |
| `farplayers`  | Modrinth project id / slug                          |

The physical Java package is the literal `fr/zeffut/farplayers/...` (`package fr.zeffut.farplayers`).
`/mc-mod` renames it to `fr.zeffut.farplayers` (git mv + sed of the `package`/`import` lines) and
runs a `sed` pass over the placeholders above.

## How /mc-mod instantiates this template

1. Copy `template/` into the new mod repo.
2. `sed` every `__PLACEHOLDER__` to the chosen values across all files.
3. Rename the package: `git mv src/main/java/fr/zeffut/farplayers src/main/java/<group-path>/<modid>`
   and rewrite `package fr.zeffut.farplayers` / `import fr.zeffut.farplayers` accordingly.
4. Rename the asset namespace: `git mv src/main/resources/assets/farplayers src/main/resources/assets/<modid>`,
   so the in-jar icon path matches the mod id. The `icon`/`logoFile` references in
   `fabric.mod.json` and `neoforge.mods.toml` are written as `assets/farplayers/icon.png` and are
   rewritten by the same `farplayers` → `<modid>` substitution applied in step 3.
5. Update the entrypoint class references in `fabric.mod.json` and the `@Mod` id.
6. Adjust `org.gradle.java.installations.paths` for the target machine.
7. Build the matrix (see commands above) until green.

## Modules

| File                              | Role                                                                 |
|-----------------------------------|----------------------------------------------------------------------|
| `telemetry/Telemetry.java`        | PostHog client (mapping-agnostic, async, opt-out, install_id, `regionProbe()`). |
| `config/ModConfig.java`           | JSON config `config/farplayers.json` (telemetry flag, install_id, settings map). |
| `config/ConfigScreen.java.txt`    | Documented GUI stub — **not compiled** (no GUI dep). Rename to activate. |
| `platform/Platform.java`          | Loader detection (no Architectury), gated per loader.                |
| `net/ExampleC2SPayload.java`      | Example C2S networking payload, gated `//? if >=26.1` for the registry split. |
| `compat/Compat.java`              | Cross-version API shims (`//? if >=26.1`), e.g. `Identifier`.        |
| `fabric/ModTemplateFabric.java`   | Fabric `ClientModInitializer` — emits `client_started` + `mod_loaded`. |
| `neoforge/ModTemplateNeoForge.java` | NeoForge `@Mod` client — emits `client_started` + `mod_loaded`.    |

## Publishing (Modrinth)

`build.fabric.gradle.kts` / `build.neoforge.gradle.kts` wire `me.modmuss50.mod-publish-plugin`.
The `publishMods {}` block is **guarded** — it is only configured when `MODRINTH_TOKEN` is set, so
plain `build` never requires a token.

```bash
MODRINTH_TOKEN=xxxx ./gradlew :1.21.11-fabric:publishMods :1.21.11-neoforge:publishMods
```

Modrinth license id: `LicenseRef-PolyForm-Noncommercial-1.0.0` (see `LICENSE`).

## License

PolyForm Noncommercial License 1.0.0 — see [`LICENSE`](./LICENSE).
