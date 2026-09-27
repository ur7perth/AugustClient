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
