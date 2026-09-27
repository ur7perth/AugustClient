package com.pingtweaks.mixin;

import com.pingtweaks.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * See LivingEntityRendererMixin in this package for the "read before
 * building" notes about Yarn mapping names - the same caveats apply here
 * for updateRenderState's descriptor and the `nameLabel` field.
 *
 * This mixin runs once per player per frame while their render state is
 * being (re)built from the real entity, and:
 *  1) reads their current ping from the tab list and stashes it on the
 *     render state (via PingRenderStateAccessor) so LivingEntityRendererMixin
 *     can read it back later when actually drawing text, and
 *  2) implements "Name In F5" by forcing the local player's own name label
 *     on/off.
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(
            method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void pingtweaks$captureState(AbstractClientPlayerEntity player, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        int ping = 0;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(player.getUuid());
            if (entry != null) {
                ping = entry.getLatency();
            }
        }
        ((PingRenderStateAccessor) state).pingtweaks$setPing(ping);

        // "Name In F5": force the local player's own name tag to show/hide
        // while looking at yourself in 3rd person. Other players are untouched.
        if (client.player != null && player.getUuid().equals(client.player.getUuid())) {
            if (ModConfig.INSTANCE.nameInF5) {
                if (state.nameLabel == null) {
                    state.nameLabel = Text.literal(player.getGameProfile().getName());
                }
            } else {
                state.nameLabel = null;
            }
        }
    }
}
