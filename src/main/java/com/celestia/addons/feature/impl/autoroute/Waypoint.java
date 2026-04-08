package com.celestia.addons.feature.impl.autoroute;

public class Waypoint {
    public enum ActionType {
        WALK, ETHERWARP, SUPERBOOM, INTERACT,
        // AK47 specifics
        PEARL_CLIP, AWAIT_SECRET, START_WALK, ROTATE, BAT_SPAWN, USE
    }

    public final double x, y, z;
    public final float yaw, pitch;
    public final ActionType action;

    // AK47 Metadata
    public boolean stopMotion = false;
    public boolean center = false;
    public double distance = 0.0;
    public String item = null;
    public boolean leftClick = false;
    public boolean chain = false;
    public boolean dungeonBreaker = false;

    public Waypoint(double x, double y, double z, float yaw, float pitch, ActionType action) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.action = action != null ? action : ActionType.WALK;
    }

    public Waypoint copyOffset(double originX, double originZ) {
        Waypoint wp = new Waypoint(this.x + originX, this.y, this.z + originZ, this.yaw, this.pitch, this.action);
        wp.stopMotion = this.stopMotion;
        wp.center = this.center;
        wp.distance = this.distance;
        wp.item = this.item;
        wp.leftClick = this.leftClick;
        wp.chain = this.chain;
        wp.dungeonBreaker = this.dungeonBreaker;
        return wp;
    }
}
