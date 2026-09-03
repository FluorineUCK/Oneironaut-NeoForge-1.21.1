package net.beholderface.oneironaut.item;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.items.storage.ItemFocus;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.iotatypes.DimIota;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemLibraryCard extends Item implements IotaHolderItem {
    public ItemLibraryCard(net.minecraft.world.item.Item.Properties settings) {
        super(settings);
    }

    @Nullable
    public ResourceKey<Level> getDimension(ItemStack stack){
        Iota stored = stack.get(HexDataComponents.IOTA_HOLDER_IOTA.get());
        return stored instanceof DimIota dim ? dim.getWorldKey() : null;
    }

    @Override
    public boolean writeable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canWrite(ItemStack stack, @Nullable Iota iota) {
        return iota instanceof DimIota || iota == null;
    }

    @Override
    public void writeDatum(ItemStack stack, @Nullable Iota iota) {
        if (iota == null){
            stack.remove(HexDataComponents.IOTA_HOLDER_IOTA.get());
        } else {
            stack.set(HexDataComponents.IOTA_HOLDER_IOTA.get(), iota);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext context,
                                List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        IotaHolderItem.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced);
    }
}
