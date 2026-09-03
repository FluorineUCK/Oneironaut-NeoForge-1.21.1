package net.beholderface.oneironaut.network;

import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/** Synchronises a redirected projectile's motion to tracking clients. */
public record FireballUpdatePacket(Vec3 targetVelocity, int entityId) implements CustomPacketPayload {
    public static final Type<FireballUpdatePacket> TYPE = new Type<>(Oneironaut.id("fireballupdate"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FireballUpdatePacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public @NotNull FireballUpdatePacket decode(RegistryFriendlyByteBuf buf) {
                return new FireballUpdatePacket(
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    buf.readVarInt());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, FireballUpdatePacket payload) {
                buf.writeDouble(payload.targetVelocity.x);
                buf.writeDouble(payload.targetVelocity.y);
                buf.writeDouble(payload.targetVelocity.z);
                buf.writeVarInt(payload.entityId);
            }
        };

    public FireballUpdatePacket(Vec3 targetVelocity, AbstractHurtingProjectile entity) {
        this(targetVelocity, entity.getId());
    }

    public void handleClient(Player receiver) {
        Entity entity = receiver.level().getEntity(entityId);
        if (entity instanceof AbstractHurtingProjectile projectile) {
            projectile.setDeltaMovement(targetVelocity);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
