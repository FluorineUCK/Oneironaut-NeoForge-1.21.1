package net.beholderface.oneironaut.item;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class WriteableBlockItem extends BlockItem implements IotaHolderItem {

    public final Function<Iota, Boolean> acceptsIota;

    public WriteableBlockItem(Block block, Settings settings, Function<Iota, Boolean> acceptsIota) {
        super(block, settings);
        this.acceptsIota = acceptsIota;
    }

    public static final String TAG_IOTA = "iota";

    @Override
    public @Nullable NbtCompound readIotaTag(ItemStack stack) {
        NbtCompound blockEntityNBT = stack.getOrCreateNbt().getCompound(BlockItem.BLOCK_ENTITY_TAG_KEY);
        if (blockEntityNBT != null){
            NbtCompound iotaNBT = blockEntityNBT.getCompound(TAG_IOTA);
            if (iotaNBT != null){
                return iotaNBT;
            }
        }
        return IotaType.serialize(new NullIota());
    }

    @Override
    public boolean writeable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canWrite(ItemStack stack, @Nullable Iota iota) {
        return this.acceptsIota.apply(iota);
    }

    @Override
    public void writeDatum(ItemStack stack, @Nullable Iota iota) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtCompound blockEntityNBT = nbt.getCompound(BlockItem.BLOCK_ENTITY_TAG_KEY);
        NBTHelper.putCompound(blockEntityNBT, TAG_IOTA, IotaType.serialize(iota != null ? iota : new NullIota()));
        NBTHelper.putCompound(nbt, BlockItem.BLOCK_ENTITY_TAG_KEY, blockEntityNBT);
    }
}
