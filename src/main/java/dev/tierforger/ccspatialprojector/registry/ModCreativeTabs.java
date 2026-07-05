package dev.tierforger.ccspatialprojector.registry;

import dev.tierforger.ccspatialprojector.CcSpatialProjector;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CcSpatialProjector.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.cc_spatial_projector.main"))
            .icon(() -> new ItemStack(ModItems.SPATIAL_GOGGLES.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.SPATIAL_PROJECTOR.get());
                output.accept(ModItems.SPATIAL_GOGGLES.get());
            })
            .build()
    );
}
