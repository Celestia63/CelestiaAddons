package com.celestia.addons.gui;

import com.celestia.addons.feature.Feature;
import com.celestia.addons.feature.FeatureManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CelestiaScreen extends Screen {
    private final List<Panel> panels = new ArrayList<>();
    private final FeatureManager featureManager;

    public CelestiaScreen(FeatureManager featureManager) {
        super(Text.of("CelestiaAddons"));
        this.featureManager = featureManager;
    }

    public CelestiaScreen(Text title) {
        super(title);
        this.featureManager = FeatureManager.getInstance();
    }

    @Override
    protected void init() {
        super.init();
        if (panels.isEmpty()) {
            int x = 20;
            int y = 20;
            Map<String, List<Feature>> categories = FeatureManager.getInstance().getFeaturesByCategories();

            for (Map.Entry<String, List<Feature>> entry : categories.entrySet()) {
                panels.add(new Panel(entry.getKey(), x, y, entry.getValue()));
                x += 120; // Spacing
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        // Draw background tint is removed as per previous fix

        // Headline
        Text title = Text.of("CelestiaAddons");
        int titleWidth = this.textRenderer.getWidth(title);
        int centerX = this.width / 2;

        context.drawTextWithShadow(this.textRenderer, title, centerX - (titleWidth / 2), 10, 0xFFA020F0); // Purple
                                                                                                          // color

        for (Panel panel : panels) {
            panel.render(context, mouseX, mouseY, delta);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (Panel panel : panels) {
            // Simple bounds check instead of isMouseOver
            if (mouseX >= panel.getX() && mouseX <= panel.getX() + panel.getWidth() &&
                mouseY >= panel.getY() && mouseY <= panel.getY() + panel.getHeight()) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) {
            if (panel.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public void removed() {
        com.celestia.addons.config.ConfigManager.save();
        super.removed();
    }
}
