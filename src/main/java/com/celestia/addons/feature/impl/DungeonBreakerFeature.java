package com.celestia.addons.feature.impl;

import com.celestia.addons.feature.Feature;
import com.celestia.addons.setting.NumberSetting;
import com.celestia.addons.setting.BooleanSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class DungeonBreakerFeature extends Feature {
    private final NumberSetting range = new NumberSetting("Range", 5.0, 1.0, 10.0, 0.5);
    private final NumberSetting height = new NumberSetting("Height", 3.0, 1.0, 8.0, 0.5);
    private final BooleanSetting autoBreak = new BooleanSetting("Auto Break", true);

    public DungeonBreakerFeature() {
        super("DungeonBreaker", "Dungeon", false);
        addSetting(range);
        addSetting(height);
        addSetting(autoBreak);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient mc) {
        if (!isEnabled() || !autoBreak.getValue() || mc.player == null || mc.world == null) {
            return;
        }

        ClientPlayerEntity player = mc.player;
        World world = mc.world;

        // Calculate the breaking area around the player
        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        double breakRange = range.getValue();
        double breakHeight = height.getValue();

        // Create a box around the player for block breaking
        Box breakArea = new Box(
            playerX - breakRange, playerY - 1, playerZ - breakRange,
            playerX + breakRange, playerY + breakHeight, playerZ + breakRange
        );

        // Break blocks in the area
        for (int x = (int) Math.floor(breakArea.minX); x <= Math.ceil(breakArea.maxX); x++) {
            for (int y = (int) Math.floor(breakArea.minY); y <= Math.ceil(breakArea.maxY); y++) {
                for (int z = (int) Math.floor(breakArea.minZ); z <= Math.ceil(breakArea.maxZ); z++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    // Skip blocks that are air or unbreakable
                    if (world.getBlockState(pos).isAir() ||
                        world.getBlockState(pos).getHardness(world, pos) < 0) {
                        continue;
                    }

                    // Check if block is within exact range
                    double distance = Math.sqrt(
                        Math.pow(x - playerX, 2) +
                        Math.pow(y - playerY, 2) +
                        Math.pow(z - playerZ, 2)
                    );

                    if (distance <= breakRange) {
                        // Break the block
                        mc.interactionManager.attackBlock(pos, mc.player.getHorizontalFacing());
                    }
                }
            }
        }
    }

    @Override
    protected void onEnable() {
        System.out.println("DungeonBreaker enabled - will automatically break blocks in range");
    }

    @Override
    protected void onDisable() {
        System.out.println("DungeonBreaker disabled");
    }
}