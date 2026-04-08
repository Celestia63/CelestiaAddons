package com.celestia.addons.mixin;

import com.celestia.addons.feature.FeatureManager;
import com.celestia.addons.feature.impl.autoroute.AutoRouteFeature;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        AutoRouteFeature autoroute = getAutorouteFeature();
        if (autoroute != null && autoroute.isPlaying()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                mc.player.setSprinting(false);
            }
        }
    }

    private AutoRouteFeature getAutorouteFeature() {
        return FeatureManager.getInstance().getFeatures().stream()
            .filter(feature -> feature instanceof AutoRouteFeature)
            .map(feature -> (AutoRouteFeature) feature)
            .findFirst().orElse(null);
    }
}
