package dev.tierforger.ccspatialprojector.visual;

import dev.tierforger.ccspatialprojector.config.SpatialProjectorConfig;

public record VisualOptions(int color, float width, long ttlTicks, float size) {
    public boolean persistent() {
        return ttlTicks <= 0;
    }

    public static VisualOptions defaults() {
        return SpatialProjectorConfig.defaultVisualOptions();
    }
}
