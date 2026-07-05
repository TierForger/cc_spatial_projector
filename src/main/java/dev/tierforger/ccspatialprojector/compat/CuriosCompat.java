package dev.tierforger.ccspatialprojector.compat;

import dev.tierforger.ccspatialprojector.util.GoggleLink;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

public final class CuriosCompat {
    private CuriosCompat() {}

    /**
     * Optional Curios integration. Only a Curios slot with identifier "head"
     * acts like equipped goggles and subscribes to projector visuals.
     */
    public static Optional<ItemStack> findHeadGoggles(Player player) {
        if (!ModList.get().isLoaded("curios")) return Optional.empty();

        return CuriosApi.getCuriosInventory(player)
            .stream()
            .flatMap(handler -> handler.findCurios(GoggleLink::isGoggles).stream())
            .filter(CuriosCompat::isHeadSlot)
            .map(SlotResult::stack)
            .findFirst();
    }

    private static boolean isHeadSlot(SlotResult result) {
        return "head".equals(result.slotContext().identifier());
    }
}
