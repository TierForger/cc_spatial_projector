package dev.tierforger.ccspatialprojector.registry;

import dev.tierforger.ccspatialprojector.CcSpatialProjector;
import dev.tierforger.ccspatialprojector.block.SpatialProjectorBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CcSpatialProjector.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpatialProjectorBlockEntity>> SPATIAL_PROJECTOR =
        BLOCK_ENTITY_TYPES.register("spatial_projector", () ->
            BlockEntityType.Builder.of(SpatialProjectorBlockEntity::new, ModBlocks.SPATIAL_PROJECTOR.get()).build(null)
        );
}
