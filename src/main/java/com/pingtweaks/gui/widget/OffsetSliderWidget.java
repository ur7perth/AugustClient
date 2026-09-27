package com.pingtweaks.gui.widget;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;

/**
 * Slider for the "Ping Above Name" horizontal offset, ranging from -30
 * (centered over the player's name) to 0 (pushed away to the right), just
 * like the Thickness slider in the reference screenshot.
 */
public class OffsetSliderWidget extends SliderWidget {

    private static final double MIN = -30.0D;
    private static final double MAX = 0.0D;

    private final DoubleConsumer setter;

    public OffsetSliderWidget(int x, int y, int width, int height, double initialValue, DoubleConsumer setter) {
        super(x, y, width, height, Text.empty(), (initialValue - MIN) / (MAX - MIN));
        this.setter = setter;
        updateMessage();
    }

    private double currentValue() {
        return MIN + this.value * (MAX - MIN);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Text.literal(String.valueOf(Math.round(currentValue()))));
    }

    @Override
    protected void applyValue() {
        setter.accept(currentValue());
    }
}
