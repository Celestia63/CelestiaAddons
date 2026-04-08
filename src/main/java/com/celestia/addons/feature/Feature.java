package com.celestia.addons.feature;

import com.celestia.addons.setting.Setting;
import java.util.ArrayList;
import java.util.List;

public class Feature {
    private final String name;
    private final String category;
    private boolean enabled;
    private int keybind;
    private boolean expanded;
    private final List<Setting> settings = new ArrayList<>();

    public Feature(String name, String category, boolean enabledDefault) {
        this.name = name;
        this.category = category;
        this.enabled = enabledDefault;
        this.keybind = -1; // No keybind by default
        this.expanded = false;
    }

    public void addSetting(Setting setting) {
        this.settings.add(setting);
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
        com.celestia.addons.config.ConfigManager.save();
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public int getKeybind() {
        return keybind;
    }

    public void setKeybind(int keybind) {
        this.keybind = keybind;
        com.celestia.addons.config.ConfigManager.save();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    protected void onEnable() {
        System.out.println(name + " enabled!");
    }

    protected void onDisable() {
        System.out.println(name + " disabled!");
    }
}
