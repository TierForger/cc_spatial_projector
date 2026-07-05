package dev.tierforger.ccspatialprojector.client;

import dev.tierforger.ccspatialprojector.network.VisualSyncPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientPayloadBridge {
    private ClientPayloadBridge() {}

    public static void apply(VisualSyncPayload payload) {
        ClientVisualStore.apply(payload.tag());
    }
}
