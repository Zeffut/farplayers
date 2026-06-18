package fr.zeffut.farplayers.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import fr.zeffut.farplayers.core.FarPlayers;

// The entity-collection method and the per-entity section check diverged across versions, so each
// (mapping x version) is a flat, mutually-exclusive Stonecutter branch:
//   Yarn 1.21.11   (fabric && <26.1) : WorldRenderer.fillEntityRenderStates -> isRenderingReady
//   Mojmap 1.21.11 (neoforge && <26.1): LevelRenderer.extractVisibleEntities -> isSectionCompiledAndVisible
//   Mojmap 26.1.x  (>=26.1 && <26.2)  : LevelRenderer.extractVisibleEntities -> isSectionCompiledAndVisible (LevelRenderState pkg moved)
//   Mojmap 26.2    (>=26.2)           : LevelExtractor.isEntityVisible        -> LevelRenderer.isSectionCompiledAndVisible
//? if fabric && <26.1 {
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
//?}
//? if neoforge && <26.1 {
/*import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.player.AbstractClientPlayer;
*///?}
//? if >=26.1 && <26.2 {
/*import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.player.AbstractClientPlayer;
*///?}
//? if >=26.2 {
/*import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.player.AbstractClientPlayer;
*///?}

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Second client render gate, restricted to OTHER PLAYERS. The entity-collection loop renders an
 * entity only if its chunk section is "rendering ready" (within the local render grid). With a low
 * render distance, far players the server sent are dropped here before the per-entity cull. We force
 * the section check to pass for client players only; the distance cull
 * ({@code EntityRenderDistanceMixin}) and the vanilla frustum check still decide what draws.
 */
//? if fabric && <26.1 {
@Mixin(WorldRenderer.class)
//?}
//? if neoforge && <26.1 {
/*@Mixin(LevelRenderer.class)
*///?}
//? if >=26.1 && <26.2 {
/*@Mixin(LevelRenderer.class)
*///?}
//? if >=26.2 {
/*@Mixin(LevelExtractor.class)
*///?}
public abstract class EntitySectionGateMixin {

    //? if fabric && <26.1 {
    @ModifyExpressionValue(method = "fillEntityRenderStates(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/client/render/state/WorldRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;isRenderingReady(Lnet/minecraft/util/math/BlockPos;)Z"))
    //?}
    //? if neoforge && <26.1 {
    /*@ModifyExpressionValue(method = "extractVisibleEntities(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/renderer/state/LevelRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;isSectionCompiledAndVisible(Lnet/minecraft/core/BlockPos;)Z"))
    *///?}
    //? if >=26.1 && <26.2 {
    /*@ModifyExpressionValue(method = "extractVisibleEntities(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;isSectionCompiledAndVisible(Lnet/minecraft/core/BlockPos;)Z"))
    *///?}
    //? if >=26.2 {
    /*@ModifyExpressionValue(method = "isEntityVisible(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;isSectionCompiledAndVisible(Lnet/minecraft/core/BlockPos;)Z"))
    *///?}
    private boolean farplayers$forcePlayerSection(boolean ready, @Local Entity entity) {
        if (ready || !FarPlayers.enabled()) return ready;
        //? if fabric && <26.1 {
        return entity instanceof AbstractClientPlayerEntity;
        //?}
        //? if >=26.1 || neoforge {
        /*return entity instanceof AbstractClientPlayer;*/
        //?}
    }
}
