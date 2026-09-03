package net.beholderface.oneironaut.network;

import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public record UnBrainsweepPacket(int patientID) implements CustomPacketPayload {
    public static final Type<UnBrainsweepPacket> TYPE = new Type<>(Oneironaut.id("unbrainsweep"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnBrainsweepPacket> STREAM_CODEC =
        StreamCodec.composite(ByteBufCodecs.VAR_INT, UnBrainsweepPacket::patientID, UnBrainsweepPacket::new);

    public void handleClient(Player receiver) {
        if (receiver.level().getEntity(patientID) instanceof Mob patient) {
            MiscAPIKt.unbrainsweep(patient);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
