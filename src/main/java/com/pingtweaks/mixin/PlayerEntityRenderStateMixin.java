package com.pingtweaks.mixin;

import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntityRenderState.class)
public class PlayerEntityRenderStateMixin implements PingRenderStateAccessor {

    @Unique
    private int pingtweaks$ping = 0;

    @Override
    public int pingtweaks$getPing() {
        return pingtweaks$ping;
    }

    @Override
    public void pingtweaks$setPing(int ping) {
        this.pingtweaks$ping = ping;
    }
}
