package dev.tierforger.ccspatialprojector.block;

import dev.tierforger.ccspatialprojector.item.SpatialProjectorBlockItem;
import dev.tierforger.ccspatialprojector.peripheral.SpatialProjectorPeripheral;
import dev.tierforger.ccspatialprojector.registry.ModBlockEntities;
import dev.tierforger.ccspatialprojector.util.ProjectorIdSavedData;
import dev.tierforger.ccspatialprojector.util.ProjectorKey;
import dev.tierforger.ccspatialprojector.util.ProjectorRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class SpatialProjectorBlockEntity extends BlockEntity {
    public static final String TAG_PROJECTOR_ID = "projector_id";

    private final SpatialProjectorPeripheral peripheral = new SpatialProjectorPeripheral(this);
    private String projectorId = "";

    public SpatialProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPATIAL_PROJECTOR.get(), pos, state);
    }

    public SpatialProjectorPeripheral peripheral() {
        return peripheral;
    }

    public String getProjectorId() {
        return projectorId;
    }

    /**
     * Returns a source key for code paths that are allowed to mutate this block entity.
     *
     * <p>ComputerCraft/MoreRed may call {@code Level#getBlockEntity()} while processing neighbour
     * updates. That can re-enter {@link #clearRemoved()}, so lifecycle methods below deliberately
     * never allocate ids or call {@link #setChanged()}. Allocation is kept here and in explicit
     * placement/Lua/player actions only.</p>
     */
    public ProjectorKey projectorKey(ServerLevel level) {
        ensureProjectorId(level);
        return new ProjectorKey(projectorId);
    }

    public Optional<ProjectorKey> projectorKeyIfPresent() {
        return normalizeProjectorId(projectorId).map(ProjectorKey::new);
    }

    public void restoreOrAllocateProjectorId(ServerLevel level, Optional<String> candidate) {
        if (candidate.isPresent()
            && !ProjectorRegistry.isIdUsedByAnotherProjector(level, worldPosition, candidate.get())) {
            setProjectorId(level, candidate.get());
            return;
        }
        ensureProjectorId(level);
    }

    public void setProjectorId(ServerLevel level, String newProjectorId) {
        requireValidProjectorId(newProjectorId);
        if (newProjectorId.equals(projectorId)) {
            ProjectorIdSavedData.get(level).observeExistingId(newProjectorId);
            ProjectorRegistry.register(level, worldPosition, newProjectorId);
            return;
        }

        String oldProjectorId = projectorId;
        projectorId = newProjectorId;
        setChanged();

        ProjectorIdSavedData.get(level).observeExistingId(newProjectorId);
        ProjectorRegistry.unregister(level, worldPosition, oldProjectorId);
        ProjectorRegistry.register(level, worldPosition, newProjectorId);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) registerLoadedProjectorId(serverLevel);
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            ProjectorRegistry.unregister(serverLevel, worldPosition, projectorId);
        }
        super.setRemoved();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (level instanceof ServerLevel serverLevel) registerLoadedProjectorId(serverLevel);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString(TAG_PROJECTOR_ID, projectorId);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        projectorId = normalizeProjectorId(tag.getString(TAG_PROJECTOR_ID)).orElse("");
    }

    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        super.saveToItem(stack, registries);
        SpatialProjectorBlockItem.setProjectorId(stack, projectorId);
    }

    public void ensureProjectorId(ServerLevel level) {
        Optional<String> normalized = normalizeProjectorId(projectorId);
        if (normalized.isEmpty()
            || ProjectorRegistry.isIdUsedByAnotherProjector(level, worldPosition, normalized.get())) {
            allocateProjectorId(level);
            return;
        }

        String validProjectorId = normalized.get();
        if (!validProjectorId.equals(projectorId)) {
            projectorId = validProjectorId;
            setChanged();
        }
        ProjectorIdSavedData.get(level).observeExistingId(projectorId);
        ProjectorRegistry.register(level, worldPosition, projectorId);
    }

    private void registerLoadedProjectorId(ServerLevel level) {
        Optional<String> normalized = normalizeProjectorId(projectorId);
        if (normalized.isEmpty()) return;

        String validProjectorId = normalized.get();
        projectorId = validProjectorId;
        ProjectorIdSavedData.get(level).observeExistingId(validProjectorId);
        if (!ProjectorRegistry.isIdUsedByAnotherProjector(level, worldPosition, validProjectorId)) {
            ProjectorRegistry.register(level, worldPosition, validProjectorId);
        }
    }

    private void allocateProjectorId(ServerLevel level) {
        String oldProjectorId = projectorId;
        projectorId = ProjectorIdSavedData.get(level).allocate();
        setChanged();
        ProjectorRegistry.unregister(level, worldPosition, oldProjectorId);
        ProjectorRegistry.register(level, worldPosition, projectorId);
    }

    private static Optional<String> normalizeProjectorId(String value) {
        if (value == null) return Optional.empty();
        String trimmed = value.trim();
        return ProjectorIdSavedData.isValidProjectorId(trimmed) ? Optional.of(trimmed) : Optional.empty();
    }

    private static void requireValidProjectorId(String value) {
        if (!ProjectorIdSavedData.isValidProjectorId(value)) {
            throw new IllegalArgumentException("Invalid projector id: " + value);
        }
    }
}
