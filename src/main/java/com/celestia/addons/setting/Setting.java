package com.celestia.addons.setting;

import java.util.function.Supplier;

public abstract class Setting {
    private final String name;
    private final Supplier<Boolean> visible;

    public Setting(String name, Supplier<Boolean> visible) {
        this.name = name;
        this.visible = visible;
    }

    public Setting(String name) {
        this(name, () -> true);
    }

    public String getName() {
        return name;
    }

    public boolean isVisible() {
        return visible.get();
    }
}
