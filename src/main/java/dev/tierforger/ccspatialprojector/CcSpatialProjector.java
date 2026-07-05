package dev.tierforger.ccspatialprojector;

import dan200.computercraft.api.peripheral.PeripheralCapability;
import dev.tierforger.ccspatialprojector.config.SpatialProjectorConfig;
import dev.tierforger.ccspatialprojector.network.ModNetworking;
import dev.tierforger.ccspatialprojector.registry.ModArmorMaterials;
import dev.tierforger.ccspatialprojector.registry.ModBlockEntities;
import dev.tierforger.ccspatialprojector.registry.ModBlocks;
import dev.tierforger.ccspatialprojector.registry.ModCreativeTabs;
import dev.tierforger.ccspatialprojector.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(CcSpatialProjector.MOD_ID)
public final class CcSpatialProjector {
    public static final String MOD_ID = "cc_spatial_projector";

    public CcSpatialProjector(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, SpatialProjectorConfig.SPEC, "cc-spatial-projector-common.toml");

        ModArmorMaterials.ARMOR_MATERIALS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modBus);
        ModCreativeTabs.CREATIVE_TABS.register(modBus);

        modBus.addListener(CcSpatialProjector::registerCapabilities);
        modBus.addListener(ModNetworking::registerPayloads);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            PeripheralCapability.get(),
            ModBlockEntities.SPATIAL_PROJECTOR.get(),
            (blockEntity, context) -> blockEntity.peripheral()
        );
    }
}
