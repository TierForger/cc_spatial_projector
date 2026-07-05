package dev.tierforger.ccspatialprojector.client;

import dev.tierforger.ccspatialprojector.util.ProjectorKey;
import dev.tierforger.ccspatialprojector.util.ProjectorLocation;
import dev.tierforger.ccspatialprojector.visual.VisualObject;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public final class ClientVisualStore {
    private ClientVisualStore() {}

    private static final Map<String, Map<String, VisualObject>> OBJECTS_BY_SOURCE = new LinkedHashMap<>();
    private static final Map<String, ProjectorLocation> LOCATIONS_BY_SOURCE = new LinkedHashMap<>();

    public static void apply(CompoundTag root) {
        if (!root.contains("source", Tag.TAG_COMPOUND)) {
            clearAll();
            return;
        }

        Optional<ProjectorKey> maybeSource = ProjectorKey.tryFromTag(root.getCompound("source"));
        if (maybeSource.isEmpty()) return;
        ProjectorKey source = maybeSource.get();

        if (root.contains("location", Tag.TAG_COMPOUND)) {
            LOCATIONS_BY_SOURCE.put(source.id(), ProjectorLocation.fromTag(root.getCompound("location")));
        } else {
            LOCATIONS_BY_SOURCE.remove(source.id());
        }

        Map<String, VisualObject> objects = readObjects(root, clientTick());
        if (objects.isEmpty()) OBJECTS_BY_SOURCE.remove(source.id());
        else OBJECTS_BY_SOURCE.put(source.id(), objects);
    }

    public static Collection<VisualObject> objectsFor(ProjectorKey source) {
        Map<String, VisualObject> objects = OBJECTS_BY_SOURCE.get(source.id());
        if (objects == null) return List.of();

        long now = clientTick();
        objects.values().removeIf(object -> object.expired(now));
        if (objects.isEmpty()) {
            OBJECTS_BY_SOURCE.remove(source.id());
            return List.of();
        }
        return List.copyOf(objects.values());
    }

    public static Optional<ProjectorLocation> activeLocationFor(ProjectorKey key) {
        return Optional.ofNullable(LOCATIONS_BY_SOURCE.get(key.id()));
    }

    private static Map<String, VisualObject> readObjects(CompoundTag root, long now) {
        Map<String, VisualObject> objects = new LinkedHashMap<>();
        ListTag list = root.getList("objects", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            VisualObject object = VisualObject.fromTag(list.getCompound(i), now);
            objects.put(object.id(), object);
        }
        return objects;
    }

    private static void clearAll() {
        OBJECTS_BY_SOURCE.clear();
        LOCATIONS_BY_SOURCE.clear();
    }

    private static long clientTick() {
        return Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
    }
}
