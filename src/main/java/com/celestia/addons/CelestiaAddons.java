package com.celestia.addons;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celestia.addons.feature.FeatureManager;
import com.celestia.addons.config.ConfigManager;

public class CelestiaAddons implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("celestiaaddons");
    private static FeatureManager featureManager;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing CelestiaAddons Common");
        featureManager = FeatureManager.getInstance();
        ConfigManager.load();
    }

    public static FeatureManager getFeatureManager() {
        return featureManager;
    }
}
