package com.pingtweaks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Holds every setting shown in the config GUI and persists them to
 * <minecraft>/config/pingtweaks.json
 *
 * Everything here is client-side only: nothing in this class ever talks
 * to a server, so this mod works perfectly fine on servers that don't
 * have it installed.
 */
public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("pingtweaks.json");

    public static ModConfig INSTANCE = new ModConfig();

    // ----- "Name Tweaks" box -----
    /** Master switch for the whole "Name Tweaks" box. */
    public boolean nameTweaksEnabled = true;

    /** Show the player's ping next to / above their name tag. */
    public boolean nameTagPing = true;

    /** If true, ping text is colored by latency tier. If false, it's always gray. */
    public boolean pingColor = true;

    /** If true, ping is drawn on its own line above the name. If false, beside the name. */
    public boolean pingAboveName = false;

    /**
     * Horizontal offset of the "above name" ping line, ranges from -30 (centered
     * over the name) to 0 (pushed away to the right). Only used when pingAboveName
     * is true. Matches the -15 default shown in the reference screenshot's slider.
     */
    public double pingAboveNameOffset = -15.0D;

    // ----- Below the box -----
    /** Force-render your own name tag above your head while in 3rd person (F5). */
    public boolean nameInF5 = false;

    // ----- Ping color thresholds (ms) -----
    public static final int TIER_GREEN_MAX = 80;   // <= 80 -> green
    public static final int TIER_ORANGE_MAX = 180;  // 80 < ping <= 180 -> orange, > 180 -> red

    public static void load() {
        if (!Files.exists(PATH)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
            ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
            if (loaded != null) {
                INSTANCE = loaded;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 0 = gray (disabled/unknown), 1 = green, 2 = orange, 3 = red */
    public static int tierFor(int ping) {
        if (!INSTANCE.pingColor) return 0;
        if (ping <= TIER_GREEN_MAX) return 1;
        if (ping <= TIER_ORANGE_MAX) return 2;
        return 3;
    }

    public static int colorFor(int ping) {
        switch (tierFor(ping)) {
            case 1: return 0xFF55FF55; // green
            case 2: return 0xFFFFAA00; // orange
            case 3: return 0xFFFF5555; // red
            default: return 0xFFAAAAAA; // gray
        }
    }
}
