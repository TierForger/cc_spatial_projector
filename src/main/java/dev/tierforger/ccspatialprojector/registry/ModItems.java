package dev.tierforger.ccspatialprojector.registry;

import dev.tierforger.ccspatialprojector.CcSpatialProjector;
import dev.tierforger.ccspatialprojector.item.SpatialGogglesItem;
import dev.tierforger.ccspatialprojector.item.SpatialProjectorBlockItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CcSpatialProjector.MOD_ID);

    public static final DeferredItem<SpatialProjectorBlockItem> SPATIAL_PROJECTOR = ITEMS.register("spatial_projector", () ->
        new SpatialProjectorBlockItem(ModBlocks.SPATIAL_PROJECTOR.get(), new Item.Properties())
    );

    public static final DeferredItem<Item> SPATIAL_GOGGLES = ITEMS.register("spatial_goggles", () ->
        new SpatialGogglesItem(new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(5)))
    );
}
