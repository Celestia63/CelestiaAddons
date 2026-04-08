package com.celestia.addons.feature.impl;

import com.celestia.addons.feature.Feature;
import com.celestia.addons.setting.BooleanSetting;
import com.celestia.addons.setting.ColorSetting;
import com.celestia.addons.setting.ModeSetting;
import java.awt.Color;

public class PlayerEspFeature extends Feature {

    public final ModeSetting mode = new ModeSetting("Mode", "3D Box", "3D Box", "2D Box", "Glowing", "Tracers");
    public final ColorSetting color = new ColorSetting("ESP Color", new Color(255, 0, 0)); // Red
    public final BooleanSetting showInvisible = new BooleanSetting("Show Invisible", false);
    public final BooleanSetting showFriends = new BooleanSetting("Show Friends", true);

    public PlayerEspFeature() {
        super("Player ESP", "Render", false);
        addSetting(mode);
        addSetting(color);
        addSetting(showInvisible);
        addSetting(showFriends);

        // Register Render Listener
        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!isEnabled())
                return;
            render(context);
        });
    }

    private void render(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
        if (net.minecraft.client.MinecraftClient.getInstance().world == null)
            return;

        net.minecraft.client.MinecraftClient.getInstance().world.getEntities().forEach(entity -> {
            if (entity instanceof net.minecraft.entity.player.PlayerEntity) {
                net.minecraft.entity.player.PlayerEntity player = (net.minecraft.entity.player.PlayerEntity) entity;

                // Skip self
                if (player == net.minecraft.client.MinecraftClient.getInstance().player)
                    return;

                // Skip invisible if not enabled
                if (player.isInvisible() && !showInvisible.getValue())
                    return;

                // Skip friends if disabled (would need friend list implementation)
                // For now, just render all

                drawEsp(context, player);
            }
        });
    }

    private void drawEsp(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context,
            net.minecraft.entity.player.PlayerEntity player) {
        net.minecraft.client.util.math.MatrixStack matrices = context.matrixStack();
        net.minecraft.util.math.Vec3d cameraPos = net.minecraft.client.MinecraftClient.getInstance().gameRenderer
                .getCamera().getPos();

        double x = player.getX() - cameraPos.x;
        double y = player.getY() - cameraPos.y;
        double z = player.getZ() - cameraPos.z;

        int argb = color.getRGB();

        if (mode.is("3D Box")) {
            net.minecraft.client.render.VertexConsumer consumer = context.consumers()
                    .getBuffer(net.minecraft.client.render.RenderLayer.getLines());
            com.celestia.addons.util.RenderUtils.draw3DBox(matrices.peek().getPositionMatrix(), consumer, x - 0.3, y, z - 0.3, 0.6f,
                    1.8f, argb);
        } else if (mode.is("2D Box")) {
            // 2D box would require screen space calculations
            // For now, fallback to 3D
            net.minecraft.client.render.VertexConsumer consumer = context.consumers()
                    .getBuffer(net.minecraft.client.render.RenderLayer.getLines());
            com.celestia.addons.util.RenderUtils.draw3DBox(matrices.peek().getPositionMatrix(), consumer, x - 0.3, y, z - 0.3, 0.6f,
                    1.8f, argb);
        } else if (mode.is("Tracers")) {
            // Draw line from screen center to player
            drawTracer(context, player);
        }
    }

    private void drawTracer(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context,
            net.minecraft.entity.player.PlayerEntity player) {
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        net.minecraft.client.util.math.MatrixStack matrices = context.matrixStack();

        // Get screen dimensions
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        // Calculate screen position of player
        net.minecraft.util.math.Vec3d playerPos = player.getPos();
        net.minecraft.util.math.Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        // Simple tracer: line from bottom center of screen to player position
        double tracerStartX = screenWidth / 2.0;
        double tracerStartY = screenHeight;

        // Convert 3D position to screen space (simplified)
        // This is a basic implementation - real tracers would need proper 3D to 2D projection

        matrices.push();
        matrices.translate(tracerStartX, tracerStartY, 0);
        // Draw line to player (this is simplified)
        matrices.pop();
    }

    @Override
    protected void onEnable() {
        System.out.println("Player ESP Enabled");
    }

    @Override
    protected void onDisable() {
        System.out.println("Player ESP Disabled");
    }
}