package fr.zeffut.farplayers.mixin;

import fr.zeffut.farplayers.core.FarPlayers;

// Mapping divergence: Fabric <26.1 is Yarn, Fabric >=26.1 and all NeoForge are Mojang-mapped.
//? if fabric {
//? if >=26.1 {
/*import net.minecraft.world.entity.Entity;
import net.minecraft.client.player.AbstractClientPlayer;
*///?} else {
import net.minecraft.entity.Entity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
//?}
//?}
//? if neoforge {
/*import net.minecraft.world.entity.Entity;
import net.minecraft.client.player.AbstractClientPlayer;
*///?}

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps other players' models rendering up to a configurable distance (default 32 chunks),
 * independently of the terrain render distance.
 *
 * <p>Injects at the TAIL of the per-entity distance cull
 * {@code Entity#shouldRender(double, double, double)} — the method whose result, in vanilla, caps
 * player rendering at roughly {@code 64 * viewScale * boundingBoxSize} blocks (about 8 chunks at
 * default settings). For client players within range we override the result to {@code true}.
 *
 * <p>Frustum culling is performed separately by {@code EntityRenderDispatcher.shouldRender} (an
 * independent {@code frustum.isVisible(...)} check) and is left untouched, so off-screen players
 * are still not drawn. This only lifts the distance ceiling — it is not an ESP/wallhack and reveals
 * nothing the client does not already track.
 */
@Mixin(Entity.class)
public abstract class EntityRenderDistanceMixin {

    @Inject(method = "shouldRender(DDD)Z", at = @At("TAIL"), cancellable = true)
    private void farplayers$extendPlayerRender(double camX, double camY, double camZ,
                                               CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue())) return; // already rendering
        if (!FarPlayers.enabled()) return;

        Entity self = (Entity) (Object) this;
        //? if fabric {
        //? if >=26.1 {
        /*boolean isPlayer = self instanceof AbstractClientPlayer;
        *///?} else {
        boolean isPlayer = self instanceof AbstractClientPlayerEntity;
        //?}
        //?}
        //? if neoforge {
        /*boolean isPlayer = self instanceof AbstractClientPlayer;*/
        //?}
        if (!isPlayer) return;

        double dx = self.getX() - camX;
        double dy = self.getY() - camY;
        double dz = self.getZ() - camZ;
        if (FarPlayers.shouldExtendRender(dx * dx + dy * dy + dz * dz)) {
            cir.setReturnValue(true);
        }
    }
}
