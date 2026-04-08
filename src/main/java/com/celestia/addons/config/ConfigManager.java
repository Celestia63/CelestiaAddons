package com.celestia.addons.config;

import com.celestia.addons.feature.Feature;
import com.celestia.addons.feature.FeatureManager;
import com.celestia.addons.setting.ColorSetting;
import com.celestia.addons.setting.ModeSetting;
import com.celestia.addons.setting.NumberSetting;
import com.celestia.addons.setting.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;

    public static void init() {
        Path configPath = MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve("celestiaaddons_config.json");
        configFile = configPath.toFile();
    }

    public static void save() {
        if (configFile == null) init();

        JsonObject json = new JsonObject();
        for (Feature feature : FeatureManager.getInstance().getFeatures()) {
            JsonObject featureJson = new JsonObject();
            featureJson.addProperty("enabled", feature.isEnabled());
            featureJson.addProperty("keybind", feature.getKeybind());

            JsonObject settingsJson = new JsonObject();
            for (Setting setting : feature.getSettings()) {
                if (setting instanceof NumberSetting) {
                    settingsJson.addProperty(setting.getName(), ((NumberSetting) setting).getValue());
                } else if (setting instanceof ModeSetting) {
                    settingsJson.addProperty(setting.getName(), ((ModeSetting) setting).getMode());
                } else if (setting instanceof ColorSetting) {
                    settingsJson.addProperty(setting.getName(), ((ColorSetting) setting).getRGB());
                }
            }
            featureJson.add("settings", settingsJson);
            json.add(feature.getName(), featureJson);
        }

        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(json, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (configFile == null) init();
        if (!configFile.exists()) return;

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (Feature feature : FeatureManager.getInstance().getFeatures()) {
                if (json.has(feature.getName())) {
                    JsonObject featureJson = json.getAsJsonObject(feature.getName());
                    
                    if (featureJson.has("enabled")) {
                        feature.setEnabled(featureJson.get("enabled").getAsBoolean());
                    }
                    if (featureJson.has("keybind")) {
                        feature.setKeybind(featureJson.get("keybind").getAsInt());
                    }

                    if (featureJson.has("settings")) {
                        JsonObject settingsJson = featureJson.getAsJsonObject("settings");
                        for (Setting setting : feature.getSettings()) {
                            if (settingsJson.has(setting.getName())) {
                                if (setting instanceof NumberSetting) {
                                    ((NumberSetting) setting).setValue(settingsJson.get(setting.getName()).getAsDouble());
                                } else if (setting instanceof ModeSetting) {
                                    String mode = settingsJson.get(setting.getName()).getAsString();
                                    ModeSetting ms = (ModeSetting) setting;
                                    // Cycle until we hit the loaded mode or stay at default if not found
                                    for (int i = 0; i < 20; i++) { // Max modes safety
                                        if (ms.getMode().equalsIgnoreCase(mode)) break;
                                        ms.cycle();
                                    }
                                } else if (setting instanceof ColorSetting) {
                                    ((ColorSetting) setting).setColor(settingsJson.get(setting.getName()).getAsInt());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
