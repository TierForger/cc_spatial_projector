package dev.tierforger.ccspatialprojector.visual;

import dev.tierforger.ccspatialprojector.util.ProjectorKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public record VisualObject(
    ProjectorKey source,
    String id,
    VisualKind kind,
    List<Vec3> points,
    AABB box,
    VisualOptions options,
    long expiresAtGameTime
) {
    public VisualObject {
        points = List.copyOf(points);
    }

    public static VisualObject line(ProjectorKey source, String id, Vec3 from, Vec3 to, VisualOptions options, long now) {
        return new VisualObject(source, id, VisualKind.LINE, List.of(from, to), null, options, expiresAt(options, now));
    }

    public static VisualObject polyline(ProjectorKey source, String id, List<Vec3> points, VisualOptions options, long now) {
        return new VisualObject(source, id, VisualKind.POLYLINE, points, null, options, expiresAt(options, now));
    }

    public static VisualObject box(ProjectorKey source, String id, AABB box, VisualOptions options, long now) {
        return new VisualObject(source, id, VisualKind.BOX, List.of(), box, options, expiresAt(options, now));
    }

    public static VisualObject marker(ProjectorKey source, String id, Vec3 center, VisualOptions options, long now) {
        double radius = options.size();
        AABB box = new AABB(
            center.x - radius, center.y - radius, center.z - radius,
            center.x + radius, center.y + radius, center.z + radius
        );
        return new VisualObject(source, id, VisualKind.MARKER, List.of(center), box, options, expiresAt(options, now));
    }

    public VisualObject withSource(ProjectorKey newSource) {
        return new VisualObject(newSource, id, kind, points, box, options, expiresAtGameTime);
    }

    public boolean expired(long now) {
        return expiresAtGameTime != Long.MAX_VALUE && now >= expiresAtGameTime;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("source", source.toTag());
        tag.putString("id", id);
        tag.putString("kind", kind.name());
        tag.putInt("color", options.color());
        tag.putFloat("width", options.width());
        tag.putLong("ttlTicks", Math.max(0, options.ttlTicks()));
        tag.putFloat("size", options.size());

        ListTag pointsTag = new ListTag();
        for (Vec3 point : points) {
            CompoundTag pointTag = new CompoundTag();
            pointTag.putDouble("x", point.x);
            pointTag.putDouble("y", point.y);
            pointTag.putDouble("z", point.z);
            pointsTag.add(pointTag);
        }
        tag.put("points", pointsTag);

        if (box != null) tag.put("box", boxTag(box));
        return tag;
    }

    public static VisualObject fromTag(CompoundTag tag, long now) {
        ProjectorKey source = ProjectorKey.fromTag(tag.getCompound("source"));
        String id = tag.getString("id");
        VisualKind kind = VisualKind.valueOf(tag.getString("kind"));
        VisualOptions options = new VisualOptions(
            tag.getInt("color"),
            tag.getFloat("width"),
            tag.getLong("ttlTicks"),
            tag.getFloat("size")
        );

        List<Vec3> points = pointsFromTag(tag.getList("points", Tag.TAG_COMPOUND));
        AABB box = tag.contains("box", Tag.TAG_COMPOUND) ? boxFromTag(tag.getCompound("box")) : null;
        return new VisualObject(source, id, kind, points, box, options, expiresAt(options, now));
    }

    private static long expiresAt(VisualOptions options, long now) {
        return options.persistent() ? Long.MAX_VALUE : now + options.ttlTicks();
    }

    private static List<Vec3> pointsFromTag(ListTag pointsTag) {
        List<Vec3> points = new ArrayList<>(pointsTag.size());
        for (int i = 0; i < pointsTag.size(); i++) {
            CompoundTag point = pointsTag.getCompound(i);
            points.add(new Vec3(point.getDouble("x"), point.getDouble("y"), point.getDouble("z")));
        }
        return points;
    }

    private static CompoundTag boxTag(AABB box) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("minX", box.minX);
        tag.putDouble("minY", box.minY);
        tag.putDouble("minZ", box.minZ);
        tag.putDouble("maxX", box.maxX);
        tag.putDouble("maxY", box.maxY);
        tag.putDouble("maxZ", box.maxZ);
        return tag;
    }

    private static AABB boxFromTag(CompoundTag tag) {
        return new AABB(
            tag.getDouble("minX"), tag.getDouble("minY"), tag.getDouble("minZ"),
            tag.getDouble("maxX"), tag.getDouble("maxY"), tag.getDouble("maxZ")
        );
    }
}
