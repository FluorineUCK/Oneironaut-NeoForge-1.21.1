package net.beholderface.oneironaut.item;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.iotatypes.DimIota;
import net.beholderface.oneironaut.network.SpoopyScreamPacket;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

public class RiftResidueItem extends ArbitaryDeltaPigmentItem implements IotaHolderItem {
    public RiftResidueItem(net.minecraft.world.item.Item.Properties settings, int[] colors, Supplier<Double> deltaGetter) {
        super(settings, colors, deltaGetter);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 128;
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand){
        super.use(world, user, hand);
        ItemStack itemStack = user.getItemInHand(hand);
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(itemStack);
    }

    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user){
        if (!world.isClientSide && world != Oneironaut.getDeepNoosphere()){
            Vec3 newPos = MiscAPIKt.coerceWithinBorder(
                    MiscAPIKt.scaleBetweenDimensions(user.position(), world, Oneironaut.getDeepNoosphere()),
                    Oneironaut.getDeepNoosphere().getWorldBorder());
            user.teleportTo(Oneironaut.getDeepNoosphere(), newPos.x, 64.0, newPos.z, EnumSet.noneOf(RelativeMovement.class), user.getYRot(), user.getXRot());
        }
        if ((user instanceof Player player && !player.isCreative()) || !(user instanceof Player)){
            stack.shrink(1);
        }
        return stack;
    }

    public UseAnim getUseAnimation(ItemStack stack){
        return UseAnim.EAT;
    }

    @Override
    public @Nullable Iota readIota(ItemStack stack) {
        return new DimIota("oneironaut:deep_noosphere");
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
    public void appendHoverText(ItemStack pStack, net.minecraft.world.item.Item.TooltipContext context,
                                List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        IotaHolderItem.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public void onDestroyed(ItemEntity entity) {
        Level world = entity.level();
        if (!world.isClientSide && world instanceof ServerLevel serverWorld){
            float pitch = 0.75f + (world.random.nextFloat() / 2);
            IXplatAbstractions.INSTANCE.sendPacketNear(entity.position(), 16.0, serverWorld, new SpoopyScreamPacket(SoundEvents.FOX_SCREECH, pitch));
        }
    }
}
