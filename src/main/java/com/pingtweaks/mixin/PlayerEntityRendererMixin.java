package com.pingtweaks.mixin;

import com.pingtweaks.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * IMPORTANT - read before building:
 *
 * This targets Minecraft 1.21's (pre-render-state-refactor) renderer API,
 * where PlayerEntityRenderer overrides hasLabel(AbstractClientPlayerEntity)
 * directly to decide whether a name tag gets drawn at all. If compilation
 * fails with "cannot find target method" for hasLabel:
 *   1. Open the decompiled PlayerEntityRenderer (Loom's "Generate Sources"
 *      task, or your IDE's decompiler) and check the exact method name/
 *      signature for your Yarn build - it may be declared on EntityRenderer
 *      or LivingEntityRenderer instead, in which case move this @Inject to
 *      a mixin targeting that class (with the matching generic type).
 *   2. Update the @Inject "method" value to match.
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(method = "hasLabel", at = @At("RETURN"), cancellable = true)
    private void pingtweaks$forceNameInF5(AbstractClientPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !player.getUuid().equals(client.player.getUuid())) {
            return; // only touch the local player's own name tag
        }
        cir.setReturnValue(ModConfig.INSTANCE.nameInF5);
    }
}
