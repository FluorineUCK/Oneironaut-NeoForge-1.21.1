package net.beholderface.oneironaut.item;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import net.beholderface.oneironaut.hexcompat.HexCodecCompat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/** A block item whose written iota is copied into the placed block entity. */
public class WriteableBlockItem extends BlockItem implements IotaHolderItem {
    public static final String TAG_IOTA = "iota";

    public final Function<Iota, Boolean> acceptsIota;

    public WriteableBlockItem(Block block, Properties settings, Function<Iota, Boolean> acceptsIota) {
        super(block, settings);
        this.acceptsIota = acceptsIota;
    }

    @Override
    public @Nullable Iota readIota(ItemStack stack) {
        Iota componentIota = stack.get(HexDataComponents.IOTA_HOLDER_IOTA.get());
        if (componentIota != null) {
            return componentIota;
        }

        CustomData blockEntityData = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
        Iota decoded = HexCodecCompat.decode(blockEntityData.copyTag().get(TAG_IOTA));
        return decoded != null ? decoded : new NullIota();
    }

    @Override
    public boolean writeable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canWrite(ItemStack stack, @Nullable Iota iota) {
        return acceptsIota.apply(iota);
    }

    @Override
    public void writeDatum(ItemStack stack, @Nullable Iota iota) {
        Iota stored = iota != null ? iota : new NullIota();
        stack.set(HexDataComponents.IOTA_HOLDER_IOTA.get(), stored);

        CompoundTag blockEntityTag = stack
                .getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY)
                .copyTag();
        blockEntityTag.put(TAG_IOTA, HexCodecCompat.encode(stored));
        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(blockEntityTag));
    }
}
