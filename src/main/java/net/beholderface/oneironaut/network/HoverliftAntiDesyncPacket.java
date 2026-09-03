package net.beholderface.oneironaut.network;

import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class HoverliftAntiDesyncPacket implements CustomPacketPayload {
    public static final Type<HoverliftAntiDesyncPacket> TYPE = new Type<>(Oneironaut.id("hoverliftdesync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HoverliftAntiDesyncPacket> STREAM_CODEC =
        StreamCodec.unit(new HoverliftAntiDesyncPacket());

    public void handleClient(Player receiver) {
        if (receiver.hasEffect(MobEffects.SLOW_FALLING)) receiver.removeEffect(MobEffects.SLOW_FALLING);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
