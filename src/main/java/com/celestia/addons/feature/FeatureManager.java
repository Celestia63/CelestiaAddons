package com.celestia.addons.feature;

import com.celestia.addons.feature.Feature;
import com.celestia.addons.feature.impl.StarMobEspFeature;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FeatureManager {
    private static final FeatureManager INSTANCE = new FeatureManager();
    private final List<Feature> features = new ArrayList<>();

    public static FeatureManager getInstance() {
        return INSTANCE;
    }

    private FeatureManager() {
        registerFeatures();
    }

    private void registerFeatures() {
        // General
        features.add(new Feature("Test Feature 1", "General", false));
        features.add(new Feature("Test Feature 2", "General", false));
        features.add(new com.celestia.addons.feature.impl.AutoRouteFeature());

        // Combat
        features.add(new Feature("Combat Test", "Combat", false));

        // Render
        features.add(new StarMobEspFeature());
        features.add(new com.celestia.addons.feature.impl.PlayerEspFeature());
        features.add(new com.celestia.addons.feature.impl.ChestEspFeature());
        features.add(new com.celestia.addons.feature.impl.FullbrightFeature());
        features.add(new Feature("Render Test", "Render", false));

        // Movement
        features.add(new com.celestia.addons.feature.impl.SpeedFeature());

        // Dungeon
        features.add(new com.celestia.addons.feature.impl.DungeonBreakerFeature());
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public List<Feature> getFeaturesByCategory(String category) {
        return features.stream()
                .filter(f -> f.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public Map<String, List<Feature>> getFeaturesByCategories() {
        return features.stream().collect(Collectors.groupingBy(Feature::getCategory));
    }
}
