package dev.tierforger.ccspatialprojector.util;

import dev.tierforger.ccspatialprojector.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class GoggleLink {
    private GoggleLink() {}

    private static final String BOUND_PROJECTOR = "bound_projector";

    public static boolean isGoggles(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItems.SPATIAL_GOGGLES.get());
    }

    public static void bind(ItemStack stack, ProjectorKey key) {
        if (!isGoggles(stack)) return;
        StackData.putCompound(stack, BOUND_PROJECTOR, key.toTag());
    }

    public static void clear(ItemStack stack) {
        if (!isGoggles(stack)) return;
        StackData.remove(stack, BOUND_PROJECTOR);
    }

    public static Optional<ProjectorKey> boundProjector(ItemStack stack) {
        if (!isGoggles(stack)) return Optional.empty();
        return StackData.compound(stack, BOUND_PROJECTOR).flatMap(ProjectorKey::tryFromTag);
    }

    public static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        Optional<ProjectorKey> key = boundProjector(stack);
        if (key.isEmpty()) {
            tooltip.add(Component.translatable("item.cc_spatial_projector.spatial_goggles.unbound").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.cc_spatial_projector.spatial_goggles.bind_hint").withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.translatable("item.cc_spatial_projector.spatial_goggles.projector_id", key.get().id()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.cc_spatial_projector.spatial_goggles.unlink_hint").withStyle(ChatFormatting.GRAY));
    }
}
