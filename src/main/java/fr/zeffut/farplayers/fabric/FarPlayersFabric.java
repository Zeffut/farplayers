//? if fabric {
package fr.zeffut.farplayers.fabric;

import fr.zeffut.farplayers.config.ModConfig;
import fr.zeffut.farplayers.core.FarPlayers;
import fr.zeffut.farplayers.platform.Platform;
import fr.zeffut.farplayers.telemetry.Telemetry;
import fr.zeffut.farplayers.update.UpdateService;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric client entrypoint. Initializes config + the player-render controller + telemetry, and
 * emits the standard {@code client_started} / {@code mod_loaded} events. The actual far-player
 * rendering is done by {@code mixin.EntityRenderDistanceMixin}.
 */
public class FarPlayersFabric implements ClientModInitializer {
    private static final Logger LOG = LoggerFactory.getLogger("FarPlayers");

    @Override
    public void onInitializeClient() {
        // Touch config first so install_id / telemetry opt-out are resolved before any capture.
        ModConfig.get();

        String mc = Platform.mcVersion();
        String modVer = Platform.modVersion();

        // Player-render controller (reads config, seeds defaults, emits fp_config).
        FarPlayers.init("mod-fabric", mc, modVer);

        Map<String, Object> started = new LinkedHashMap<>();
        started.put("loader", "fabric");
        started.put("installed_mods_count", Platform.installedModCount());
        started.put("os_name", System.getProperty("os.name"));
        started.put("os_arch", System.getProperty("os.arch"));
        started.put("java_version", System.getProperty("java.version"));
        Telemetry.capture("client_started", "mod-fabric", mc, modVer, started);
        Telemetry.capture("mod_loaded", "mod-fabric", mc, modVer, Map.of("loader", "fabric"));
        Telemetry.regionProbe("mod-fabric", mc, modVer);
        Telemetry.startHeartbeat("mod-fabric", mc, modVer);

        // Silent auto-update module (shared across Zeffut mods; first mod to start wins the lock).
        UpdateService.start();

        LOG.info("[FarPlayers] initialized on fabric {} (players render up to {} chunks, telemetry={})",
                mc, FarPlayers.distanceChunks(), Telemetry.enabled());
    }
}
//?}
