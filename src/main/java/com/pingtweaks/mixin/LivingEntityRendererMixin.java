package com.pingtweaks.mixin;

import com.pingtweaks.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
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
 * This targets Minecraft 1.21's "entity render state" pipeline with Yarn
 * mappings current as of writing. `renderLabelIfPresent` is declared on the
 * generic LivingEntityRenderer<T, S, M> (that's why we mixin into that class
 * directly rather than PlayerEntityRenderer - PlayerEntityRenderer doesn't
 * override it, so the bytecode only exists on LivingEntityRenderer). If the
 * project fails to compile because this method's name, parameter order, or
 * the `height` field on the render state has changed:
 *   1. Use Loom's "Generate Sources" task (or your IDE's decompiler) to look
 *      at the real LivingEntityRenderer / LivingEntityRenderState classes for
 *      your exact mappings build.
 *   2. Update the @Inject "method" value and the field access below to match.
 * The overall approach - read the ping we stashed in PlayerEntityRendererMixin
 * off the render state, then draw a small billboard text next to/above the
 * name - stays valid regardless of small naming differences.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends net.minecraft.entity.LivingEntity, S extends LivingEntityRenderState, M extends net.minecraft.client.render.entity.model.EntityModel<? super S>> {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"))
    private void pingtweaks$onLabelRender(S genericState, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ModConfig cfg = ModConfig.INSTANCE;
        if (!cfg.nameTweaksEnabled || !cfg.nameTagPing) return;
        if (!(genericState instanceof PlayerEntityRenderState state)) return;

        int ping = ((PingRenderStateAccessor) state).pingtweaks$getPing();
        int color = ModConfig.colorFor(ping);
        MutableText pingText = Text.literal(ping + "ms");
        pingText.setStyle(pingText.getStyle().withColor(color));

        if (!cfg.pingAboveName) {
            // Beside the name: push it out to the right of wherever the name
            // label ends.
            float nameHalfWidthPx = state.nameLabel != null
                    ? MinecraftClient.getInstance().textRenderer.getWidth(state.nameLabel) / 2.0F : 0F;
            float xOffset = (nameHalfWidthPx + 4F) * 0.025F;
            pingtweaks$drawBillboard(state, matrices, vertexConsumers, light, pingText, xOffset, 0.5F);
        } else {
            // Above the name: separate line, horizontal position controlled
            // by the "Ping Above Name" offset slider (-30 = centered over the
            // name, 0 = pushed away to the right).
            float xOffset = (float) (cfg.pingAboveNameOffset * 0.025D);
            pingtweaks$drawBillboard(state, matrices, vertexConsumers, light, pingText, xOffset, 0.85F);
        }
    }

    @Unique
    private void pingtweaks$drawBillboard(PlayerEntityRenderState state, MatrixStack matrices,
                                           VertexConsumerProvider vertexConsumers, int light,
                                           Text text, float xOffset, float yOffset) {
        MinecraftClient client = MinecraftClient.getInstance();
        matrices.push();
        matrices.translate(xOffset, state.height + yOffset, 0.0D);
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
