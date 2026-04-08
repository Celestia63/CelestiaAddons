package com.celestia.addons.feature.impl;

import com.celestia.addons.feature.Feature;
import com.celestia.addons.setting.NumberSetting;

public class FullbrightFeature extends Feature {

    public final NumberSetting gamma = new NumberSetting("Gamma", 1.0, 0.0, 5.0, 0.1);
    private double originalGamma = 1.0;

    public FullbrightFeature() {
        super("Fullbright", "Render", false);
        addSetting(gamma);

        // Register tick event to maintain gamma
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (isEnabled()) {
                mc.options.getGamma().setValue(gamma.getValue());
            }
        });
    }

    @Override
    protected void onEnable() {
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        originalGamma = mc.options.getGamma().getValue();
        mc.options.getGamma().setValue(gamma.getValue());
        System.out.println("Fullbright Enabled - Gamma: " + gamma.getValue());
    }

    @Override
    protected void onDisable() {
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        mc.options.getGamma().setValue(originalGamma);
        System.out.println("Fullbright Disabled - Restored Gamma: " + originalGamma);
    }
}