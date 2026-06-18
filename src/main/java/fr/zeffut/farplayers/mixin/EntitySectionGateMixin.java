package fr.zeffut.farplayers.mixin;

import fr.zeffut.farplayers.core.FarPlayers;

//? if fabric {
//? if >=26.1 {
/*import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
*///?} else {
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
//?}
//?}
//? if neoforge {
/*import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
*///?}

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Second client render gate. The entity-collection loop only renders an entity whose chunk section
 * is "rendering ready" (i.e. within the local render grid). With a low render distance, the far
 * players the server did send are dropped here before the per-entity cull is even consulted. We
 * force the section check to pass; the distance cull ({@code EntityRenderDistanceMixin}) and the
 * vanilla frustum check still decide what actually draws.
 *
 * <p>We redirect the CALL to {@code isRenderingReady} (not its body) so this composes with
 * Sodium/Embeddium, which replace that method's body with their own section-readiness lookup.
 */
//? if fabric {
//? if >=26.1 {
/*@Mixin(LevelRenderer.class)
*///?} else {
@Mixin(WorldRenderer.class)
//?}
//?}
//? if neoforge {
/*@Mixin(LevelRenderer.class)*/
//?}
public abstract class EntitySectionGateMixin {

    //? if fabric {
    //? if >=26.1 {
    /*// TODO(26.x Mojmap): verify method + isSectionReady/isRenderingReady names by javap before building 26.x nodes.
    @Redirect(method = "MOJMAP_TODO_fillEntityRenderStates", at = @At(value = "INVOKE", target = "MOJMAP_TODO_isRenderingReady"))
    private boolean farplayers$forceSection(LevelRenderer self, BlockPos pos) {
        return FarPlayers.enabled() || self.isSectionReady(pos);
    }
    *///?} else {
    @Redirect(
            method = "fillEntityRenderStates",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;isRenderingReady(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean farplayers$forceSection(WorldRenderer self, BlockPos pos) {
        return FarPlayers.enabled() || self.isRenderingReady(pos);
    }
    //?}
    //?}
    //? if neoforge {
    /*@Redirect(method = "MOJMAP_TODO_fillEntityRenderStates", at = @At(value = "INVOKE", target = "MOJMAP_TODO_isRenderingReady"))
    private boolean farplayers$forceSection(LevelRenderer self, BlockPos pos) {
        return FarPlayers.enabled() || self.isSectionReady(pos);
    }*/
    //?}
}
