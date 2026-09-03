package net.beholderface.oneironaut.network;

import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record SpoopyScreamPacket(SoundEvent sound, float pitch) implements CustomPacketPayload {
    public static final Type<SpoopyScreamPacket> TYPE = new Type<>(Oneironaut.id("scream"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpoopyScreamPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SpoopyScreamPacket decode(RegistryFriendlyByteBuf buf) {
                ResourceLocation id = buf.readResourceLocation();
                return new SpoopyScreamPacket(BuiltInRegistries.SOUND_EVENT.get(id), buf.readFloat());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, SpoopyScreamPacket payload) {
                buf.writeResourceLocation(BuiltInRegistries.SOUND_EVENT.getKey(payload.sound));
                buf.writeFloat(payload.pitch);
            }
        };

    public void handleClient(Player receiver) {
        Vec3 pos = receiver.getEyePosition().subtract(receiver.getLookAngle());
        receiver.level().playLocalSound(pos.x, pos.y, pos.z, sound, SoundSource.HOSTILE, 3f, pitch, true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
