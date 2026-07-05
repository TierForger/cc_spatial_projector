package dev.tierforger.ccspatialprojector.util;

import dev.tierforger.ccspatialprojector.CcSpatialProjector;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Server-wide monotonic id allocator for Spatial Projectors.
 *
 * Ids intentionally match ComputerCraft's human-readable style: 0, 1, 2...
 * The counter is stored in the overworld SavedData so it survives restarts and
 * is shared by all dimensions on the same server.
 */
public final class ProjectorIdSavedData extends SavedData {
    private static final String DATA_NAME = CcSpatialProjector.MOD_ID + "_projector_ids";
    private static final String TAG_NEXT_ID = "next_projector_id";

    private long nextId = 0L;

    public static ProjectorIdSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(ProjectorIdSavedData::new, ProjectorIdSavedData::load),
            DATA_NAME
        );
    }

    private static ProjectorIdSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        ProjectorIdSavedData data = new ProjectorIdSavedData();
        data.nextId = Math.max(0L, tag.getLong(TAG_NEXT_ID));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong(TAG_NEXT_ID, nextId);
        return tag;
    }

    public synchronized String allocate() {
        String id = Long.toString(nextId);
        nextId++;
        setDirty();
        return id;
    }

    public synchronized void observeExistingId(String projectorId) {
        long parsed = parseNumericId(projectorId);
        if (parsed >= nextId) {
            nextId = parsed + 1;
            setDirty();
        }
    }

    public static boolean isValidProjectorId(String projectorId) {
        if (projectorId == null || projectorId.isBlank()) return false;
        try {
            parseNumericId(projectorId);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static long parseNumericId(String projectorId) {
        if (projectorId == null || projectorId.isBlank()) {
            throw new IllegalArgumentException("blank projector id");
        }
        if (!projectorId.matches("0|[1-9][0-9]*")) {
            throw new IllegalArgumentException("projector id must be a non-negative decimal integer without leading zeroes");
        }
        try {
            return Long.parseLong(projectorId);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("projector id is too large for a 64-bit server counter", ex);
        }
    }
}
