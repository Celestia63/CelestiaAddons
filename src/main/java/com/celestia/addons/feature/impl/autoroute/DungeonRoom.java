package com.celestia.addons.feature.impl.autoroute;

import java.util.List;

public class DungeonRoom {
    public int core;
    public int offset;
    public int block_count;
    public List<RoomBlock> blocks;

    public static class RoomBlock {
        public Pos pos;
        public Data data;

        public static class Pos {
            public int x;
            public int y;
            public int z;
        }

        public static class Data {
            public int block;
            public int meta;
        }

        public Waypoint.ActionType getActionType() {
            if (this.data == null) return Waypoint.ActionType.WALK;
            switch(this.data.block) {
                case 152: return Waypoint.ActionType.ETHERWARP;
                case 46: return Waypoint.ActionType.SUPERBOOM;
                case 69: return Waypoint.ActionType.INTERACT;
                case 179:
                default: return Waypoint.ActionType.WALK;
            }
        }

        public boolean isRouteWaypoint() {
            if (this.data == null) return false;
            int b = this.data.block;
            return b == 179 || b == 152 || b == 46 || b == 69;
        }
    }
}
