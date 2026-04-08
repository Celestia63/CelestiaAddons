package com.celestia.addons.feature.impl;

import com.celestia.addons.feature.Feature;
import com.celestia.addons.feature.impl.autoroute.*;
import com.celestia.addons.setting.ModeSetting;
import com.celestia.addons.setting.NumberSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class AutoRouteFeature extends Feature {
    private final RoutePlayer player = new RoutePlayer();
    private final RouteRenderer renderer = new RouteRenderer();
    public final RouteRecorder recorder = new RouteRecorder();
    private Route currentRoute = null;

    public final NumberSetting speed = new NumberSetting("Packets / Tick", 5, 1, 20, 1.0);

    public AutoRouteFeature() {
        super("AutoRoute", "General", false);
        addSetting(speed);
        addSetting(new ModeSetting("Movement", "Safe", new String[]{"Safe", "Fast"}));
        addSetting(new ModeSetting("Visuals", "On", new String[]{"On", "Off"}));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (isEnabled()) {
                player.onTick();
            }
        });
    }

    public void scanAndLoadRoom(int forcedCore) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        double originX = Math.floor(mc.player.getX() / 32.0) * 32.0;
        double originZ = Math.floor(mc.player.getZ() / 32.0) * 32.0;

        Route baseRoute = DungeonRoomManager.getInstance().getRouteByCore(forcedCore);
        if (baseRoute == null) {
            System.out.println("No matching route found.");
            return;
        }

        List<Waypoint> wps = new ArrayList<>();
        for (Waypoint wp : baseRoute.getWaypoints()) {
            wps.add(wp.copyOffset(originX, originZ));
        }

        currentRoute = new Route(wps);
        renderer.setActiveRoute(currentRoute);
        renderer.setVisualsEnabled(isVisualsEnabled());
        
        // Auto-enable DungeonBreaker if route has dungeon breaker waypoints
        if (currentRoute.hasDungeonBreaker()) {
            enableDungeonBreaker();
        }
        
        System.out.println("Room scanned, loaded " + wps.size() + " waypoints.");
    }

    public void playRoute() {
        if (currentRoute != null) {
            boolean safeMode = isSafeModeEnabled();
            int packetCount = safeMode ? Math.max(1, Math.min(10, (int) speed.getValue())) : Math.max(1, Math.min(50, (int) speed.getValue()));
            player.startPlaying(currentRoute, packetCount, safeMode, () -> {
                System.out.println("Route finished!");
            });
            renderer.setVisualsEnabled(isVisualsEnabled());
            renderer.setRoutePlayer(player);
        }
    }

    public void stopRoute() {
        player.stop();
    }

    public boolean isSafeModeEnabled() {
        for (com.celestia.addons.setting.Setting setting : getSettings()) {
            if (setting instanceof ModeSetting && setting.getName().equals("Movement")) {
                return ((ModeSetting) setting).is("Safe");
            }
        }
        return true;
    }

    public boolean isVisualsEnabled() {
        for (com.celestia.addons.setting.Setting setting : getSettings()) {
            if (setting instanceof ModeSetting && setting.getName().equals("Visuals")) {
                return ((ModeSetting) setting).is("On");
            }
        }
        return true;
    }

    private void enableDungeonBreaker() {
        // Find and enable the DungeonBreaker feature
        for (com.celestia.addons.feature.Feature feature : com.celestia.addons.feature.FeatureManager.getInstance().getFeatures()) {
            if (feature.getName().equals("DungeonBreaker")) {
                if (!feature.isEnabled()) {
                    feature.setEnabled(true);
                    System.out.println("AutoRoute: DungeonBreaker feature auto-enabled for dungeon breaker route");
                }
                break;
            }
        }
    }

    @Override
    protected void onEnable() {
        // Nothing special, tick listener handles it
    }

    @Override
    protected void onDisable() {
        player.stop();
        renderer.clear();
    }
}
