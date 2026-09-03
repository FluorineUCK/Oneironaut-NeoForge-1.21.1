package net.beholderface.oneironaut.network;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import ram.talia.hexal.api.FunUtilsKt;

public record ParticleBurstPacket(
    Vec3 origin,
    Vec3 direction,
    double posRandom,
    double speedRandom,
    FrozenPigment color,
    int quantity,
    boolean actuallySound
) implements CustomPacketPayload {
    public static final Type<ParticleBurstPacket> TYPE = new Type<>(Oneironaut.id("particleburst"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleBurstPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public @NotNull ParticleBurstPacket decode(RegistryFriendlyByteBuf buf) {
                Vec3 origin = readVec(buf);
                Vec3 direction = readVec(buf);
                double posRandom = buf.readDouble();
                double speedRandom = buf.readDouble();
                FrozenPigment pigment = FrozenPigment.STREAM_CODEC.decode(buf);
                return new ParticleBurstPacket(origin, direction, posRandom, speedRandom,
                    pigment, buf.readVarInt(), buf.readBoolean());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, ParticleBurstPacket payload) {
                writeVec(buf, payload.origin);
                writeVec(buf, payload.direction);
                buf.writeDouble(payload.posRandom);
                buf.writeDouble(payload.speedRandom);
                FrozenPigment.STREAM_CODEC.encode(buf, payload.color);
                buf.writeVarInt(payload.quantity);
                buf.writeBoolean(payload.actuallySound);
            }
        };

    private static Vec3 readVec(RegistryFriendlyByteBuf buf) {
        return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    private static void writeVec(RegistryFriendlyByteBuf buf, Vec3 vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }

    public void handleClient(Player receiver) {
        Level level = receiver.level();
        RandomSource random = level.random;
        int renderedColor = FunUtilsKt.nextColour(color, random);
        int count = Math.max(1, quantity);
        if (!actuallySound) {
            for (int i = 0; i < count; i++) {
                Vec3 pos = origin.add(random.nextGaussian() * posRandom,
                    random.nextGaussian() * posRandom, random.nextGaussian() * posRandom);
                Vec3 speed = direction.add(random.nextGaussian() * speedRandom,
                    random.nextGaussian() * speedRandom, random.nextGaussian() * speedRandom);
                level.addParticle(new ConjureParticleOptions(renderedColor),
                    pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
            }
        } else {
            level.playLocalSound(origin.x, origin.y, origin.z, HexSounds.CASTING_AMBIANCE.value(),
                SoundSource.MASTER, 1f, 1f, false);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
