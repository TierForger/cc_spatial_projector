package dev.tierforger.ccspatialprojector.config;

import dev.tierforger.ccspatialprojector.visual.VisualOptions;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class SpatialProjectorConfig {
    private SpatialProjectorConfig() {}

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.IntValue MAX_OBJECTS_PER_PROJECTOR;
    private static final ModConfigSpec.IntValue MAX_POINTS_PER_POLYLINE;
    private static final ModConfigSpec.IntValue MAX_TTL_SECONDS;
    private static final ModConfigSpec.IntValue MAX_OBJECT_ID_LENGTH;
    private static final ModConfigSpec.DoubleValue MAX_WIDTH;
    private static final ModConfigSpec.DoubleValue MAX_MARKER_SIZE;

    private static final ModConfigSpec.IntValue DEFAULT_COLOR;
    private static final ModConfigSpec.DoubleValue DEFAULT_WIDTH;
    private static final ModConfigSpec.DoubleValue DEFAULT_TTL_SECONDS;
    private static final ModConfigSpec.DoubleValue DEFAULT_MARKER_SIZE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("limits");
        MAX_OBJECTS_PER_PROJECTOR = builder
            .comment("Maximum number of visual objects stored in one projector buffer. Reusing an existing object ID does not count as a new object.")
            .defineInRange("maxObjectsPerProjector", 1024, 1, 65536);
        MAX_POINTS_PER_POLYLINE = builder
            .comment("Maximum number of points accepted by projector.polyline(id, points, options).")
            .defineInRange("maxPointsPerPolyline", 2048, 2, 65536);
        MAX_TTL_SECONDS = builder
            .comment("Maximum accepted ttl option in seconds. ttl=0 is always allowed and means persistent.")
            .defineInRange("maxTtlSeconds", 600, 0, 86400);
        MAX_OBJECT_ID_LENGTH = builder
            .comment("Maximum string length for visual object IDs used by line/polyline/box/marker/remove.")
            .defineInRange("maxObjectIdLength", 96, 1, 512);
        MAX_WIDTH = builder
            .comment("Maximum accepted line/polyline/box width option.")
            .defineInRange("maxWidth", 2.0, 0.001, 64.0);
        MAX_MARKER_SIZE = builder
            .comment("Maximum accepted marker size option.")
            .defineInRange("maxMarkerSize", 64.0, 0.001, 256.0);
        builder.pop();

        builder.push("defaults");
        DEFAULT_COLOR = builder
            .comment("Default RGB color for visuals when options.color is omitted. Use a decimal RGB integer from 0 to 16777215; 3407752 equals 0x33ff88.")
            .defineInRange("defaultColor", 0x33ff88, 0, 0xFFFFFF);
        DEFAULT_WIDTH = builder
            .comment("Default width for lines, polylines, and boxes when options.width is omitted. The effective value is clamped to limits.maxWidth.")
            .defineInRange("defaultWidth", 0.0625, 0.001, 64.0);
        DEFAULT_TTL_SECONDS = builder
            .comment("Default ttl in seconds when options.ttl is omitted. 0 means persistent. The effective value is clamped to limits.maxTtlSeconds.")
            .defineInRange("defaultTtlSeconds", 0.0, 0.0, 86400.0);
        DEFAULT_MARKER_SIZE = builder
            .comment("Default marker radius when options.size is omitted. The effective value is clamped to limits.maxMarkerSize.")
            .defineInRange("defaultMarkerSize", 0.35, 0.001, 256.0);
        builder.pop();

        SPEC = builder.build();
    }

    public static int maxObjectsPerProjector() {
        return MAX_OBJECTS_PER_PROJECTOR.get();
    }

    public static int maxPointsPerPolyline() {
        return MAX_POINTS_PER_POLYLINE.get();
    }

    public static int maxTtlSeconds() {
        return MAX_TTL_SECONDS.get();
    }

    public static int maxObjectIdLength() {
        return MAX_OBJECT_ID_LENGTH.get();
    }

    public static double maxWidth() {
        return MAX_WIDTH.get();
    }

    public static double maxMarkerSize() {
        return MAX_MARKER_SIZE.get();
    }

    public static int defaultColor() {
        return DEFAULT_COLOR.get();
    }

    public static double defaultWidth() {
        return clamp(DEFAULT_WIDTH.get(), 0.001, maxWidth());
    }

    public static double defaultTtlSeconds() {
        return clamp(DEFAULT_TTL_SECONDS.get(), 0.0, maxTtlSeconds());
    }

    public static double defaultMarkerSize() {
        return clamp(DEFAULT_MARKER_SIZE.get(), 0.001, maxMarkerSize());
    }

    public static VisualOptions defaultVisualOptions() {
        double ttlSeconds = defaultTtlSeconds();
        long ttlTicks = ttlSeconds == 0.0 ? 0L : Math.max(1L, Math.round(ttlSeconds * 20.0));
        return new VisualOptions(defaultColor(), (float) defaultWidth(), ttlTicks, (float) defaultMarkerSize());
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
