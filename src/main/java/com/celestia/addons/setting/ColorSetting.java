package com.celestia.addons.setting;

import java.awt.Color;

public class ColorSetting extends Setting {
    private int color; // ARGB
    private boolean expanded; // For the picker

    public ColorSetting(String name, Color defaultColor) {
        super(name);
        this.color = defaultColor.getRGB();
    }

    public int getRGB() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setColor(Color color) {
        this.color = color.getRGB();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    // Helper to get components
    public int getRed() {
        return (color >> 16) & 0xFF;
    }

    public int getGreen() {
        return (color >> 8) & 0xFF;
    }

    public int getBlue() {
        return color & 0xFF;
    }

    public int getAlpha() {
        return (color >> 24) & 0xFF;
    }

    public void setRed(int r) {
        color = (getAlpha() << 24) | (r << 16) | (getGreen() << 8) | getBlue();
    }

    public void setGreen(int g) {
        color = (getAlpha() << 24) | (getRed() << 16) | (g << 8) | getBlue();
    }

    public void setBlue(int b) {
        color = (getAlpha() << 24) | (getRed() << 16) | (getGreen() << 8) | b;
    }
}
