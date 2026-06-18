package fr.zeffut.farplayers.mixin;

import fr.zeffut.farplayers.core.FarPlayers;

//? if fabric {
//? if >=26.1 {
/*import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.player.AbstractClientPlayer;
*///?} else {
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
//?}
//?}
//? if neoforge {
/*import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.player.AbstractClientPlayer;
*///?}

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes far players visible when they stand over un-rendered terrain. There, the client has no
 * light data, so the player model's packed light is 0 and it renders pitch black — you only see the
 * (always-lit) nametag. We force full-bright lighting for other players whose computed light is 0,
 * so their model is actually visible. Near players keep their real lighting (their light is rarely 0).
 */
@Mixin(EntityRenderer.class)
public abstract class EntityLightMixin {

    //? if fabric {
    //? if >=26.1 {
    /*@Inject(method = "getPackedLightCoords", at = @At("TAIL"), cancellable = true)
    *///?} else {
    @Inject(method = "getLight", at = @At("TAIL"), cancellable = true)
    //?}
    //?}
    //? if neoforge {
    /*@Inject(method = "getPackedLightCoords", at = @At("TAIL"), cancellable = true)*/
    //?}
    private void farplayers$fullbrightDarkPlayers(Entity entity, float tickDelta,
                                                  CallbackInfoReturnable<Integer> cir) {
        if (!FarPlayers.enabled()) return;
        //? if fabric {
        //? if >=26.1 {
        /*if (!(entity instanceof AbstractClientPlayer)) return;
        *///?} else {
        if (!(entity instanceof AbstractClientPlayerEntity)) return;
        //?}
        //?}
        //? if neoforge {
        /*if (!(entity instanceof AbstractClientPlayer)) return;*/
        //?}
        if (cir.getReturnValueI() == 0) {
            //? if >=26.1 {
            /*cir.setReturnValue(net.minecraft.util.LightCoordsUtil.FULL_BRIGHT);
            *///?} else {
            //? if fabric {
            cir.setReturnValue(net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE);
            //?}
            //? if neoforge {
            /*cir.setReturnValue(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT);*/
            //?}
            //?}
        }
    }
}
