package com.celestia.addons.feature.impl;

import com.celestia.addons.feature.Feature;
import com.celestia.addons.setting.NumberSetting;

public class SpeedFeature extends Feature {

    public final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1);

    public SpeedFeature() {
        super("Speed", "Movement", false);
        addSetting(speed);

        // Register tick event for speed modification
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!isEnabled() || mc.player == null) return;

            // Apply speed multiplier to player movement
            double multiplier = speed.getValue();
            if (multiplier > 1.0) {
                net.minecraft.util.math.Vec3d velocity = mc.player.getVelocity();
                mc.player.setVelocity(velocity.x * multiplier, velocity.y, velocity.z * multiplier);
            }
        });
    }

    @Override
    protected void onEnable() {
        System.out.println("Speed Enabled - Multiplier: " + speed.getValue());
    }

    @Override
    protected void onDisable() {
        System.out.println("Speed Disabled");
    }
}