package net.beholderface.oneironaut.network;

import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ItemUpdatePacket(ItemStack stack, int entityId) implements CustomPacketPayload {
    public static final Type<ItemUpdatePacket> TYPE = new Type<>(Oneironaut.id("itemupdate"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemUpdatePacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public ItemUpdatePacket decode(RegistryFriendlyByteBuf buf) {
                return new ItemUpdatePacket(ItemStack.STREAM_CODEC.decode(buf), buf.readVarInt());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, ItemUpdatePacket payload) {
                ItemStack.STREAM_CODEC.encode(buf, payload.stack);
                buf.writeVarInt(payload.entityId);
            }
        };

    public ItemUpdatePacket(ItemStack stack, @Nullable Entity entity) {
        this(stack.copy(), entity == null ? -1 : entity.getId());
    }

    public void handleClient(Player receiver) {
        Entity entity = receiver.level().getEntity(entityId);
        if (entity instanceof ItemFrame frame) frame.setItem(stack.copy());
        else if (entity instanceof ItemEntity item) item.setItem(stack.copy());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
