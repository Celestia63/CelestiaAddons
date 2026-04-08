package com.celestia.addons.feature.impl.autoroute;

import com.celestia.addons.util.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Vec3d;

public class RouteRenderer {
    private Route currentRoute;
    private RoutePlayer routePlayer;
    private boolean active;
    private boolean showVisuals = true;
    private final int[] colors = {
        0x00FF00, // Green - WALK
        0xFF00FF, // Magenta - ETHERWARP
        0xFF0000, // Red - SUPERBOOM
        0x00FFFF, // Cyan - INTERACT/USE/ROTATE/etc
    };

    public RouteRenderer() {
        // Register world render event for 1.21.10 compatibility
        WorldRenderEvents.AFTER_ENTITIES.register(context -> this.renderRoute(context));
    }

    public void setActiveRoute(Route route) {
        this.currentRoute = route;
        this.active = (route != null);
    }

    public void setRoutePlayer(RoutePlayer routePlayer) {
        this.routePlayer = routePlayer;
    }

    public void setVisualsEnabled(boolean enabled) {
        this.showVisuals = enabled;
    }

    public void clear() {
        this.currentRoute = null;
        this.active = false;
        this.routePlayer = null;
    }

    private void renderRoute(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
        if (!active || currentRoute == null || !showVisuals) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        VertexConsumer consumer = context.consumers().getBuffer(RenderLayer.getLines());

        for (int i = 0; i < currentRoute.size(); i++) {
            Waypoint wp = currentRoute.get(i);
            int color = getColorForAction(wp.action);
            double x = wp.x - cameraPos.x;
            double y = wp.y - cameraPos.y;
            double z = wp.z - cameraPos.z;
            RenderUtils.draw3DBox(context.matrixStack().peek().getPositionMatrix(), 
                                consumer, x - 0.25, y, z - 0.25, 0.5f, 0.5f, color);
        }

        // Draw route lines between waypoints
        for (int i = 1; i < currentRoute.size(); i++) {
            Waypoint previous = currentRoute.get(i - 1);
            Waypoint current = currentRoute.get(i);
            int lineColor = getColorForAction(current.action);
            double x1 = previous.x - cameraPos.x;
            double y1 = previous.y - cameraPos.y + 0.15;
            double z1 = previous.z - cameraPos.z;
            double x2 = current.x - cameraPos.x;
            double y2 = current.y - cameraPos.y + 0.15;
            double z2 = current.z - cameraPos.z;
            RenderUtils.draw3DLine(context.matrixStack().peek().getPositionMatrix(), consumer, x1, y1, z1, x2, y2, z2, lineColor);
        }

        // Draw start/end markers if available
        if (currentRoute.size() > 0) {
            Waypoint start = currentRoute.get(0);
            Waypoint end = currentRoute.get(currentRoute.size() - 1);
            double startX = start.x - cameraPos.x;
            double startY = start.y - cameraPos.y;
            double startZ = start.z - cameraPos.z;
            double endX = end.x - cameraPos.x;
            double endY = end.y - cameraPos.y;
            double endZ = end.z - cameraPos.z;
            RenderUtils.draw3DBox(context.matrixStack().peek().getPositionMatrix(), consumer, startX - 0.35, startY, startZ - 0.35, 0.7f, 0.7f, 0xFF00FF00);
            RenderUtils.draw3DBox(context.matrixStack().peek().getPositionMatrix(), consumer, endX - 0.35, endY, endZ - 0.35, 0.7f, 0.7f, 0xFFFF0000);
        }
    }

    private int getColorForAction(Waypoint.ActionType action) {
        switch(action) {
            case WALK:
            case START_WALK:
                return 0xFF00FF00; // Green with alpha
            case ETHERWARP:
                return 0xFFFF00FF; // Magenta with alpha
            case SUPERBOOM:
                return 0xFFFF0000; // Red with alpha
            case INTERACT:
            case USE:
            case AWAIT_SECRET:
            case PEARL_CLIP:
            case ROTATE:
            case BAT_SPAWN:
                return 0xFF00FFFF; // Cyan with alpha
            default:
                return 0xFF00FF00;
        }
    }
}
