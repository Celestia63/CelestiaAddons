package com.celestia.addons.feature.impl;

import com.celestia.addons.feature.Feature;
import com.celestia.addons.setting.ColorSetting;
import com.celestia.addons.setting.ModeSetting;
import java.awt.Color;

public class StarMobEspFeature extends Feature {

    // Public settings so we can access them in the render loop later
    public final ModeSetting mode = new ModeSetting("Mode", "3D Box", "3D Box", "2D Box", "Glowing");
    public final ColorSetting color = new ColorSetting("ESP Color", new Color(255, 215, 0)); // Gold

    public StarMobEspFeature() {
        super("Starred Mob ESP", "Render", false);
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

        net.minecraft.client.MinecraftClient.getInstance().world.getEntities().forEach(entity -> {
            if (entity instanceof net.minecraft.entity.decoration.ArmorStandEntity) {
                String name = entity.getDisplayName().getString();
                if (name.contains("✯")) { // Check for Star
                    // Logic: The mob is usually the one RIDING the armor stand or directly below
                    // it.
                    // In Hypixel Dungeons, the star is on a separate ArmorStand that follows the
                    // mob.
                    // We will simple highlight the ArmorStand itself OR scanning for entities
                    // extremely close (below).

                    // Simple approach for testing: Highlight the ArmorStand itself if it has the
                    // star.
                    // Refined approach: Look for entities in a small box below the armor stand.

                    net.minecraft.util.math.Box searchBox = entity.getBoundingBox().expand(0, 2, 0).offset(0, -1, 0);

                    net.minecraft.client.MinecraftClient.getInstance().world
                            .getEntitiesByClass(net.minecraft.entity.LivingEntity.class, searchBox,
                                    e -> !(e instanceof net.minecraft.entity.decoration.ArmorStandEntity))
                            .stream().findFirst().ifPresent(target -> {
                                drawEspBox(context, target);
                            });
                }
            }
        });
    }

    private void drawEspBox(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context,
            net.minecraft.entity.Entity entity) {
        net.minecraft.client.util.math.MatrixStack matrices = context.matrixStack();
        net.minecraft.util.math.Vec3d cameraPos = net.minecraft.client.MinecraftClient.getInstance().gameRenderer
                .getCamera().getPos();

        double x = entity.getX() - cameraPos.x;
        double y = entity.getY() - cameraPos.y;
        double z = entity.getZ() - cameraPos.z;

        int argb = color.getRGB();

        if (mode.is("3D Box")) {
            // Get VertexConsumer for lines
            net.minecraft.client.render.VertexConsumer consumer = context.consumers()
                    .getBuffer(net.minecraft.client.render.RenderLayer.getLines());
            com.celestia.addons.util.RenderUtils.draw3DBox(matrices.peek().getPositionMatrix(), consumer, x, y, z, entity.getWidth(),
                    entity.getHeight(), argb);
        } else if (mode.is("Glowing")) {
            // Fallback for now using same box logic or skip
            net.minecraft.client.render.VertexConsumer consumer = context.consumers()
                    .getBuffer(net.minecraft.client.render.RenderLayer.getLines());
            com.celestia.addons.util.RenderUtils.draw3DBox(matrices.peek().getPositionMatrix(), consumer, x, y, z, entity.getWidth(),
                    entity.getHeight(), argb);
        }
    }

    @Override
    protected void onEnable() {
        System.out.println("Starred Mob ESP Enabled");
    }

    @Override
    protected void onDisable() {
        System.out.println("Starred Mob ESP Disabled");
    }
}
