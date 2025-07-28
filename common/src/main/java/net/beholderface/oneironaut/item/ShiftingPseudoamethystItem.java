package net.beholderface.oneironaut.item;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.OneironautClient;
import net.beholderface.oneironaut.casting.iotatypes.DimIota;
import net.beholderface.oneironaut.network.SpoopyScreamPacket;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShiftingPseudoamethystItem extends Item implements IotaHolderItem {
    public ShiftingPseudoamethystItem(Settings settings) {
        super(settings);
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        World world = entity.getWorld();
        if (!world.isClient && world instanceof ServerWorld serverWorld){
            float pitch = 0.75f + (world.random.nextFloat() / 2);
            IXplatAbstractions.INSTANCE.sendPacketNear(entity.getPos(), 16.0, serverWorld, new SpoopyScreamPacket(SoundEvents.ENTITY_FOX_SCREECH, pitch));
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> pTooltipComponents, TooltipContext context){
        if (world != null && world.isClient){
            OneironautClient.lastShiftingHoverTick = world.getTime();
            if (OneironautClient.lastHoveredShifting != stack){
                OneironautClient.lastHoveredShifting = stack;
            }
        }
    }

    @Override
    public @Nullable NbtCompound readIotaTag(ItemStack stack) {
        if (Oneironaut.getDeepNoosphere() != null){
            return IotaType.serialize(new DimIota(Oneironaut.getDeepNoosphere()));
        }
        return null;
    }

    @Override
    public boolean writeable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canWrite(ItemStack stack, @Nullable Iota iota) {
        return false;
    }

    @Override
    public void writeDatum(ItemStack stack, @Nullable Iota iota) {

    }
}
