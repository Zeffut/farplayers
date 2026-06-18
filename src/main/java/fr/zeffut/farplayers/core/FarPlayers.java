package fr.zeffut.farplayers.core;

import fr.zeffut.farplayers.config.ModConfig;
import fr.zeffut.farplayers.telemetry.Telemetry;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mapping-agnostic controller for FarPlayers. Holds the configured player render distance and the
 * decision used by the rendering mixin, plus telemetry. References no Minecraft class, so it
 * compiles identically on every (MC x loader) node.
 *
 * <p>The mixin ({@code mixin.EntityRenderDistanceMixin}) calls {@link #shouldExtendRender(double)}
 * for each client player; everything version/loader-specific stays in the mixin, everything else
 * (config, clamping, telemetry) lives here.
 */
public final class FarPlayers {

    /** Settings keys inside {@code config/farplayers.json} ({@code "settings"} object). */
    public static final String SETTING_DISTANCE = "render_distance_chunks";
    public static final String SETTING_ENABLED = "enabled";

    private static final int DEFAULT_CHUNKS = 32;
    private static final int MIN_CHUNKS = 1;
    private static final int MAX_CHUNKS = 64;

    private static volatile boolean enabled = true;
    private static volatile int distanceChunks = DEFAULT_CHUNKS;
    private static volatile double maxRenderSqr = sqr(DEFAULT_CHUNKS);

    /** Usage is reported at most once per session (the mixin path runs every frame). */
    private static final AtomicBoolean ACTIVE_REPORTED = new AtomicBoolean(false);
    private static String source = "mod";
    private static String mcVersion = "unknown";
    private static String modVersion = "unknown";

    private FarPlayers() {}

    private static double sqr(int chunks) {
        double blocks = chunks * 16.0;
        return blocks * blocks;
    }

    /**
     * Called once from each loader entrypoint. Loads config, seeds the default options into the
     * generated config file (so they are discoverable), and emits the {@code fp_config} event.
     */
    public static void init(String source, String mcVersion, String modVersion) {
        FarPlayers.source = source;
        FarPlayers.mcVersion = mcVersion;
        FarPlayers.modVersion = modVersion;

        ModConfig cfg = ModConfig.get();
        if (!cfg.settings().containsKey(SETTING_DISTANCE)) {
            cfg.putSetting(SETTING_DISTANCE, String.valueOf(DEFAULT_CHUNKS));
        }
        if (!cfg.settings().containsKey(SETTING_ENABLED)) {
            cfg.putSetting(SETTING_ENABLED, "true");
        }

        distanceChunks = clamp(parseInt(cfg.setting(SETTING_DISTANCE, String.valueOf(DEFAULT_CHUNKS))),
                MIN_CHUNKS, MAX_CHUNKS);
        enabled = Boolean.parseBoolean(cfg.setting(SETTING_ENABLED, "true"));
        maxRenderSqr = sqr(distanceChunks);

        Telemetry.captureModEvent("config", source, mcVersion, modVersion,
                Map.of("render_distance_chunks", distanceChunks, "enabled", enabled));
    }

    public static boolean enabled() { return enabled; }

    public static int distanceChunks() { return distanceChunks; }

    /**
     * Decision used by the rendering mixin: should this player entity be force-rendered at the given
     * squared distance (in blocks^2) from the camera, overriding the vanilla per-entity distance
     * cull? Frustum culling still applies downstream (in {@code EntityRenderDispatcher}), so
     * off-screen players are not drawn — this only lifts the distance ceiling.
     */
    public static boolean shouldExtendRender(double sqrDistanceToCamera) {
        if (!enabled) return false;
        if (sqrDistanceToCamera > maxRenderSqr) return false;
        // First time we actually extend a player's render this session: report usage once.
        if (ACTIVE_REPORTED.compareAndSet(false, true)) {
            Telemetry.captureModEvent("active", source, mcVersion, modVersion,
                    Map.of("render_distance_chunks", distanceChunks));
        }
        return true;
    }

    private static int clamp(int v, int lo, int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (RuntimeException e) {
            return DEFAULT_CHUNKS;
        }
    }
}
