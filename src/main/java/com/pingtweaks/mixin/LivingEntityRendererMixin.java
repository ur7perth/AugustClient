package com.pingtweaks.mixin;

import com.pingtweaks.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * IMPORTANT - read before building:
 *
 * This targets Minecraft 1.21's (pre-render-state-refactor) renderer API:
 * renderLabelIfPresent(T entity, Text text, MatrixStack matrices,
 * VertexConsumerProvider vertexConsumerProvider, int light, float tickDelta),
 * where T is the actual entity (not a separate "render state" object - that
 * only exists in later Minecraft versions). Note the trailing tickDelta
 * parameter - Mixin infers the target descriptor from this handler's own
 * parameters, so if it's missing here the injector fails to resolve any
 * target at all ("could not find any targets matching ...").
 *
 * If compilation fails on the @Inject below:
 *   1. Open the decompiled LivingEntityRenderer (Loom's "Generate Sources"
 *      task) for your exact Yarn build and compare the real method name/
 *      parameter order.
 *   2. Update the @Inject "method" value and the generic bounds on this
 *      class to match.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void pingtweaks$onLabelRender(T entity, Text text, MatrixStack matrices,
                                           VertexConsumerProvider vertexConsumers, int light,
                                           float tickDelta, CallbackInfo ci) {
        ModConfig cfg = ModConfig.INSTANCE;
        if (!cfg.nameTweaksEnabled || !cfg.nameTagPing) return;
        if (!(entity instanceof AbstractClientPlayerEntity player)) return;

        int ping = 0;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(player.getUuid());
            if (entry != null) {
                ping = entry.getLatency();
            }
        }

        int color = ModConfig.colorFor(ping);
        MutableText pingText = Text.literal(ping + "ms");
        pingText.setStyle(pingText.getStyle().withColor(color));

        float entityHeight = entity.getHeight();

        if (!cfg.pingAboveName) {
            float nameHalfWidthPx = client.textRenderer.getWidth(text) / 2.0F;
            float xOffset = (nameHalfWidthPx + 4F) * 0.025F;
            pingtweaks$drawBillboard(entityHeight, matrices, vertexConsumers, light, pingText, xOffset, 0.5F);
        } else {
            float xOffset = (float) (cfg.pingAboveNameOffset * 0.025D);
            pingtweaks$drawBillboard(entityHeight, matrices, vertexConsumers, light, pingText, xOffset, 0.85F);
        }
    }

    @Unique
    private void pingtweaks$drawBillboard(float entityHeight, MatrixStack matrices,
                                           VertexConsumerProvider vertexConsumers, int light,
                                           Text text, float xOffset, float yOffset) {
        MinecraftClient client = MinecraftClient.getInstance();
        matrices.push();
        matrices.translate(xOffset, entityHeight + yOffset, 0.0D);
        matrices.multiply(client.getEntityRenderDispatcher().getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);

        MatrixStack.Entry entry = matrices.peek();
        float halfWidthPx = client.textRenderer.getWidth(text) / 2.0F;
        int background = (int) (0.25F * 255.0F) << 24;

        client.textRenderer.draw(text, halfWidthPx, 0, 0xFFFFFF, false, entry.getPositionMatrix(),
                vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, background, light);

        matrices.pop();
    }
}
