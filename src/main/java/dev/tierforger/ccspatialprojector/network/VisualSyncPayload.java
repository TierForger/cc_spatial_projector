package dev.tierforger.ccspatialprojector.network;

import dev.tierforger.ccspatialprojector.CcSpatialProjector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record VisualSyncPayload(CompoundTag tag) implements CustomPacketPayload {
    public static final Type<VisualSyncPayload> TYPE = new Type<>(CcSpatialProjector.id("visual_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VisualSyncPayload> STREAM_CODEC = StreamCodec.ofMember(
        VisualSyncPayload::write,
        VisualSyncPayload::read
    );

    public static VisualSyncPayload read(RegistryFriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        return new VisualSyncPayload(tag == null ? new CompoundTag() : tag);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeNbt(tag);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
