package dev.tierforger.ccspatialprojector.util;

import dev.tierforger.ccspatialprojector.compat.CuriosCompat;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves where goggles can participate in gameplay.
 * Only equipped goggles subscribe to visuals; any bound goggles in inventory can
 * receive clean-up syncs when their projector changes or is removed.
 */
public final class GoggleAccess {
    private GoggleAccess() {}

    public static Optional<ItemStack> equippedGoggles(Player player) {
        Optional<ItemStack> vanillaHead = vanillaHead(player);
        return vanillaHead.isPresent() ? vanillaHead : CuriosCompat.findHeadGoggles(player);
    }

    public static Optional<ProjectorKey> activeSource(Player player) {
        return equippedGoggles(player).flatMap(GoggleLink::boundProjector);
    }

    public static boolean isSubscribed(Player player, ProjectorKey source) {
        return activeSource(player).map(source::equals).orElse(false);
    }

    public static boolean hasAnyBoundGoggles(Player player, ProjectorKey source) {
        return boundSources(player).contains(source);
    }

    public static Set<ProjectorKey> boundSources(Player player) {
        Set<ProjectorKey> result = new LinkedHashSet<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            GoggleLink.boundProjector(player.getInventory().getItem(slot)).ifPresent(result::add);
        }
        equippedGoggles(player).flatMap(GoggleLink::boundProjector).ifPresent(result::add);
        GoggleLink.boundProjector(player.getMainHandItem()).ifPresent(result::add);
        GoggleLink.boundProjector(player.getOffhandItem()).ifPresent(result::add);
        return result;
    }

    private static Optional<ItemStack> vanillaHead(Player player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.HEAD);
        return GoggleLink.isGoggles(stack) ? Optional.of(stack) : Optional.empty();
    }
}
