package dev.tierforger.ccspatialprojector.peripheral;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dev.tierforger.ccspatialprojector.config.SpatialProjectorConfig;
import dev.tierforger.ccspatialprojector.visual.VisualOptions;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Strict Lua argument validation for the spatial_projector peripheral.
 *
 * Policy:
 * - Missing optional options use defaults.
 * - Explicitly invalid values throw LuaException.
 * - Unknown option keys throw LuaException.
 * - Point arrays use exactly {number, number, number}.
 */
public final class SpatialProjectorArgs {
    private SpatialProjectorArgs() {}

    private static final Set<String> ALLOWED_OPTIONS = Set.of("color", "width", "ttl", "size");

    public static void noArguments(String method, IArguments args) throws LuaException {
        if (args.count() != 0) {
            throw fail(method, "expected no arguments; got " + args.count());
        }
    }

    public static void argumentCount(String method, IArguments args, int expected) throws LuaException {
        if (args.count() != expected) {
            throw fail(method, "expected " + expected + " argument" + (expected == 1 ? "" : "s") + "; got " + args.count());
        }
    }

    public static void argumentRange(String method, IArguments args, int min, int max) throws LuaException {
        if (args.count() < min || args.count() > max) {
            throw fail(method, "expected " + min + " to " + max + " arguments; got " + args.count());
        }
    }

    public static String objectId(String method, IArguments args, int index, String name) throws LuaException {
        String id = string(method, args, index, name);
        if (id.isBlank()) {
            throw badArgument(method, index, name, "a non-empty string", "blank string");
        }
        int maxLength = SpatialProjectorConfig.maxObjectIdLength();
        if (id.length() > maxLength) {
            throw fail(method, "argument #" + (index + 1) + " '" + name + "' is too long; max " + maxLength + " characters, got " + id.length());
        }
        return id;
    }

    public static String string(String method, IArguments args, int index, String name) throws LuaException {
        Object value = args.get(index);
        if (value instanceof String s) return s;
        throw badArgument(method, index, name, "string", describe(value));
    }

    public static double finiteNumber(String method, IArguments args, int index, String name) throws LuaException {
        return finiteNumberValue(method, "argument #" + (index + 1) + " '" + name + "'", args.get(index));
    }

    public static Vec3 vec3(String method, IArguments args, int firstIndex, String xName, String yName, String zName) throws LuaException {
        return new Vec3(
            finiteNumber(method, args, firstIndex, xName),
            finiteNumber(method, args, firstIndex + 1, yName),
            finiteNumber(method, args, firstIndex + 2, zName)
        );
    }

    public static Map<?, ?> table(String method, IArguments args, int index, String name) throws LuaException {
        Object value = args.get(index);
        if (value instanceof Map<?, ?> table) return table;
        throw badArgument(method, index, name, "table", describe(value));
    }

    public static VisualOptions options(String method, IArguments args, int index) throws LuaException {
        VisualOptions defaults = VisualOptions.defaults();
        if (args.count() <= index || args.get(index) == null) return defaults;

        Object raw = args.get(index);
        if (!(raw instanceof Map<?, ?> table)) {
            throw fail(method, "options must be a table; got " + describe(raw));
        }

        for (Object rawKey : table.keySet()) {
            if (!(rawKey instanceof String key) || !ALLOWED_OPTIONS.contains(key)) {
                throw fail(method, "unknown option '" + rawKey + "'; allowed options: color, width, ttl, size");
            }
        }

        int color = colorOption(method, table, defaults.color());
        double width = numberOption(method, table, "width", defaults.width());
        double ttlSeconds = numberOption(method, table, "ttl", defaults.ttlTicks() / 20.0);
        double size = numberOption(method, table, "size", defaults.size());

        int maxTtlSeconds = SpatialProjectorConfig.maxTtlSeconds();
        double maxWidth = SpatialProjectorConfig.maxWidth();
        double maxMarkerSize = SpatialProjectorConfig.maxMarkerSize();

        if (ttlSeconds < 0 || ttlSeconds > maxTtlSeconds) {
            String rule = maxTtlSeconds == 0
                ? "0 because positive TTL is disabled by config"
                : "0 for persistent, or > 0 and <= " + maxTtlSeconds + " seconds";
            throw fail(method, "option 'ttl' must be " + rule + "; got " + formatNumber(ttlSeconds));
        }
        if (width <= 0 || width > maxWidth) {
            throw fail(method, "option 'width' must be > 0 and <= " + formatNumber(maxWidth) + "; got " + formatNumber(width));
        }
        if (size <= 0 || size > maxMarkerSize) {
            throw fail(method, "option 'size' must be > 0 and <= " + formatNumber(maxMarkerSize) + "; got " + formatNumber(size));
        }

        long ttlTicks = ttlSeconds == 0 ? 0 : Math.max(1, Math.round(ttlSeconds * 20.0));
        return new VisualOptions(color, (float) width, ttlTicks, (float) size);
    }

    public static List<Vec3> points(String method, Map<?, ?> table) throws LuaException {
        int maxIndex = maxArrayIndex(method, "points", table);
        List<Vec3> points = new ArrayList<>(maxIndex);
        for (int i = 1; i <= maxIndex; i++) {
            Object raw = arrayValue(table, i);
            if (raw == null) {
                throw fail(method, "points table has a gap at index #" + i + "; expected a dense array starting at 1");
            }
            if (!(raw instanceof Map<?, ?> point)) {
                throw fail(method, "point #" + i + " must be a table; expected {number, number, number}");
            }
            points.add(point(method, point, i));
        }
        return points;
    }

    private static Vec3 point(String method, Map<?, ?> table, int pointIndex) throws LuaException {
        if (table.containsKey("x") || table.containsKey("y") || table.containsKey("z")) {
            throw fail(method, "point #" + pointIndex + " uses named coordinates; expected compact format {number, number, number}");
        }

        Set<Integer> indexes = new LinkedHashSet<>();
        for (Object key : table.keySet()) {
            Integer idx = positiveIntegerKey(key);
            if (idx == null || idx < 1 || idx > 3) {
                throw fail(method, "point #" + pointIndex + " has invalid key '" + key + "'; expected exactly {number, number, number}");
            }
            indexes.add(idx);
        }
        for (int i = 1; i <= 3; i++) {
            if (!indexes.contains(i)) {
                throw fail(method, "point #" + pointIndex + " is missing coordinate #" + i + "; expected {number, number, number}");
            }
        }

        return new Vec3(
            finiteNumberValue(method, "point #" + pointIndex + " coordinate #1", arrayValue(table, 1)),
            finiteNumberValue(method, "point #" + pointIndex + " coordinate #2", arrayValue(table, 2)),
            finiteNumberValue(method, "point #" + pointIndex + " coordinate #3", arrayValue(table, 3))
        );
    }

    private static double numberOption(String method, Map<?, ?> table, String key, double defaultValue) throws LuaException {
        Object value = table.get(key);
        if (value == null) return defaultValue;
        return finiteNumberValue(method, "option '" + key + "'", value);
    }

    private static int colorOption(String method, Map<?, ?> table, int defaultValue) throws LuaException {
        Object value = table.get("color");
        if (value == null) return defaultValue;
        double number = finiteNumberValue(method, "option 'color'", value);
        if (number != Math.rint(number) || number < 0 || number > 0xFFFFFF) {
            throw fail(method, "option 'color' must be an integer from 0x000000 to 0xffffff; got " + describe(value));
        }
        return (int) number;
    }

    private static double finiteNumberValue(String method, String label, Object value) throws LuaException {
        if (!(value instanceof Number n)) {
            throw fail(method, label + " must be a finite number; got " + describe(value));
        }
        double number = n.doubleValue();
        if (!Double.isFinite(number)) {
            throw fail(method, label + " must be a finite number; got " + number);
        }
        return number;
    }

    private static int maxArrayIndex(String method, String tableName, Map<?, ?> table) throws LuaException {
        int max = 0;
        for (Object key : table.keySet()) {
            Integer idx = positiveIntegerKey(key);
            if (idx == null) {
                throw fail(method, tableName + " table has invalid key '" + key + "'; expected a dense numeric array starting at 1");
            }
            max = Math.max(max, idx);
        }
        return max;
    }

    private static Object arrayValue(Map<?, ?> table, int index) {
        Object value = table.get((double) index);
        if (value == null) value = table.get(index);
        return value;
    }

    private static Integer positiveIntegerKey(Object key) {
        if (!(key instanceof Number n)) return null;
        double value = n.doubleValue();
        if (!Double.isFinite(value) || value < 1 || value != Math.rint(value)) return null;
        return (int) value;
    }

    private static LuaException badArgument(String method, int index, String name, String expected, String got) {
        return fail(method, "argument #" + (index + 1) + " '" + name + "' must be " + expected + "; got " + got);
    }

    public static LuaException fail(String method, String message) {
        return new LuaException("spatial_projector." + method + ": " + message);
    }

    private static String describe(Object value) {
        if (value == null) return "nil";
        if (value instanceof String s) return '"' + s + '"';
        if (value instanceof Number || value instanceof Boolean) return String.valueOf(value);
        if (value instanceof Map<?, ?>) return "table";
        return value.getClass().getSimpleName();
    }

    private static String formatNumber(double value) {
        if (value == Math.rint(value)) return Long.toString((long) value);
        return Double.toString(value);
    }
}
