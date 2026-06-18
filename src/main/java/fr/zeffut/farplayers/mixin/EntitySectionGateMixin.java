package fr.zeffut.farplayers.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import fr.zeffut.farplayers.core.FarPlayers;

//? if fabric {
//? if >=26.1 {
/*import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.player.AbstractClientPlayer;
*///?} else {
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
//?}
//?}
//? if neoforge {
/*import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.player.AbstractClientPlayer;
*///?}

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Second client render gate, restricted to OTHER PLAYERS. The entity-collection loop renders an
 * entity only if its chunk section is "rendering ready" (within the local render grid). With a low
 * render distance, far players the server sent are dropped here before the per-entity cull. We force
 * the section check to pass for client players only (so we don't reveal every far mob/item), letting
 * the distance cull ({@code EntityRenderDistanceMixin}) and the vanilla frustum check decide what
 * actually draws.
 *
 * <p>{@code @ModifyExpressionValue} on the CALL to {@code isRenderingReady} (not its body) composes
 * with Sodium/Embeddium, which replace that method's body. {@code @Local} captures the loop entity.
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
    /*@ModifyExpressionValue(method = "MOJMAP_TODO_fillEntityRenderStates", at = @At(value = "INVOKE", target = "MOJMAP_TODO_isSectionReady"))
    private boolean farplayers$forcePlayerSection(boolean ready, @Local Entity entity) {
        if (ready || !FarPlayers.enabled()) return ready;
        return entity instanceof AbstractClientPlayer;
    }
    *///?} else {
    @ModifyExpressionValue(
            method = "fillEntityRenderStates",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;isRenderingReady(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean farplayers$forcePlayerSection(boolean ready, @Local Entity entity) {
        if (ready || !FarPlayers.enabled()) return ready;
        return entity instanceof AbstractClientPlayerEntity;
    }
    //?}
    //?}
    //? if neoforge {
    /*@ModifyExpressionValue(method = "MOJMAP_TODO_fillEntityRenderStates", at = @At(value = "INVOKE", target = "MOJMAP_TODO_isSectionReady"))
    private boolean farplayers$forcePlayerSection(boolean ready, @Local Entity entity) {
        if (ready || !FarPlayers.enabled()) return ready;
        return entity instanceof AbstractClientPlayer;
    }*/
    //?}
}
