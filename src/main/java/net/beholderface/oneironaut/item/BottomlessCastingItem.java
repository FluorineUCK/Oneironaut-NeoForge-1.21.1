package net.beholderface.oneironaut.item;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.utils.MediaHelper;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;

public class BottomlessCastingItem extends ItemPackagedHex {
    public BottomlessCastingItem(net.minecraft.world.item.Item.Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean breakAfterDepletion() {
        return false;
    }

    @Override
    public boolean canDrawMediaFromInventory(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canRecharge(ItemStack stack) {
        return false;
    }

    @Override
    public long getMedia(ItemStack stack) {
        return MediaConstants.DUST_UNIT / 10;
    }

    @Override
    public long getMaxMedia(ItemStack stack) {
        return Long.MAX_VALUE;
    }

    @Override
    public void setMedia(ItemStack stack, long media) {
        //no-op
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return this.hasHex(pStack);
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return Mth.hsvToRgb((Util.getMillis() % 5000L) / 5000.0F, 1.0F, 1.0F);
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        return 13;
    }

    @Override
    public void writeHex(ItemStack stack, List<Iota> program, @Nullable FrozenPigment pigment, long media) {
        stack.set(HexDataComponents.HEX_HOLDER_PATTERNS.get(), List.copyOf(program));
        if (pigment != null) {
            stack.set(HexDataComponents.PIGMENT.get(), pigment);
        } else {
            stack.remove(HexDataComponents.PIGMENT.get());
        }
    }

    public int cooldown(){
        return 5;
    }

    //why are these private in ItemMediaHolder anyway?
    public static final DecimalFormat DUST_AMOUNT = new DecimalFormat("###,###.##");
    public static final DecimalFormat PERCENTAGE = new DecimalFormat("####");


    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext context, List<Component> pTooltipComponents,
                                TooltipFlag pIsAdvanced) {
            long media = MediaConstants.DUST_UNIT / 10;

            TextColor color = TextColor.fromRgb(MediaHelper.mediaBarColor(media, Long.MAX_VALUE));

            MutableComponent mediamount = Component.literal(DUST_AMOUNT.format(media / (float) MediaConstants.DUST_UNIT));
            MutableComponent percentFull = Component.literal(PERCENTAGE.format(0) + "%");
            //infinity!
            MutableComponent maxCapacity = Component.nullToEmpty("∞").copy();

            mediamount.withStyle(style -> style.withColor(HEX_COLOR));
            maxCapacity.withStyle(style -> style.withColor(HEX_COLOR));
            percentFull.withStyle(style -> style.withColor(color));

            pTooltipComponents.add(
                    Component.translatable("hexcasting.tooltip.media_amount.advanced",
                            mediamount, maxCapacity, percentFull));
    }
}
