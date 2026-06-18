# FarPlayers

**See other players from much farther away — without cranking up your render distance.**

FarPlayers decouples *player rendering* from *terrain rendering*. Keep a low render distance for
FPS, and other players still keep showing up to a configurable distance (default **32 chunks**),
even when they are standing over terrain your client hasn't drawn.

It is a **client-side** mod. It does **not** reveal anything hidden: it only keeps drawing players
your client already knows about (within the server's entity tracking range). Frustum culling stays
intact — players off-screen or behind you are never drawn. **No radar, no ESP, no wallhack.**

## What it does

In vanilla, a player's model stops rendering at roughly `64 × entityDistanceScale × modelSize`
blocks (about **8 chunks** at default settings) — regardless of how far the server still tracks
them. FarPlayers lifts that per-player cap to your configured distance.

| Render distance | Player at 5 ch | 15 ch | 25 ch | 32 ch | > 32 ch |
|-----------------|:--:|:--:|:--:|:--:|:--:|
| 8 chunks        | ✅ | ✅ | ✅ | ✅ | ❌ hidden |

> The reachable distance is still bounded by how far the **server** sends you other players (its
> view / entity-tracking distance). On servers with a small view distance, players simply aren't
> tracked that far and nothing can draw them. FarPlayers shines in singleplayer, on LAN, and on
> servers with a generous view distance.

## Configuration

Edit `config/farplayers.json` (created on first launch):

```json
{
  "telemetry": true,
  "install_id": "…",
  "settings": {
    "render_distance_chunks": "32",
    "enabled": "true"
  }
}
```

- **`render_distance_chunks`** — how far (in chunks) players keep rendering. Default `32`, clamped to
  `1–64`.
- **`enabled`** — master switch for the feature.

## Loaders & versions

Fabric and NeoForge, Minecraft **1.21.11**, **26.1**, **26.1.1**, **26.1.2**, **26.2**.
Fabric needs only **Fabric Loader** (no Fabric API).

## Privacy / telemetry

Anonymous usage telemetry (PostHog) is **on by default and opt-out**. It sends an install id and
coarse counters (loader, MC version, configured distance) — never your IP, location, or anything
about other players. Turn it off any time with `"telemetry": false` in the config, or the JVM flag
`-Dfarplayers.telemetry=false`. It is always off in development environments.

## Building

```bash
# 1.21.11 (JDK 21)
JAVA_HOME=<jdk21> ./gradlew :1.21.11-fabric:build :1.21.11-neoforge:build
# 26.x fabric — Loom needs Gradle itself on JDK 25
JAVA_HOME=<jdk25> ./gradlew :26.2-fabric:build
# 26.x neoforge — toolchain only, Gradle on JDK 21
JAVA_HOME=<jdk21> ./gradlew :26.2-neoforge:build
```

Edit `org.gradle.java.installations.paths` in `gradle.properties` to match your JDKs.

## How it works

A single client Mixin (`mixin/EntityRenderDistanceMixin`) injects at the tail of
`Entity#shouldRender(double, double, double)` — the per-entity distance cull — and, for client
players within the configured range, forces the result to `true`. The frustum check lives elsewhere
(`EntityRenderDispatcher`) and is untouched. Yarn (Fabric ≤1.21.x) vs Mojang-mapped (Fabric 26.x +
all NeoForge) name differences are handled with Stonecutter source gating.

## License

PolyForm Noncommercial License 1.0.0 — see [`LICENSE`](./LICENSE). Source-available, non-commercial.
