# CLAUDE.md — working on a mod generated from this template

This repo was scaffolded from the local Stonecraft multi-loader template
(`mc-mod-factory/template/`). Before writing any code:

1. **Read `~/.claude/mc-conventions.md`** — the shared conventions for all generated mods
   (telemetry taxonomy, config layout, gating style, publish flow).
2. **Brainstorm + plan before coding.** Define the change, list the affected nodes/loaders, and
   write a short plan before touching files. Use the project's planning skills.
3. **Commit frequently**, in small focused commits, with clear messages.

## Build reminders

- Multi-loader x multi-version via Stonecutter split-buildscript (no Architectury / Forgix).
- Loader gating: `//? if fabric { }`, `//? if neoforge { }`. Version gating: `//? if >=26.1 { } else { }`.
- The compiled sources are the **Stonecutter-generated** ones under
  `build/generated/stonecutter/main`, not the raw `src/main`. The shared `src/` is the source of truth.
- JDK 21 for `<26.1`, JDK 25 for `>=26.1`. Fabric Loom needs **Gradle itself** on the MC's Java
  version (so `26.1.2-fabric` must run with `JAVA_HOME` = JDK 25); NeoForge moddev only needs the
  toolchain.
- Keep the build green: do not add GUI deps that vary across MC versions (the config screen ships
  as a non-compiled `.java.txt` stub on purpose).

## Auto-update module (`update/`)

- Every generated mod embeds the **silent Modrinth auto-updater** (`update/UpdateService.java`,
  `update/ModrinthApi.java`, `update/JanitorMain.java`), developed/tested in
  `~/Desktop/Projets/AutoUpdate` (private repo `Zeffut/autoupdate`). It is NOT a standalone mod.
- On client init it scans `mods/` (SHA-512), batch-asks Modrinth (`/version_files/update`) for the
  latest versions matching the MC version + loader, restricts to projects owned by `Zeffut`
  (config `update_owner` / `update_all` / `update_exclude`), stages downloads under
  `<gameDir>/.autoupdate/staging/` and swaps jars at shutdown (detached janitor process if locked).
- **One updater per instance**: a JVM-global lock (`zeffut.autoupdate.lock` system property) makes
  every other embedded copy dormant. Shared identifiers (`.autoupdate/` dir, `autoupdate.*` system
  properties, `app=autoupdate` telemetry segment) must stay IDENTICAL across mods — never
  placeholder-ize them.
- Telemetry: `upd_check_completed`, `upd_update_staged`, `upd_update_applied`, `upd_update_failed`
  are emitted via `Telemetry.captureForApp("autoupdate", ...)` with `host_mod` + `updater_version`
  props (PostHog dashboard 737633 tracks the module across all host mods).

## Telemetry

- `telemetry/Telemetry.java` is mapping-agnostic and opt-out
  (`config/<mod>.json` `"telemetry": false`, `-D<mod>.telemetry=false`, or any dev run).
- Standard events: `client_started`, `mod_loaded`, `session_heartbeat`, `command_used`,
  `ui_opened`, `ui_closed`, plus mod-specific events prefixed with the mod's event prefix.

## Publishing

- Modrinth via `me.modmuss50.mod-publish-plugin`, guarded behind `MODRINTH_TOKEN`.
- License: PolyForm Noncommercial 1.0.0 (`LicenseRef-PolyForm-Noncommercial-1.0.0`).
