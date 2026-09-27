package com.pingtweaks.gui;

import com.pingtweaks.config.ModConfig;
import com.pingtweaks.gui.widget.OffsetSliderWidget;
import com.pingtweaks.gui.widget.ToggleWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * The main config GUI, opened with Right Shift. Visually modeled after the
 * reference screenshot: a dark rounded panel with a left sidebar of
 * categories and a right-hand content area. Only the "Player" category has
 * real settings in this mod; the rest are placeholders, exactly as
 * requested.
 */
public class ConfigScreen extends Screen {

    private enum Category {
        PLAYER("Player"),
        ITEMS("Items"),
        HUD("Hud"),
        MISC("Misc"),
        FONTS("Fonts"),
        VIEWMODEL("ViewModel"),
        CONFIG("Config");

        final String label;

        Category(String label) {
            this.label = label;
        }
    }

    private static final int PANEL_W = 380;
    private static final int PANEL_H = 320;
    private static final int SIDEBAR_W = 112;
    private static final int TOPBAR_H = 40;

    private static final int COLOR_PANEL_BG = 0xF0101018;
    private static final int COLOR_SIDEBAR_BG = 0xF0161620;
    private static final int COLOR_TOPBAR_BG = 0xF01A1A24;
    private static final int COLOR_SELECTED = 0x552F2BFF;
    private static final int COLOR_BOX_BG = 0x30272242;
    private static final int COLOR_BOX_BORDER = 0xFF6C5CE7;
    private static final int COLOR_TEXT = 0xFFE6E6EE;
    private static final int COLOR_TEXT_DIM = 0xFFA0A0B0;
    private static final int COLOR_ACCENT = 0xFF9B8CFF;

    private final Screen parent;
    private Category selected = Category.PLAYER;

    private int panelX, panelY;
    private final List<int[]> sidebarHitboxes = new ArrayList<>(); // {y, index}

    public ConfigScreen(Screen parent) {
        super(Text.literal("PingTweaks"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelX = (this.width - PANEL_W) / 2;
        panelY = (this.height - PANEL_H) / 2;
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        this.clearChildren();
        sidebarHitboxes.clear();

        if (selected == Category.PLAYER) {
            buildPlayerTab();
        }
    }

    private void buildPlayerTab() {
        ModConfig cfg = ModConfig.INSTANCE;

        int contentX = panelX + SIDEBAR_W + 16;
        int contentW = PANEL_W - SIDEBAR_W - 32;
        int boxY = panelY + TOPBAR_H + 14;
        int rowH = 22;
        int rowsInBox = 3 + (cfg.pingAboveName ? 1 : 0);
        int boxH = 26 + rowsInBox * rowH + 8;

        int toggleX = contentX + contentW - 34 - 10;

        // Header toggle for the whole box ("Name Tweaks")
        addDrawableChild(new ToggleWidget(toggleX, boxY + 6,
                () -> cfg.nameTweaksEnabled,
                v -> { cfg.nameTweaksEnabled = v; ModConfig.save(); }));

        int rowY = boxY + 30;

        addDrawableChild(new ToggleWidget(toggleX, rowY,
                () -> cfg.nameTagPing,
                v -> { cfg.nameTagPing = v; ModConfig.save(); }));
        rowY += rowH;

        addDrawableChild(new ToggleWidget(toggleX, rowY,
                () -> cfg.pingColor,
                v -> { cfg.pingColor = v; ModConfig.save(); }));
        rowY += rowH;

        addDrawableChild(new ToggleWidget(toggleX, rowY,
                () -> cfg.pingAboveName,
                v -> { cfg.pingAboveName = v; ModConfig.save(); rebuildWidgets(); }));
        rowY += rowH;

        if (cfg.pingAboveName) {
            addDrawableChild(new OffsetSliderWidget(contentX + 90, rowY + 2, contentW - 100, 16,
                    cfg.pingAboveNameOffset,
                    v -> { cfg.pingAboveNameOffset = v; ModConfig.save(); }));
        }

        // "Name In F5" sits below the box, like "Zoom sensitivity" in the reference.
        int belowBoxY = boxY + boxH + 16;
        addDrawableChild(new ToggleWidget(toggleX, belowBoxY,
                () -> cfg.nameInF5,
                v -> { cfg.nameInF5 = v; ModConfig.save(); }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // Panel
        context.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, COLOR_PANEL_BG);

        // Top bar
        context.fill(panelX, panelY, panelX + PANEL_W, panelY + TOPBAR_H, COLOR_TOPBAR_BG);
        context.drawText(this.textRenderer, Text.literal("\u2715 PingTweaks"), panelX + 14, panelY + 14, COLOR_TEXT, false);

        // Sidebar
        context.fill(panelX, panelY + TOPBAR_H, panelX + SIDEBAR_W, panelY + PANEL_H, COLOR_SIDEBAR_BG);
        drawSidebar(context, mouseX, mouseY);

        // Content title
        int contentX = panelX + SIDEBAR_W + 16;
        int contentY = panelY + TOPBAR_H + 14;

        if (selected == Category.PLAYER) {
            drawPlayerTab(context, contentX, contentY);
        } else {
            context.drawText(this.textRenderer, Text.literal("Player"), contentX, contentY, COLOR_TEXT_DIM, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawSidebar(DrawContext context, int mouseX, int mouseY) {
        Category[] all = Category.values();
        int y = panelY + TOPBAR_H + 10;
        int rowH = 26;

        sidebarHitboxes.clear();
        for (int i = 0; i < all.length; i++) {
            int entryY = y + i * rowH;
            sidebarHitboxes.add(new int[]{entryY, i});

            boolean isSelected = all[i] == selected;
            boolean hovered = mouseX >= panelX && mouseX <= panelX + SIDEBAR_W
                    && mouseY >= entryY && mouseY <= entryY + rowH;

            if (isSelected) {
                context.fill(panelX, entryY, panelX + SIDEBAR_W, entryY + rowH, COLOR_SELECTED);
                context.fill(panelX, entryY, panelX + 3, entryY + rowH, COLOR_ACCENT);
            } else if (hovered) {
                context.fill(panelX, entryY, panelX + SIDEBAR_W, entryY + rowH, 0x22FFFFFF);
            }

            int textColor = isSelected ? COLOR_TEXT : COLOR_TEXT_DIM;
            context.drawText(this.textRenderer, Text.literal(all[i].label), panelX + 16, entryY + 9, textColor, false);
        }
    }

    private void drawPlayerTab(DrawContext context, int contentX, int contentY) {
        ModConfig cfg = ModConfig.INSTANCE;
        int contentW = PANEL_W - SIDEBAR_W - 32;

        int boxY = contentY;
        int rowH = 22;
        int rowsInBox = 3 + (cfg.pingAboveName ? 1 : 0);
        int boxH = 26 + rowsInBox * rowH + 8;

        // Box border + background
        context.fill(contentX - 6, boxY - 6, contentX + contentW + 6, boxY + boxH + 6, COLOR_BOX_BG);
        drawBoxBorder(context, contentX - 6, boxY - 6, contentX + contentW + 6, boxY + boxH + 6, COLOR_BOX_BORDER);

        context.drawText(this.textRenderer, Text.literal("Name Tweaks"), contentX, boxY + 10, COLOR_TEXT, false);

        int rowY = boxY + 30;
        context.drawText(this.textRenderer, Text.literal("Name Tag Ping"), contentX, rowY + 5, COLOR_TEXT, false);
        rowY += rowH;
        context.drawText(this.textRenderer, Text.literal("Ping Color"), contentX, rowY + 5, COLOR_TEXT, false);
        rowY += rowH;
        context.drawText(this.textRenderer, Text.literal("Ping Above Name"), contentX, rowY + 5, COLOR_TEXT, false);
        rowY += rowH;

        if (cfg.pingAboveName) {
            context.drawText(this.textRenderer, Text.literal("Offset"), contentX, rowY + 5, COLOR_TEXT_DIM, false);
        }

        int belowBoxY = boxY + boxH + 16;
        context.drawText(this.textRenderer, Text.literal("Name In F5"), contentX, belowBoxY + 5, COLOR_TEXT, false);
    }

    private void drawBoxBorder(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1, y1, x2, y1 + 1, color);
        context.fill(x1, y2 - 1, x2, y2, color);
        context.fill(x1, y1, x1 + 1, y2, color);
        context.fill(x2 - 1, y1, x2, y2, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= panelX && mouseX <= panelX + SIDEBAR_W) {
            for (int[] hit : sidebarHitboxes) {
                int entryY = hit[0];
                if (mouseY >= entryY && mouseY <= entryY + 26) {
                    Category clicked = Category.values()[hit[1]];
                    if (clicked != selected) {
                        selected = clicked;
                        rebuildWidgets();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
