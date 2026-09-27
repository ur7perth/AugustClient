package com.pingtweaks.mixin;

/**
 * Implemented (via mixin) by PlayerEntityRenderState so we can stash the
 * player's ping on it during updateRenderState(), then read it back later
 * during renderLabelIfPresent() / our own label drawing. Render state objects
 * are rebuilt every frame from the actual entity, then handed to the renderer,
 * so this is the supported way to carry extra per-frame data across that gap
 * in the 1.21 "entity render state" pipeline.
 */
public interface PingRenderStateAccessor {
    int pingtweaks$getPing();

    void pingtweaks$setPing(int ping);
}
