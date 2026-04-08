package com.celestia.addons.feature.impl.autoroute;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class RoutePlayer {
    private Route route;
    private boolean playing;
    private int currentIndex;
    private int packetsPerTick;
    private boolean safeMode;
    private Runnable onComplete;

    public RoutePlayer() {
        this.playing = false;
        this.packetsPerTick = 5;
        this.safeMode = true;
    }

    public void startPlaying(Route route, int packetsPerTick, boolean safeMode, Runnable onComplete) {
        this.route = route;
        this.playing = true;
        this.currentIndex = 0;
        this.packetsPerTick = packetsPerTick;
        this.safeMode = safeMode;
        this.onComplete = onComplete;
    }

    public void stop() {
        this.playing = false;
        this.route = null;
        this.currentIndex = 0;
    }

    public boolean isPlaying() {
        return playing;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public Route getRoute() {
        return route;
    }

    public double getProgress() {
        if (route == null || route.size() == 0) return 0;
        return (double) currentIndex / route.size() * 100.0;
    }

    public void onTick() {
        if (!playing || route == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.getNetworkHandler() == null) {
            stop();
            return;
        }

        for (int i = 0; i < packetsPerTick; i++) {
            if (currentIndex >= route.size()) {
                // Route finished
                playing = false;
                if (onComplete != null) {
                    onComplete.run();
                }
                return;
            }

            Waypoint wp = route.get(currentIndex);

            if (wp.action == Waypoint.ActionType.ROTATE) {
                player.setYaw(wp.yaw);
                player.setPitch(wp.pitch);
                currentIndex++;
            } else if (wp.action == Waypoint.ActionType.WALK || wp.action == Waypoint.ActionType.START_WALK) {
                double targetX = wp.center ? Math.floor(wp.x) + 0.5 : wp.x;
                double targetZ = wp.center ? Math.floor(wp.z) + 0.5 : wp.z;
                boolean reached = movePlayerTowards(player, mc, targetX, wp.y, targetZ, wp.yaw, wp.pitch);
                if (reached) {
                    if (wp.stopMotion) {
                        player.setVelocity(0, 0, 0);
                    }
                    currentIndex++;
                }
            } else {
                double targetX = wp.center ? Math.floor(wp.x) + 0.5 : wp.x;
                double targetZ = wp.center ? Math.floor(wp.z) + 0.5 : wp.z;
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(targetX, wp.y, targetZ, wp.yaw, wp.pitch, true, true));
                player.setPosition(targetX, wp.y, targetZ);
                player.setYaw(wp.yaw);
                player.setPitch(wp.pitch);

                if (wp.stopMotion) {
                    player.setVelocity(0, 0, 0);
                }

                if (wp.item != null) {
                    swapToItem(wp.item.toLowerCase());
                }

                if (wp.action == Waypoint.ActionType.ETHERWARP) {
                    player.setSneaking(true);
                    mc.interactionManager.interactItem(player, net.minecraft.util.Hand.MAIN_HAND);
                    player.setSneaking(false);
                } else if (wp.action == Waypoint.ActionType.SUPERBOOM || wp.action == Waypoint.ActionType.INTERACT || wp.action == Waypoint.ActionType.USE || wp.action == Waypoint.ActionType.PEARL_CLIP) {
                    if (wp.leftClick) {
                        mc.interactionManager.attackBlock(player.getBlockPos(), net.minecraft.util.math.Direction.DOWN);
                    } else {
                        mc.interactionManager.interactItem(player, net.minecraft.util.Hand.MAIN_HAND);
                    }
                }

                currentIndex++;
            }
        }
    }

    private boolean movePlayerTowards(ClientPlayerEntity player, MinecraftClient mc, double targetX, double targetY, double targetZ, float targetYaw, float targetPitch) {
        double currentX = player.getX();
        double currentY = player.getY();
        double currentZ = player.getZ();

        double dx = targetX - currentX;
        double dy = targetY - currentY;
        double dz = targetZ - currentZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 0.25) {
            sendMovePacket(mc, targetX, targetY, targetZ, targetYaw, targetPitch);
            player.setPosition(targetX, targetY, targetZ);
            player.setYaw(targetYaw);
            player.setPitch(targetPitch);
            return true;
        }

        if (safeMode) {
            double maxStepDistance = 0.4;
            double stepRatio = Math.min(1.0, maxStepDistance / distance);
            double stepX = currentX + dx * stepRatio;
            double stepY = currentY + dy * stepRatio;
            double stepZ = currentZ + dz * stepRatio;
            sendSafeMovePacket(mc, stepX, stepY, stepZ, targetYaw, targetPitch);
            player.setYaw(targetYaw);
            player.setPitch(targetPitch);
            return false;
        }

        int stepCount = Math.min(3, Math.max(1, (int) Math.ceil(distance / 1.5)));
        for (int step = 1; step <= stepCount; step++) {
            double factor = (double) step / stepCount;
            double stepX = currentX + dx * factor;
            double stepY = currentY + dy * factor;
            double stepZ = currentZ + dz * factor;
            sendMovePacket(mc, stepX, stepY, stepZ, targetYaw, targetPitch);
        }

        player.setPosition(targetX, targetY, targetZ);
        player.setYaw(targetYaw);
        player.setPitch(targetPitch);
        return true;
    }

    private void sendMovePacket(MinecraftClient mc, double x, double y, double z, float yaw, float pitch) {
        if (mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, true, true));
    }

    private void sendSafeMovePacket(MinecraftClient mc, double x, double y, double z, float yaw, float pitch) {
        if (mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true, false));
    }

    private void swapToItem(String itemName) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        
        net.minecraft.entity.player.PlayerInventory inv = mc.player.getInventory();
        
        // Search hotbar (slots 0-8) for item containing the name
        for (int i = 0; i < 9; i++) {
            net.minecraft.item.ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                String itemDisplayName = stack.getName().getString().toLowerCase();
                if (itemDisplayName.contains(itemName)) {
                    try {
                        java.lang.reflect.Field field = inv.getClass().getDeclaredField("selectedSlot");
                        field.setAccessible(true);
                        field.setInt(inv, i);
                    } catch (Exception ignored) {
                        // Fallback: use the Minecraft packet only
                    }
                    if (mc.getNetworkHandler() != null) {
                        mc.getNetworkHandler().sendPacket(
                            new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(i)
                        );
                    }
                    return;
                }
            }
        }
    }
}
