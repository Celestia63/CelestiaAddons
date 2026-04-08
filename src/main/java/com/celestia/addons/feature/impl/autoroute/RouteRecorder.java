package com.celestia.addons.feature.impl.autoroute;

import net.minecraft.client.MinecraftClient;
import java.util.ArrayList;
import java.util.List;

public class RouteRecorder {
    private final List<Waypoint> recordingWaypoints = new ArrayList<>();
    private boolean recording = false;
    private double originX = 0;
    private double originZ = 0;

    public void startRecording() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        
        recordingWaypoints.clear();
        recording = true;
        
        // Calculate room origin based on current position
        originX = Math.floor(mc.player.getX() / 32.0) * 32.0;
        originZ = Math.floor(mc.player.getZ() / 32.0) * 32.0;
    }

    public void addWaypoint(Waypoint.ActionType type) {
        if (!recording) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        recordingWaypoints.add(new Waypoint(
            mc.player.getX(),
            mc.player.getY(),
            mc.player.getZ(),
            mc.player.getYaw(),
            mc.player.getPitch(),
            type
        ));
    }

    public Route stopAndGetRoute() {
        if (!recording) return null;
        recording = false;
        return new Route(new ArrayList<>(recordingWaypoints));
    }

    public boolean isRecording() {
        return recording;
    }

    public double getOriginX() { return originX; }
    public double getOriginZ() { return originZ; }
    public List<Waypoint> getRecordingWaypoints() { return recordingWaypoints; }
}
