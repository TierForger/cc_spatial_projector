package dev.tierforger.ccspatialprojector.server;

import dev.tierforger.ccspatialprojector.CcSpatialProjector;
import dev.tierforger.ccspatialprojector.util.ProjectorKey;
import dev.tierforger.ccspatialprojector.util.GoggleAccess;
import dev.tierforger.ccspatialprojector.visual.ServerVisualStore;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = CcSpatialProjector.MOD_ID)
public final class ServerSubscriptionTicker {
    private ServerSubscriptionTicker() {}

    private static final Map<UUID, ProjectorKey> LAST_ACTIVE_SOURCE = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;

        Optional<ProjectorKey> current = GoggleAccess.activeSource(player);
        ProjectorKey previous = LAST_ACTIVE_SOURCE.get(player.getUUID());
        ProjectorKey next = current.orElse(null);

        if (!Objects.equals(previous, next)) {
            if (next == null) LAST_ACTIVE_SOURCE.remove(player.getUUID());
            else LAST_ACTIVE_SOURCE.put(player.getUUID(), next);
            ServerVisualStore.syncCurrentTo(player);
        }
    }
}
