package dev.tierforger.ccspatialprojector.visual;

import dev.tierforger.ccspatialprojector.network.VisualSyncPayload;
import dev.tierforger.ccspatialprojector.util.GoggleAccess;
import dev.tierforger.ccspatialprojector.util.ProjectorKey;
import dev.tierforger.ccspatialprojector.util.ProjectorRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class ServerVisualStore {
    private ServerVisualStore() {}

    private static final Map<ProjectorKey, Map<String, VisualObject>> OBJECTS_BY_SOURCE = new LinkedHashMap<>();

    public static synchronized void put(ServerLevel level, ProjectorKey source, VisualObject object) {
        objectsForMutation(source).put(object.id(), object);
        syncSource(level, source);
    }

    public static synchronized void remove(ServerLevel level, ProjectorKey source, String id) {
        Map<String, VisualObject> objects = OBJECTS_BY_SOURCE.get(source);
        if (objects != null) {
            objects.remove(id);
            if (objects.isEmpty()) OBJECTS_BY_SOURCE.remove(source);
        }
        syncSource(level, source);
    }

    public static synchronized void clear(ServerLevel level, ProjectorKey source) {
        OBJECTS_BY_SOURCE.remove(source);
        syncSource(level, source);
    }

    public static synchronized void clearSource(ServerLevel level, ProjectorKey source) {
        OBJECTS_BY_SOURCE.remove(source);
        syncClearSource(level, source);
    }


    public static synchronized boolean canAccept(ServerLevel level, ProjectorKey source, String id, int maxObjects) {
        pruneExpired(level);
        Map<String, VisualObject> objects = OBJECTS_BY_SOURCE.get(source);
        return objects == null || objects.containsKey(id) || objects.size() < maxObjects;
    }

    public static synchronized Collection<VisualObject> list(ServerLevel level, ProjectorKey source) {
        pruneExpired(level);
        Map<String, VisualObject> objects = OBJECTS_BY_SOURCE.get(source);
        return objects == null ? List.of() : List.copyOf(objects.values());
    }

    public static synchronized List<String> listIds(ServerLevel level, ProjectorKey source) {
        pruneExpired(level);
        Map<String, VisualObject> objects = OBJECTS_BY_SOURCE.get(source);
        return objects == null ? List.of() : List.copyOf(objects.keySet());
    }

    public static int subscriberCount(ServerLevel level, ProjectorKey source) {
        int count = 0;
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (GoggleAccess.isSubscribed(player, source)) count++;
        }
        return count;
    }

    public static synchronized void syncSource(ServerLevel level, ProjectorKey source) {
        pruneExpired(level);
        sendTo(level, player -> GoggleAccess.isSubscribed(player, source), payload(source, true));
    }

    public static synchronized void syncSourceTo(ServerPlayer player, ProjectorKey source) {
        pruneExpired(player.serverLevel());
        PacketDistributor.sendToPlayer(player, payload(source, true));
    }

    public static synchronized void syncCurrentTo(ServerPlayer player) {
        pruneExpired(player.serverLevel());
        GoggleAccess.activeSource(player).ifPresentOrElse(
            source -> PacketDistributor.sendToPlayer(player, payload(source, true)),
            () -> PacketDistributor.sendToPlayer(player, new VisualSyncPayload(emptyTag()))
        );
    }

    private static void syncClearSource(ServerLevel level, ProjectorKey source) {
        sendTo(level, player -> GoggleAccess.hasAnyBoundGoggles(player, source), payload(source, false));
    }

    private static void sendTo(ServerLevel level, Predicate<ServerPlayer> predicate, VisualSyncPayload payload) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (predicate.test(player)) PacketDistributor.sendToPlayer(player, payload);
        }
    }

    private static Map<String, VisualObject> objectsForMutation(ProjectorKey source) {
        return OBJECTS_BY_SOURCE.computeIfAbsent(source, ignored -> new LinkedHashMap<>());
    }

    private static void pruneExpired(ServerLevel level) {
        long now = level.getGameTime();
        OBJECTS_BY_SOURCE.values().forEach(objects -> objects.values().removeIf(object -> object.expired(now)));
        OBJECTS_BY_SOURCE.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private static VisualSyncPayload payload(ProjectorKey source, boolean includeLocation) {
        CompoundTag root = new CompoundTag();
        root.put("source", source.toTag());
        if (includeLocation) ProjectorRegistry.location(source.id()).ifPresent(location -> root.put("location", location.toTag()));

        ListTag objectsTag = new ListTag();
        Map<String, VisualObject> objects = OBJECTS_BY_SOURCE.get(source);
        if (objects != null) {
            for (VisualObject object : objects.values()) objectsTag.add(object.toTag());
        }
        root.put("objects", objectsTag);
        return new VisualSyncPayload(root);
    }

    private static CompoundTag emptyTag() {
        CompoundTag root = new CompoundTag();
        root.put("objects", new ListTag());
        return root;
    }
}
