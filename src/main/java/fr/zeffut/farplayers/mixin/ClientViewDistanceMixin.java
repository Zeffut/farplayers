package fr.zeffut.farplayers.mixin;

import fr.zeffut.farplayers.core.FarPlayers;

// Yarn (Fabric <26.1) vs Mojmap (Fabric >=26.1 + NeoForge): the options class and the
// client-info record differ in name.
//? if fabric {
//? if >=26.1 {
/*import net.minecraft.client.Options;
*///?} else {
import net.minecraft.client.option.GameOptions;
//?}
//?}
//? if neoforge {
/*import net.minecraft.client.Options;
*///?}

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Inflates the view distance the client REPORTS to the server, without touching the local terrain
 * render distance. This is the decisive fix: a server only tracks and sends another player if that
 * player is within the viewer's reported view distance (ChunkMap entity tracking). With a low render
 * distance, vanilla reports that low value, so the server never sends far players and there is
 * nothing for the render mixin to draw.
 *
 * <p>We modify only the value flowing into the Client Information packet (built once on join and on
 * every options change). {@code Options.getClampedViewDistance()} / terrain rendering is untouched,
 * so FPS savings from a low render distance are preserved. The reported value is clamped to the
 * 32-chunk server ceiling (see {@link FarPlayers#serverViewDistance(int)}). On a dedicated server
 * the value is further clamped down to the server's own view-distance.
 */
//? if fabric {
//? if >=26.1 {
/*@Mixin(Options.class)
*///?} else {
@Mixin(GameOptions.class)
//?}
//?}
//? if neoforge {
/*@Mixin(Options.class)*/
//?}
public abstract class ClientViewDistanceMixin {

    //? if fabric {
    //? if >=26.1 {
    /*@ModifyArg(method = "buildPlayerInformation", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ClientInformation;<init>(Ljava/lang/String;ILnet/minecraft/world/entity/player/ChatVisiblity;ZILnet/minecraft/world/entity/HumanoidArm;ZZLnet/minecraft/server/level/ParticleStatus;)V"), index = 1)
    *///?} else {
    @ModifyArg(
            method = "getSyncedOptions",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/common/SyncedClientOptions;<init>(Ljava/lang/String;ILnet/minecraft/network/message/ChatVisibility;ZILnet/minecraft/util/Arm;ZZLnet/minecraft/particle/ParticlesMode;)V"),
            index = 1)
    //?}
    //?}
    //? if neoforge {
    /*@ModifyArg(method = "buildPlayerInformation", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ClientInformation;<init>(Ljava/lang/String;ILnet/minecraft/world/entity/player/ChatVisiblity;ZILnet/minecraft/world/entity/HumanoidArm;ZZLnet/minecraft/server/level/ParticleStatus;)V"), index = 1)*/
    //?}
    private int farplayers$inflateReportedViewDistance(int original) {
        return FarPlayers.serverViewDistance(original);
    }
}
