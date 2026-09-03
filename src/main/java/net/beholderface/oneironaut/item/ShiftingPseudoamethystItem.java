package net.beholderface.oneironaut.item;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import dev.architectury.platform.Platform;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.OneironautClient;
import net.beholderface.oneironaut.casting.iotatypes.DimIota;
import net.beholderface.oneironaut.network.SpoopyScreamPacket;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShiftingPseudoamethystItem extends Item {
    public ShiftingPseudoamethystItem(net.minecraft.world.item.Item.Properties settings) {
        super(settings);
    }

    @Override
    public void onDestroyed(ItemEntity entity) {
        Level world = entity.level();
        if (!world.isClientSide && world instanceof ServerLevel serverWorld){
            float pitch = 0.75f + (world.random.nextFloat() / 2);
            IXplatAbstractions.INSTANCE.sendPacketNear(entity.position(), 16.0, serverWorld, new SpoopyScreamPacket(SoundEvents.FOX_SCREECH, pitch));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext tooltipContext,
                                List<Component> pTooltipComponents, TooltipFlag context){
        super.appendHoverText(stack, tooltipContext, pTooltipComponents, context);
        Level world = tooltipContext.level();
        if (world != null && world.isClientSide){
            OneironautClient.lastShiftingHoverTick = world.getGameTime();
            if (OneironautClient.lastHoveredShifting != stack){
                OneironautClient.lastHoveredShifting = stack;
            }
        }
    }
}
