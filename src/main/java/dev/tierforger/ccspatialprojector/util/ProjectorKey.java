package dev.tierforger.ccspatialprojector.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;

/**
 * Stable projector identity used by goggles bindings and visual subscriptions.
 */
public record ProjectorKey(String id) {
    private static final String TAG_ID = "id";

    public ProjectorKey {
        if (!ProjectorIdSavedData.isValidProjectorId(id)) {
            throw new IllegalArgumentException("Invalid projector id: " + id);
        }
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_ID, id);
        return tag;
    }

    public static Optional<ProjectorKey> tryFromTag(CompoundTag tag) {
        if (!tag.contains(TAG_ID, Tag.TAG_STRING)) return Optional.empty();

        String id = tag.getString(TAG_ID);
        return ProjectorIdSavedData.isValidProjectorId(id) ? Optional.of(new ProjectorKey(id)) : Optional.empty();
    }

    public static ProjectorKey fromTag(CompoundTag tag) {
        return tryFromTag(tag).orElseThrow(() -> new IllegalArgumentException("Tag does not contain a valid projector id"));
    }
}
