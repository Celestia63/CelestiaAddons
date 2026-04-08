package com.celestia.addons.feature.impl;

import com.celestia.addons.feature.Feature;
import com.celestia.addons.setting.ColorSetting;
import com.celestia.addons.setting.ModeSetting;
import java.awt.Color;

public class ChestEspFeature extends Feature {

    public final ModeSetting mode = new ModeSetting("Mode", "3D Box", "3D Box", "2D Box", "Glowing");
    public final ColorSetting color = new ColorSetting("ESP Color", new Color(255, 215, 0)); // Gold

    public ChestEspFeature() {
        super("Chest ESP", "Render", false);
        addSetting(mode);
        addSetting(color);

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

        // Scan for chests in render distance
        net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
        int renderDistance = mc.options.getViewDistance().getValue() * 16;

        for (int x = (int) (mc.player.getX() - renderDistance); x < mc.player.getX() + renderDistance; x += 16) {
            for (int y = (int) Math.max(0, mc.player.getY() - 64); y < Math.min(256, mc.player.getY() + 64); y += 16) {
                for (int z = (int) (mc.player.getZ() - renderDistance); z < mc.player.getZ() + renderDistance; z += 16) {
                    net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(x, y, z);
                    net.minecraft.block.BlockState state = mc.world.getBlockState(pos);

                    if (isChestBlock(state)) {
                        drawEspBox(context, pos);
                    }
                }
            }
        }
    }

    private boolean isChestBlock(net.minecraft.block.BlockState state) {
        net.minecraft.block.Block block = state.getBlock();
        return block instanceof net.minecraft.block.ChestBlock ||
               block instanceof net.minecraft.block.EnderChestBlock ||
               block instanceof net.minecraft.block.TrappedChestBlock;
    }

    private void drawEspBox(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context,
            net.minecraft.util.math.BlockPos pos) {
        net.minecraft.client.util.math.MatrixStack matrices = context.matrixStack();
        net.minecraft.util.math.Vec3d cameraPos = net.minecraft.client.MinecraftClient.getInstance().gameRenderer
                .getCamera().getPos();

        double x = pos.getX() - cameraPos.x;
        double y = pos.getY() - cameraPos.y;
        double z = pos.getZ() - cameraPos.z;

        int argb = color.getRGB();

        if (mode.is("3D Box")) {
            net.minecraft.client.render.VertexConsumer consumer = context.consumers()
                    .getBuffer(net.minecraft.client.render.RenderLayer.getLines());
            com.celestia.addons.util.RenderUtils.draw3DBox(matrices.peek().getPositionMatrix(), consumer, x, y, z, 1.0f, 1.0f, argb);
        } else if (mode.is("Glowing")) {
            // For glowing, we could modify block rendering, but for now use box
            net.minecraft.client.render.VertexConsumer consumer = context.consumers()
                    .getBuffer(net.minecraft.client.render.RenderLayer.getLines());
            com.celestia.addons.util.RenderUtils.draw3DBox(matrices.peek().getPositionMatrix(), consumer, x, y, z, 1.0f, 1.0f, argb);
        }
    }

    @Override
    protected void onEnable() {
        System.out.println("Chest ESP Enabled");
    }

    @Override
    protected void onDisable() {
        System.out.println("Chest ESP Disabled");
    }
}