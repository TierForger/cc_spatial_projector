package dev.tierforger.ccspatialprojector.network;

import dev.tierforger.ccspatialprojector.CcSpatialProjector;
import dev.tierforger.ccspatialprojector.client.ClientPayloadBridge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private ModNetworking() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(CcSpatialProjector.MOD_ID).versioned("1");
        registrar.playToClient(VisualSyncPayload.TYPE, VisualSyncPayload.STREAM_CODEC, (payload, context) -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                context.enqueueWork(() -> ClientPayloadBridge.apply(payload));
            }
        });
    }
}
