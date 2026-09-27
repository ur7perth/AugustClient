package com.pingtweaks.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * A small pill-shaped on/off switch, matching the toggle switches in the
 * reference screenshot (purple when on, dark gray when off).
 */
public class ToggleWidget extends ClickableWidget {

    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;

    public ToggleWidget(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        super(x, y, 34, 18, Text.empty());
        this.getter = getter;
        this.setter = setter;
    }

    public boolean value() {
        return getter.getAsBoolean();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        setter.accept(!value());
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean on = value();
        int track = on ? 0xFF7B61FF : 0xFF3A3A46;
        context.fill(getX(), getY(), getX() + width, getY() + height, track);

        int knobSize = height - 4;
        int knobX = on ? getX() + width - knobSize - 2 : getX() + 2;
        context.fill(knobX, getY() + 2, knobX + knobSize, getY() + 2 + knobSize, 0xFFFFFFFF);
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}
