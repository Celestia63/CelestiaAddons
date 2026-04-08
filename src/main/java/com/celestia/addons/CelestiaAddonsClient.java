package com.celestia.addons;

import com.celestia.addons.command.CelestiaCommands;
import com.celestia.addons.feature.FeatureManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class CelestiaAddonsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Load configuration first
        com.celestia.addons.config.ConfigManager.load();
        
        // The FeatureManager is initialized with features automatically
        // Features are registered in FeatureManager constructor
        System.out.println("[CelestiaAddons] Loaded " + FeatureManager.getInstance().getFeatures().size() + " features");
        
        // Register commands
        CelestiaCommands.register();
        
        // Register client tick event for any global mod-level tick handling
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        
        System.out.println("[CelestiaAddons] Client mod initialized successfully");
    }
    
    private void onClientTick(MinecraftClient mc) {
        // Individual features handle their own tick logic via ClientTickEvents
        // This is just for any global mod-level tick handling if needed
    }
}
