package com.celestia.addons.feature.impl.autoroute;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DungeonRoomManager {
    private static final DungeonRoomManager INSTANCE = new DungeonRoomManager();
    private static final Gson GSON = new Gson();

    private final Map<Integer, String> coreToName = new HashMap<>();
    private final Map<String, Route> routesByName = new HashMap<>();

    public static DungeonRoomManager getInstance() {
        return INSTANCE;
    }

    public static class RoomMeta {
        public String name;
        public List<Integer> cores;
    }

    public static class NodeData {
        public double yaw, pitch;
        public boolean stopMotion, center;
        public String distance, item;
        public boolean leftClick;
        public boolean dungeonBreaker;
    }

    public static class RouteNode {
        public String type;
        public NodeData data;
        public double x, y, z;
        public String id;
        public boolean chain;
    }

    public void loadRoomData(Path file) {
        if (!Files.exists(file)) return;
        try (FileReader reader = new FileReader(file.toFile())) {
            Type listType = new TypeToken<List<RoomMeta>>() {}.getType();
            List<RoomMeta> metaList = GSON.fromJson(reader, listType);
            if (metaList != null) {
                for (RoomMeta meta : metaList) {
                    if (meta.cores != null) {
                        for (int core : meta.cores) {
                            coreToName.put(core, meta.name);
                        }
                    }
                }
                System.out.println("[CelestiaAddons] Loaded " + coreToName.size() + " Core -> Name mappings.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadRoutes(Path file) {
        if (!Files.exists(file)) return;
        try (FileReader reader = new FileReader(file.toFile())) {
            Type mapType = new TypeToken<Map<String, List<RouteNode>>>() {}.getType();
            Map<String, List<RouteNode>> routeMap = GSON.fromJson(reader, mapType);
            if (routeMap == null) return;

            for (Map.Entry<String, List<RouteNode>> entry : routeMap.entrySet()) {
                String roomName = entry.getKey();
                List<Waypoint> wps = new ArrayList<>();
                for (RouteNode node : entry.getValue()) {
                    Waypoint.ActionType t;
                    switch (node.type.toLowerCase()) {
                        case "ether": t = Waypoint.ActionType.ETHERWARP; break;
                        case "awaitsecret": t = Waypoint.ActionType.AWAIT_SECRET; break;
                        case "pearlclip": t = Waypoint.ActionType.PEARL_CLIP; break;
                        case "use": t = Waypoint.ActionType.USE; break;
                        case "startwalk": t = Waypoint.ActionType.START_WALK; break;
                        case "rotate": t = Waypoint.ActionType.ROTATE; break;
                        case "batspawn": t = Waypoint.ActionType.BAT_SPAWN; break;
                        default: t = Waypoint.ActionType.WALK; break;
                    }
                    float yaw = node.data != null ? (float) node.data.yaw : 0;
                    float pitch = node.data != null ? (float) node.data.pitch : 0;
                    Waypoint wp = new Waypoint(node.x, node.y, node.z, yaw, pitch, t);
                    
                    if (node.data != null) {
                        wp.stopMotion = node.data.stopMotion;
                        wp.center = node.data.center;
                        if (node.data.distance != null) {
                            try { wp.distance = Double.parseDouble(node.data.distance); } catch (Exception ignored) {}
                        }
                        wp.item = node.data.item;
                        wp.leftClick = node.data.leftClick;
                        wp.dungeonBreaker = node.data.dungeonBreaker;
                    }
                    wp.chain = node.chain;
                    wps.add(wp);
                }
                routesByName.put(roomName, new Route(wps));
            }
            System.out.println("[CelestiaAddons] Loaded " + routesByName.size() + " generic routes from " + file.getFileName());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public Route getRouteByCore(int core) {
        String name = coreToName.get(core);
        if (name != null) {
            return routesByName.get(name);
        }
        return null; // No route for this room
    }

    public void saveRecordedRoute(Path file, int core, int offset, List<Waypoint> absoluteWaypoints, double originX, double originZ) {
        String roomName = coreToName.getOrDefault(core, "CustomRoom_" + core);
        List<RouteNode> nodes = new ArrayList<>();

        for (Waypoint wp : absoluteWaypoints) {
            RouteNode node = new RouteNode();
            node.id = UUID.randomUUID().toString().replace("-", "").substring(0, 22); // Auto-generate ID
            node.x = wp.x - originX;
            node.y = wp.y;
            node.z = wp.z - originZ;
            node.chain = wp.chain;

            node.data = new NodeData();
            node.data.yaw = wp.yaw;
            node.data.pitch = wp.pitch;
            node.data.stopMotion = wp.stopMotion;
            node.data.center = wp.center;
            node.data.dungeonBreaker = wp.dungeonBreaker;

            switch (wp.action) {
                case ETHERWARP: node.type = "ether"; break;
                case AWAIT_SECRET: node.type = "awaitsecret"; break;
                case USE: node.type = "use"; break;
                case PEARL_CLIP: 
                    node.type = "pearlclip"; 
                    node.data.distance = String.valueOf((int) wp.distance); 
                    break;
                case START_WALK: node.type = "startwalk"; break;
                case ROTATE: node.type = "rotate"; break;
                case BAT_SPAWN: node.type = "batspawn"; break;
                default: node.type = "walk"; break;
            }
            nodes.add(node);
        }

        Map<String, List<RouteNode>> output = new HashMap<>();
        output.put(roomName, nodes);

        try (FileWriter writer = new FileWriter(file.toFile())) {
            GSON.toJson(output, writer);
            // Optionally auto-register it locally
            routesByName.put(roomName, new Route(new ArrayList<>(absoluteWaypoints)));
            System.out.println("[CelestiaAddons] Saved custom route to " + file.getFileName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
