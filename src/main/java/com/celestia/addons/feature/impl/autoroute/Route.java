package com.celestia.addons.feature.impl.autoroute;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private final List<Waypoint> waypoints = new ArrayList<>();
    private boolean hasDungeonBreaker = false;

    public Route(List<Waypoint> waypoints) {
        this.waypoints.addAll(waypoints);
        // Check if any waypoint has dungeon breaker flag
        for (Waypoint wp : waypoints) {
            if (wp.dungeonBreaker) {
                this.hasDungeonBreaker = true;
                break;
            }
        }
    }

    public int size() {
        return waypoints.size();
    }

    public Waypoint get(int index) {
        return waypoints.get(index);
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public boolean hasDungeonBreaker() {
        return hasDungeonBreaker;
    }
}
