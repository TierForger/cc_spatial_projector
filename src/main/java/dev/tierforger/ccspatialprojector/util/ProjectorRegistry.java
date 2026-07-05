package dev.tierforger.ccspatialprojector.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Runtime lookup from stable projector id to the currently loaded projector block.
 */
public final class ProjectorRegistry {
    private ProjectorRegistry() {}

    private static final Map<String, ProjectorLocation> LOCATIONS = new LinkedHashMap<>();

    public static synchronized void register(ServerLevel level, BlockPos pos, String projectorId) {
        if (ProjectorIdSavedData.isValidProjectorId(projectorId)) {
            LOCATIONS.put(projectorId, ProjectorLocation.of(level, pos));
        }
    }

    public static synchronized void unregister(Level level, BlockPos pos, String projectorId) {
        if (!ProjectorIdSavedData.isValidProjectorId(projectorId)) return;

        ProjectorLocation current = LOCATIONS.get(projectorId);
        if (current != null && current.matches(level, pos)) {
            LOCATIONS.remove(projectorId);
        }
    }

    public static synchronized Optional<ProjectorLocation> location(String projectorId) {
        if (!ProjectorIdSavedData.isValidProjectorId(projectorId)) return Optional.empty();
        return Optional.ofNullable(LOCATIONS.get(projectorId));
    }

    public static synchronized boolean isIdUsedByAnotherProjector(Level level, BlockPos ownPos, String projectorId) {
        if (!ProjectorIdSavedData.isValidProjectorId(projectorId)) return false;

        ProjectorLocation current = LOCATIONS.get(projectorId);
        return current != null && !current.matches(level, ownPos);
    }
}
