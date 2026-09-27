package com.pingtweaks;

import com.pingtweaks.config.ModConfig;
import com.pingtweaks.gui.ConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side entrypoint. Registers the "open menu" keybind (default: Right Shift)
 * and loads the config from disk. This mod declares "environment": "client" in
 * fabric.mod.json, so Fabric Loader will refuse to load it on a dedicated server -
 * it is designed to be installed only by the player who wants it, nobody else
 * needs it and the server never has to know about it.
 */
public class PingTweaksClient implements ClientModInitializer {

    public static final String MOD_ID = "pingtweaks";

    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pingtweaks.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.pingtweaks.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ConfigScreen(null));
                }
            }
        });
    }

    public static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }
}
