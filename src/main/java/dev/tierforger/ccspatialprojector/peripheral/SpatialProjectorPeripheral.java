package dev.tierforger.ccspatialprojector.peripheral;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dev.tierforger.ccspatialprojector.block.SpatialProjectorBlockEntity;
import dev.tierforger.ccspatialprojector.config.SpatialProjectorConfig;
import dev.tierforger.ccspatialprojector.util.ProjectorKey;
import dev.tierforger.ccspatialprojector.util.ProjectorLocation;
import dev.tierforger.ccspatialprojector.visual.ServerVisualStore;
import dev.tierforger.ccspatialprojector.visual.VisualObject;
import dev.tierforger.ccspatialprojector.visual.VisualOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SpatialProjectorPeripheral implements IPeripheral {
    private static final String TYPE = "spatial_projector";

    private final SpatialProjectorBlockEntity blockEntity;

    public SpatialProjectorPeripheral(SpatialProjectorBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof SpatialProjectorPeripheral projector && projector.blockEntity == blockEntity;
    }

    @LuaFunction(mainThread = true)
    public final Object[] clear(IArguments args) throws LuaException {
        SpatialProjectorArgs.noArguments("clear", args);
        Context ctx = context();
        ServerVisualStore.clear(ctx.level(), ctx.source());
        return ok();
    }

    @LuaFunction(mainThread = true)
    public final Object[] remove(IArguments args) throws LuaException {
        SpatialProjectorArgs.argumentCount("remove", args, 1);
        String id = SpatialProjectorArgs.objectId("remove", args, 0, "id");
        Context ctx = context();
        ServerVisualStore.remove(ctx.level(), ctx.source(), id);
        return ok();
    }

    @LuaFunction(mainThread = true)
    public final Object[] line(IArguments args) throws LuaException {
        SpatialProjectorArgs.argumentRange("line", args, 7, 8);
        String id = SpatialProjectorArgs.objectId("line", args, 0, "id");
        Vec3 from = SpatialProjectorArgs.vec3("line", args, 1, "x1", "y1", "z1");
        Vec3 to = SpatialProjectorArgs.vec3("line", args, 4, "x2", "y2", "z2");
        if (from.equals(to)) {
            throw SpatialProjectorArgs.fail("line", "line endpoints are identical; expected two different points");
        }

        VisualOptions options = SpatialProjectorArgs.options("line", args, 7);
        Context ctx = context();
        return putVisual("line", ctx, id, VisualObject.line(ctx.source(), id, from, to, options, ctx.now()));
    }

    @LuaFunction(mainThread = true)
    public final Object[] polyline(IArguments args) throws LuaException {
        SpatialProjectorArgs.argumentRange("polyline", args, 2, 3);
        String id = SpatialProjectorArgs.objectId("polyline", args, 0, "id");
        Map<?, ?> table = SpatialProjectorArgs.table("polyline", args, 1, "points");
        VisualOptions options = SpatialProjectorArgs.options("polyline", args, 2);
        List<Vec3> points = SpatialProjectorArgs.points("polyline", table);
        if (points.size() < 2) {
            throw SpatialProjectorArgs.fail("polyline", "points must contain at least 2 points; got " + points.size());
        }
        int maxPoints = SpatialProjectorConfig.maxPointsPerPolyline();
        if (points.size() > maxPoints) {
            throw SpatialProjectorArgs.fail("polyline", "too many points; max " + maxPoints + ", got " + points.size());
        }

        Context ctx = context();
        return putVisual("polyline", ctx, id, VisualObject.polyline(ctx.source(), id, points, options, ctx.now()));
    }

    @LuaFunction(mainThread = true)
    public final Object[] box(IArguments args) throws LuaException {
        SpatialProjectorArgs.argumentRange("box", args, 7, 8);
        String id = SpatialProjectorArgs.objectId("box", args, 0, "id");
        double x1 = SpatialProjectorArgs.finiteNumber("box", args, 1, "x1");
        double y1 = SpatialProjectorArgs.finiteNumber("box", args, 2, "y1");
        double z1 = SpatialProjectorArgs.finiteNumber("box", args, 3, "z1");
        double x2 = SpatialProjectorArgs.finiteNumber("box", args, 4, "x2");
        double y2 = SpatialProjectorArgs.finiteNumber("box", args, 5, "y2");
        double z2 = SpatialProjectorArgs.finiteNumber("box", args, 6, "z2");
        if (x1 == x2 && y1 == y2 && z1 == z2) {
            throw SpatialProjectorArgs.fail("box", "box corners are identical; expected two different opposite corners");
        }

        VisualOptions options = SpatialProjectorArgs.options("box", args, 7);
        Context ctx = context();
        return putVisual("box", ctx, id, VisualObject.box(ctx.source(), id, new AABB(x1, y1, z1, x2, y2, z2), options, ctx.now()));
    }

    @LuaFunction(mainThread = true)
    public final Object[] marker(IArguments args) throws LuaException {
        SpatialProjectorArgs.argumentRange("marker", args, 4, 5);
        String id = SpatialProjectorArgs.objectId("marker", args, 0, "id");
        Vec3 center = SpatialProjectorArgs.vec3("marker", args, 1, "x", "y", "z");
        VisualOptions options = SpatialProjectorArgs.options("marker", args, 4);
        Context ctx = context();
        return putVisual("marker", ctx, id, VisualObject.marker(ctx.source(), id, center, options, ctx.now()));
    }

    @LuaFunction(mainThread = true)
    public final Map<Integer, String> list(IArguments args) throws LuaException {
        SpatialProjectorArgs.noArguments("list", args);
        Context ctx = context();

        Map<Integer, String> result = new LinkedHashMap<>();
        int index = 1;
        for (String id : ServerVisualStore.listIds(ctx.level(), ctx.source())) {
            result.put(index++, id);
        }
        return result;
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> stats(IArguments args) throws LuaException {
        SpatialProjectorArgs.noArguments("stats", args);
        Context ctx = context();
        ProjectorLocation loc = ProjectorLocation.of(ctx.level(), blockEntity.getBlockPos());
        return Map.of(
            "objects", ServerVisualStore.list(ctx.level(), ctx.source()).size(),
            "subscribers", ServerVisualStore.subscriberCount(ctx.level(), ctx.source()),
            "dimension", loc.dimension(),
            "sourceX", loc.x(),
            "sourceY", loc.y(),
            "sourceZ", loc.z(),
            "type", TYPE
        );
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> getLimits(IArguments args) throws LuaException {
        SpatialProjectorArgs.noArguments("getLimits", args);
        return Map.ofEntries(
            Map.entry("maxObjectsPerProjector", SpatialProjectorConfig.maxObjectsPerProjector()),
            Map.entry("maxPointsPerPolyline", SpatialProjectorConfig.maxPointsPerPolyline()),
            Map.entry("maxTtlSeconds", SpatialProjectorConfig.maxTtlSeconds()),
            Map.entry("persistentTtl", 0),
            Map.entry("ttlZeroMeans", "persistent until clear/remove/overwrite"),
            Map.entry("maxObjectIdLength", SpatialProjectorConfig.maxObjectIdLength()),
            Map.entry("maxWidth", SpatialProjectorConfig.maxWidth()),
            Map.entry("maxMarkerSize", SpatialProjectorConfig.maxMarkerSize()),
            Map.entry("defaultColor", SpatialProjectorConfig.defaultColor()),
            Map.entry("defaultWidth", SpatialProjectorConfig.defaultWidth()),
            Map.entry("defaultTtl", SpatialProjectorConfig.defaultTtlSeconds()),
            Map.entry("defaultMarkerSize", SpatialProjectorConfig.defaultMarkerSize()),
            Map.entry("pointFormat", "{number, number, number}"),
            Map.entry("config", "cc-spatial-projector-common.toml"),
            Map.entry("type", TYPE)
        );
    }

    @LuaFunction(mainThread = true)
    public final Object[] sync(IArguments args) throws LuaException {
        SpatialProjectorArgs.noArguments("sync", args);
        Context ctx = context();
        ServerVisualStore.syncSource(ctx.level(), ctx.source());
        return ok();
    }


    private static Object[] putVisual(String method, Context ctx, String id, VisualObject object) throws LuaException {
        int maxObjects = SpatialProjectorConfig.maxObjectsPerProjector();
        if (!ServerVisualStore.canAccept(ctx.level(), ctx.source(), id, maxObjects)) {
            throw SpatialProjectorArgs.fail(method, "projector buffer is full; max " + maxObjects + " objects; remove/clear objects or overwrite an existing id");
        }
        ServerVisualStore.put(ctx.level(), ctx.source(), object);
        return ok();
    }

    private Context context() throws LuaException {
        ServerLevel level = level();
        return new Context(level, blockEntity.projectorKey(level), level.getGameTime());
    }

    private ServerLevel level() throws LuaException {
        if (blockEntity.getLevel() instanceof ServerLevel serverLevel) return serverLevel;
        throw SpatialProjectorArgs.fail("internal", "projector is not attached to a server level");
    }

    private static Object[] ok() {
        return new Object[] { true };
    }

    private record Context(ServerLevel level, ProjectorKey source, long now) {}
}
