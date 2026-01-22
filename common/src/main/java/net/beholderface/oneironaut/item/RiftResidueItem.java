package net.beholderface.oneironaut.item;

import at.petrak.hexcasting.api.addldata.ADPigment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.item.PigmentItem;
import at.petrak.hexcasting.api.pigment.ColorProvider;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.iotatypes.DimIota;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class RiftResidueItem extends Item implements IotaHolderItem, PigmentItem {
    public RiftResidueItem(Settings settings) {
        super(settings);
    }

    public int getMaxUseTime(ItemStack stack) {
        return 128;
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
        super.use(world, user, hand);
        ItemStack itemStack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        return TypedActionResult.consume(itemStack);
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user){
        if (!world.isClient && world != Oneironaut.getDeepNoosphere()){
            Vec3d newPos = MiscAPIKt.coerceWithinBorder(
                    MiscAPIKt.scaleBetweenDimensions(user.getPos(), world, Oneironaut.getDeepNoosphere()),
                    Oneironaut.getDeepNoosphere().getWorldBorder());
            user.teleport(Oneironaut.getDeepNoosphere(), newPos.x, 64.0, newPos.z, EnumSet.noneOf(PositionFlag.class), user.getYaw(), user.getPitch());
        }
        stack.decrement(1);
        return stack;
    }

    public UseAction getUseAction(ItemStack stack){
        return UseAction.EAT;
    }

    private static NbtCompound deepNooTag = null;
    @Override
    public @Nullable NbtCompound readIotaTag(ItemStack stack) {
        if (Oneironaut.getDeepNoosphere() != null){
            if (deepNooTag == null){
                deepNooTag = IotaType.serialize(new DimIota(Oneironaut.getDeepNoosphere()));
            }
            return deepNooTag.copy();
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

    @Override
    public void appendTooltip(ItemStack pStack, @Nullable World pLevel, List<Text> pTooltipComponents,
                              TooltipContext pIsAdvanced) {
        IotaHolderItem.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced);
    }

    protected static class SystemTimeColorProvider extends ColorProvider {
        private static final int[] colors = {0x080085, 0xff4c00, 0x8bd4ff, 0x8bd4ff, 0x8bd4ff, 0xce3d00, 0x080085, 0x090092};
        protected static final Vec3d vec = new Vec3d(1, 1, 1);
        @Override
        protected int getRawColor(float time, Vec3d position) {
            return ADPigment.morphBetweenColors(colors, vec, (float) ((System.currentTimeMillis() % 10000) / 10000.0) /*((System.currentTimeMillis() % 86400000.0) / 86400000.0)*/, Vec3d.ZERO);
        }
    }
    protected static SystemTimeColorProvider provider = new SystemTimeColorProvider();

    @Override
    public ColorProvider provideColor(ItemStack stack, UUID owner) {
        return provider;
    }
}
