package com.celestia.addons.gui;

import com.celestia.addons.feature.Feature;
import com.celestia.addons.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundEvents;

import java.awt.Color;
import java.util.List;

public class Panel {
    private int x, y, width, height;
    private final int headerHeight = 20;
    private final int itemHeight = 15;
    private final String category;
    private final List<Feature> features;
    private boolean open = true;
    private boolean dragging = false;
    private int dragX, dragY;
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // Colors
    private static final int HEADER_COLOR = 0xCC1E1E1E; // Dark Gray transparent
    private static final int BACKGROUND_COLOR = 0xB4282828; // Slightly lighter
    private static final int TEXT_COLOR_DISABLED = 0xFFAAAAAA; // Gray
    private static final int TEXT_COLOR_ENABLED = 0xFFA020F0; // Purple (Lila)

    public Panel(String category, int x, int y, List<Feature> features) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = 110;
        this.features = features;
        updateHeight();
    }

    private void updateHeight() {
        if (open) {
            int h = headerHeight + 2;
            for (Feature f : features) {
                h += itemHeight;
                if (f.isExpanded()) {
                    h += itemHeight; // Extra space for settings (Keybind)
                    // Add height for settings
                    for (com.celestia.addons.setting.Setting setting : f.getSettings()) {
                        if (setting.isVisible()) {
                            h += itemHeight;
                            if (setting instanceof com.celestia.addons.setting.ColorSetting
                                    && ((com.celestia.addons.setting.ColorSetting) setting).isExpanded()) {
                                h += itemHeight * 3; // RGB Sliders
                            }
                        }
                    }
                }
            }
            this.height = h + 2;
        } else {
            this.height = headerHeight;
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }

        // Draw Header
        RenderUtils.fill(context, x, y, x + width, y + headerHeight, HEADER_COLOR);

        // Draw Text
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int textWidth = tr.getWidth(category);
        context.drawTextWithShadow(tr, category, x + (width - textWidth) / 2, y + 6, 0xFFFFFFFF);

        // Draw Content
        if (open) {
            RenderUtils.fill(context, x, y + headerHeight, x + width, y + height, BACKGROUND_COLOR);

            int currentY = y + headerHeight + 2;
            for (Feature feature : features) {
                // Hover calculation
                boolean hovered = isHovered(mouseX, mouseY, x, currentY, width, itemHeight);
                int color = feature.isEnabled() ? TEXT_COLOR_ENABLED : TEXT_COLOR_DISABLED;

                // Draw background hover effect for the main button
                if (hovered) {
                    RenderUtils.fill(context, x + 2, currentY, x + width - 2, currentY + itemHeight,
                            0x30FFFFFF);
                }

                // Center Text
                int featWidth = tr.getWidth(feature.getName());
                context.drawTextWithShadow(tr, feature.getName(), x + (width - featWidth) / 2, currentY + 3, color);

                currentY += itemHeight;

                // Render Settings if expanded
                if (feature.isExpanded()) {
                    // Slight indentation for settings
                    // Keybind Setting
                    String keyName = feature.getKeybind() == -1 ? "None" : String.valueOf(feature.getKeybind());
                    String settingText = "Keybind: " + keyName;

                    int settingWidth = tr.getWidth(settingText);
                    // Slightly darker background for settings area?
                    RenderUtils.fill(context, x + 5, currentY, x + width - 5, currentY + itemHeight, 0x20000000);

                    context.drawTextWithShadow(tr, settingText, x + (width - settingWidth) / 2, currentY + 3,
                            0xFFDDDDDD);

                    currentY += itemHeight;

                    // Settings Rendering
                    for (com.celestia.addons.setting.Setting setting : feature.getSettings()) {
                        if (!setting.isVisible())
                            continue;

                        int setX = x + 5;
                        int setWidth = width - 10;

                        // Background for setting
                        RenderUtils.fill(context, setX, currentY, setX + setWidth, currentY + itemHeight, 0x20000000);

                        if (setting instanceof com.celestia.addons.setting.ModeSetting) {
                            com.celestia.addons.setting.ModeSetting mode = (com.celestia.addons.setting.ModeSetting) setting;
                            String text = mode.getName() + ": " + mode.getMode();
                            context.drawTextWithShadow(tr, text, setX + 2, currentY + 3, 0xFFFFFFFF);
                        } else if (setting instanceof com.celestia.addons.setting.ColorSetting) {
                            com.celestia.addons.setting.ColorSetting colorSet = (com.celestia.addons.setting.ColorSetting) setting;
                            context.drawTextWithShadow(tr, colorSet.getName(), setX + 2, currentY + 3, 0xFFFFFFFF);

                            // Color Preview Box
                            int previewSize = 10;
                            int previewX = setX + setWidth - previewSize - 2;
                            int previewY = currentY + 2;
                            RenderUtils.fill(context, previewX, previewY, previewX + previewSize,
                                    previewY + previewSize, colorSet.getRGB());

                            currentY += itemHeight;

                            if (colorSet.isExpanded()) {
                                // R Slider
                                drawSlider(context, tr, setX, currentY, setWidth, "R", colorSet.getRed(), 0xFFFF0000);
                                currentY += itemHeight;
                                // G Slider
                                drawSlider(context, tr, setX, currentY, setWidth, "G", colorSet.getGreen(), 0xFF00FF00);
                                currentY += itemHeight;
                                // B Slider
                                drawSlider(context, tr, setX, currentY, setWidth, "B", colorSet.getBlue(), 0xFF0000FF);
                                currentY += itemHeight;
                                continue;
                            } else {
                                continue; // Skip generic inc if expanded logic handled
                            }
                        }
                        currentY += itemHeight;
                    }
                }
            }
        }
    }

    private void drawSlider(DrawContext context, TextRenderer tr, int x, int y, int width, String label, int value,
            int color) {
        RenderUtils.fill(context, x, y, x + width, y + itemHeight, 0x40000000); // Track
        int sliderPos = (int) ((value / 255.0f) * width);
        RenderUtils.fill(context, x, y, x + sliderPos, y + itemHeight, color); // Bar
        context.drawTextWithShadow(tr, label + ": " + value, x + 2, y + 3, 0xFFFFFFFF);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, x, y, width, headerHeight)) {
            if (button == 0) {
                dragging = true;
                dragX = (int) mouseX - x;
                dragY = (int) mouseY - y;
                return true;
            } else if (button == 1) {
                open = !open;
                updateHeight();
                playSound();
                return true;
            }
        } else if (open && isHovered(mouseX, mouseY, x, y + headerHeight, width, height - headerHeight)) {
            int currentY = y + headerHeight + 2;
            for (Feature feature : features) {
                // Check click on feature item
                if (isHovered(mouseX, mouseY, x, currentY, width, itemHeight)) {
                    if (button == 0) { // Left Click -> Toggle
                        feature.toggle();
                        playSound();
                        return true;
                    } else if (button == 1) { // Right Click -> Expand
                        feature.setExpanded(!feature.isExpanded());
                        updateHeight();
                        playSound();
                        return true;
                    }
                }
                currentY += itemHeight;

                // Check click on settings if expanded
                if (feature.isExpanded()) {
                    // Check if clicked the keybind area
                    if (isHovered(mouseX, mouseY, x, currentY, width, itemHeight)) {
                        // Logic for binding key would go here (e.g. set listening state)
                        // For now just valid consume
                        return true;
                    }
                    currentY += itemHeight;

                    // Settings Interaction
                    for (com.celestia.addons.setting.Setting setting : feature.getSettings()) {
                        if (!setting.isVisible())
                            continue;

                        if (setting instanceof com.celestia.addons.setting.ModeSetting) {
                            if (isHovered(mouseX, mouseY, x + 5, currentY, width - 10, itemHeight)) {
                                ((com.celestia.addons.setting.ModeSetting) setting).cycle();
                                playSound();
                                return true;
                            }
                            currentY += itemHeight;
                        } else if (setting instanceof com.celestia.addons.setting.ColorSetting) {
                            com.celestia.addons.setting.ColorSetting colorSet = (com.celestia.addons.setting.ColorSetting) setting;
                            if (isHovered(mouseX, mouseY, x + 5, currentY, width - 10, itemHeight)) {
                                if (button == 1) { // Open pickers
                                    colorSet.setExpanded(!colorSet.isExpanded());
                                    updateHeight();
                                    playSound();
                                    return true;
                                }
                            }
                            currentY += itemHeight;

                            if (colorSet.isExpanded()) {
                                int setX = x + 5;
                                int setWidth = width - 10;
                                // Red
                                if (isHovered(mouseX, mouseY, setX, currentY, setWidth, itemHeight)) {
                                    handleSlider(mouseX, setX, setWidth, val -> colorSet.setRed(val));
                                    return true;
                                }
                                currentY += itemHeight;
                                // Green
                                if (isHovered(mouseX, mouseY, setX, currentY, setWidth, itemHeight)) {
                                    handleSlider(mouseX, setX, setWidth, val -> colorSet.setGreen(val));
                                    return true;
                                }
                                currentY += itemHeight;
                                // Blue
                                if (isHovered(mouseX, mouseY, setX, currentY, setWidth, itemHeight)) {
                                    handleSlider(mouseX, setX, setWidth, val -> colorSet.setBlue(val));
                                    return true;
                                }
                                currentY += itemHeight;
                                continue;
                            } else {
                                continue;
                            }
                        }
                        currentY += itemHeight;
                    }
                }
            }
        }
        return false;
    }

    private void handleSlider(double mouseX, int x, int width, java.util.function.IntConsumer setter) {
        double rel = mouseX - x;
        int val = (int) ((rel / width) * 255);
        val = Math.max(0, Math.min(255, val));
        setter.accept(val);
        // No sound for sliders, too annoying
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return false;
    }

    private boolean isHovered(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void playSound() {
        MinecraftClient.getInstance().getSoundManager()
                .play(net.minecraft.client.sound.PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
