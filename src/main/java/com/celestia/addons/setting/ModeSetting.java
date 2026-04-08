package com.celestia.addons.setting;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ModeSetting extends Setting {
    private String currentMode;
    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name);
        this.modes = Arrays.asList(modes);
        this.index = this.modes.indexOf(defaultMode);
        this.currentMode = defaultMode;
        if (index == -1) {
            index = 0;
            currentMode = this.modes.get(0);
        }
    }

    public String getMode() {
        return currentMode;
    }

    public void cycle() {
        index++;
        if (index >= modes.size()) {
            index = 0;
        }
        currentMode = modes.get(index);
    }

    public boolean is(String mode) {
        return currentMode.equalsIgnoreCase(mode);
    }
}
