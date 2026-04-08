package com.celestia.addons.feature.impl.autoroute;

import com.celestia.addons.feature.Feature;
import com.celestia.addons.setting.ModeSetting;
import com.celestia.addons.setting.NumberSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class AutoRouteFeature extends Feature {
    private final RouteRecorder recorder = new RouteRecorder();
    private final RoutePlayer player = new RoutePlayer();
    private final RouteRenderer renderer = new RouteRenderer();
    private Route currentRoute;
    private int recordingMode = 0; // 0 = off, 1 = recording, 2 = playback

    public AutoRouteFeature() {
        super("AutoRoute", "Skyblock", false);
        
        // Settings
        addSetting(new ModeSetting("Mode", "Recording", new String[]{"Recording", "Playback"}));
        addSetting(new NumberSetting("Speed", 1.0, 0.1, 10.0, 0.1));
        addSetting(new ModeSetting("Movement", "Safe", new String[]{"Safe", "Fast"}));
        addSetting(new ModeSetting("Visuals", "On", new String[]{"On", "Off"}));
        
        // Register tick event for autoroute functionality
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient mc) {
        if (!isEnabled()) return;
        
        // Handle route playback
        if (player.isPlaying()) {
            player.onTick();
        }
        
        // Handle recording (can be triggered via commands or keybinds)
        if (recorder.isRecording()) {
            // Recorder is active, waiting for waypoint additions
        }
    }

    public void startRecording() {
        if (recorder.isRecording()) {
            return; // Already recording
        }
        recorder.startRecording();
        renderer.clear();
        System.out.println("AutoRoute: Recording started");
    }

    public void addWaypoint(Waypoint.ActionType type) {
        if (!recorder.isRecording()) {
            System.out.println("AutoRoute: Not recording");
            return;
        }
        recorder.addWaypoint(type);
        System.out.println("AutoRoute: Waypoint added (" + type.name() + ")");
    }

    public void stopRecording() {
        if (!recorder.isRecording()) {
            return;
        }
        Route route = recorder.stopAndGetRoute();
        if (route != null && route.size() > 0) {
            currentRoute = route;
            renderer.setActiveRoute(route);
            System.out.println("AutoRoute: Recording stopped. Route has " + route.size() + " waypoints");
        } else {
            System.out.println("AutoRoute: Recording stopped but route is empty");
            renderer.clear();
        }
    }

    public void playRoute() {
        if (currentRoute == null) {
            System.out.println("AutoRoute: No route to play");
            return;
        }
        
        if (player.isPlaying()) {
            System.out.println("AutoRoute: Already playing a route");
            return;
        }
        
        // Get speed setting (if exists)
        Double speed = 1.0;
        for (com.celestia.addons.setting.Setting setting : getSettings()) {
            if (setting instanceof NumberSetting && setting.getName().equals("Speed")) {
                speed = ((NumberSetting) setting).getValue();
            }
        }
        
        boolean safeMode = isSafeModeEnabled();
        int packetsPerTick = safeMode ? Math.max(1, Math.min(10, (int) Math.round(2 * speed))) : Math.max(1, Math.min(50, (int) Math.round(8 * speed)));
        player.startPlaying(currentRoute, packetsPerTick, safeMode, () -> {
            System.out.println("AutoRoute: Route playback completed");
        });
        renderer.setVisualsEnabled(isVisualsEnabled());
        renderer.setRoutePlayer(player);
        
        System.out.println("AutoRoute: Playing route with " + currentRoute.size() + " waypoints");
    }

    public void stopPlayback() {
        if (player.isPlaying()) {
            player.stop();
            System.out.println("AutoRoute: Playback stopped");
        }
    }

    public RouteRecorder getRecorder() {
        return recorder;
    }

    public RoutePlayer getPlayer() {
        return player;
    }

    public RouteRenderer getRenderer() {
        return renderer;
    }

    public boolean isPlaying() {
        return player.isPlaying();
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

    public Route getCurrentRoute() {
        return currentRoute;
    }

    public void setCurrentRoute(Route route) {
        this.currentRoute = route;
        if (route != null) {
            renderer.setActiveRoute(route);
            renderer.setVisualsEnabled(isVisualsEnabled());
        } else {
            renderer.clear();
        }
    }

    @Override
    protected void onEnable() {
        System.out.println("AutoRoute Feature enabled");
    }

    @Override
    protected void onDisable() {
        stopRecording();
        stopPlayback();
        renderer.clear();
        System.out.println("AutoRoute Feature disabled");
    }
}
