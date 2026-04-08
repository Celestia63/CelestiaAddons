package com.celestia.addons.command;

import com.celestia.addons.CelestiaAddons;
import com.celestia.addons.gui.CelestiaScreen;
import com.celestia.addons.feature.Feature;
import com.celestia.addons.feature.FeatureManager;
import com.celestia.addons.feature.impl.AutoRouteFeature;
import com.celestia.addons.feature.impl.autoroute.DungeonRoomManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.nio.file.Path;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class CelestiaCommands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

            dispatcher.register(literal("ca")
                    .then(literal("route")
                            .then(literal("load_rooms")
                                    .then(argument("filename", StringArgumentType.string())
                                            .executes(context -> {
                                                String filename = StringArgumentType.getString(context, "filename");
                                                Path p = MinecraftClient.getInstance().runDirectory.toPath().resolve("celestiaaddons").resolve("rooms").resolve(filename);
                                                DungeonRoomManager.getInstance().loadRoomData(p);
                                                context.getSource().sendFeedback(Text.of("Loaded room definitions from " + filename));
                                                return Command.SINGLE_SUCCESS;
                                            })))
                            .then(literal("load_routes")
                                    .then(argument("filename", StringArgumentType.string())
                                            .executes(context -> {
                                                String filename = StringArgumentType.getString(context, "filename");
                                                Path p = MinecraftClient.getInstance().runDirectory.toPath().resolve("celestiaaddons").resolve("rooms").resolve(filename);
                                                DungeonRoomManager.getInstance().loadRoutes(p);
                                                context.getSource().sendFeedback(Text.of("Loaded custom routes from " + filename));
                                                return Command.SINGLE_SUCCESS;
                                            })))
                            .then(literal("scan")
                                    .executes(context -> {
                                        AutoRouteFeature feature = (AutoRouteFeature) FeatureManager.getInstance().getFeatures().stream()
                                                .filter(f -> f instanceof AutoRouteFeature).findFirst().orElse(null);
                                        if (feature != null) {
                                            feature.scanAndLoadRoom(0);
                                            context.getSource().sendFeedback(Text.of("Scanned and loaded room."));
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .then(literal("play")
                                    .executes(context -> {
                                        AutoRouteFeature feature = (AutoRouteFeature) FeatureManager.getInstance().getFeatures().stream()
                                                .filter(f -> f instanceof AutoRouteFeature).findFirst().orElse(null);
                                        if (feature != null) {
                                            feature.playRoute();
                                            context.getSource().sendFeedback(Text.of("Playing route..."));
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .then(literal("stop")
                                    .executes(context -> {
                                        AutoRouteFeature feature = (AutoRouteFeature) FeatureManager.getInstance().getFeatures().stream()
                                                .filter(f -> f instanceof AutoRouteFeature).findFirst().orElse(null);
                                        if (feature != null) {
                                            feature.stopRoute();
                                            context.getSource().sendFeedback(Text.of("Stopped route."));
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .then(literal("gui")
                                    .executes(context -> {
                                        MinecraftClient.getInstance().send(() -> {
                                            MinecraftClient.getInstance().setScreen(new CelestiaScreen(CelestiaAddons.getFeatureManager()));
                                        });
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .then(literal("edit")
                                    .then(literal("start")
                                            .executes(context -> {
                                                AutoRouteFeature feature = getAutoRouteFeature();
                                                if (feature != null) {
                                                    feature.recorder.startRecording();
                                                    context.getSource().sendFeedback(Text.of("Started editing route. Origin: " + feature.recorder.getOriginX() + ", " + feature.recorder.getOriginZ()));
                                                }
                                                return Command.SINGLE_SUCCESS;
                                            }))
                                    .then(literal("add")
                                            .then(argument("type", StringArgumentType.string())
                                                    .executes(context -> {
                                                        String typeStr = StringArgumentType.getString(context, "type").toUpperCase();
                                                        try {
                                                            com.celestia.addons.feature.impl.autoroute.Waypoint.ActionType type = 
                                                                com.celestia.addons.feature.impl.autoroute.Waypoint.ActionType.valueOf(typeStr);
                                                            AutoRouteFeature feature = getAutoRouteFeature();
                                                            if (feature != null && feature.recorder.isRecording()) {
                                                                feature.recorder.addWaypoint(type);
                                                                context.getSource().sendFeedback(Text.of("Added waypoint ring for: " + type));
                                                            } else {
                                                                context.getSource().sendFeedback(Text.of("Not in edit mode. Start with '/sxa route edit start'."));
                                                            }
                                                        } catch(Exception e) {
                                                            context.getSource().sendFeedback(Text.of("Invalid ActionType: " + typeStr));
                                                        }
                                                        return Command.SINGLE_SUCCESS;
                                                    })))
                                    .then(literal("save")
                                            .then(argument("core", IntegerArgumentType.integer())
                                                    .then(argument("filename", StringArgumentType.string())
                                                            .executes(context -> {
                                                                int core = IntegerArgumentType.getInteger(context, "core");
                                                                String filename = StringArgumentType.getString(context, "filename");
                                                                AutoRouteFeature feature = getAutoRouteFeature();
                                                                if (feature != null && feature.recorder.isRecording()) {
                                                                    com.celestia.addons.feature.impl.autoroute.Route r = feature.recorder.stopAndGetRoute();
                                                                    Path p = MinecraftClient.getInstance().runDirectory.toPath().resolve("celestiaaddons").resolve("rooms").resolve(filename);
                                                                    DungeonRoomManager.getInstance().saveRecordedRoute(p, core, 0, r.getWaypoints(), feature.recorder.getOriginX(), feature.recorder.getOriginZ());
                                                                    context.getSource().sendFeedback(Text.of("Route saved to " + filename));
                                                                } else {
                                                                    context.getSource().sendFeedback(Text.of("Not in edit mode."));
                                                                }
                                                                return Command.SINGLE_SUCCESS;
                                                            }))))
                            )
                    )
                    .executes(context -> {
                        MinecraftClient.getInstance().send(() -> {
                            MinecraftClient.getInstance().setScreen(new CelestiaScreen(CelestiaAddons.getFeatureManager()));
                        });
                        return Command.SINGLE_SUCCESS;
                    }));

            // Register /seratrixaddons
            dispatcher.register(literal("celestiaaddons")
                    .executes(context -> {
                        MinecraftClient.getInstance().send(() -> {
                            MinecraftClient.getInstance().setScreen(new CelestiaScreen(CelestiaAddons.getFeatureManager()));
                        });
                        return Command.SINGLE_SUCCESS;
                    }));
        });
    }

    private static AutoRouteFeature getAutoRouteFeature() {
        for (Feature f : FeatureManager.getInstance().getFeatures()) {
            if (f instanceof AutoRouteFeature) {
                return (AutoRouteFeature) f;
            }
        }
        return null;
    }
}
