package net.beholderface.oneironaut.status;

import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.api.misc.MediaConstants;
import net.beholderface.oneironaut.casting.lichdom.LichData;
import net.beholderface.oneironaut.casting.lichdom.LichdomManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;

public class MonkfruitDelayEffect extends StatusEffect {
    public MonkfruitDelayEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xa1b25e);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier){
        return duration % 20 == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier){
        if (entity instanceof ServerPlayerEntity player){
            ServerWorld world = (ServerWorld) entity.getWorld();
            double maxGaussian = 2.0;
            double rawGaussian = world.random.nextGaussian();
            double gaussian = Math.min(Math.max((rawGaussian * (maxGaussian / 2)) + (maxGaussian / 2), 0), maxGaussian);
            double overallReleased = gaussian + 1;
            List<ItemStack> mediaHolders = new ArrayList<>();
            for (ItemStack checkedStack : player.getInventory().main){
                if (checkedStack.getItem() instanceof MediaHolderItem battery){
                    if (battery.canRecharge(checkedStack) && battery.getMaxMedia(checkedStack) != battery.getMedia(checkedStack)){
                        mediaHolders.add(checkedStack);
                    }
                }
            }
            if (player.getStackInHand(Hand.OFF_HAND).getItem() instanceof MediaHolderItem battery){
                if (battery.canRecharge(player.getStackInHand(Hand.OFF_HAND))){
                    mediaHolders.add(player.getStackInHand(Hand.OFF_HAND));
                }
            }
            boolean isLich = LichdomManager.isPlayerLich(player);
            int quantity = mediaHolders.size() + (isLich ? 1 : 0);
            double multiplier = 1.0 + (amplifier / 4.0);
            long inserted = (long) (((overallReleased / quantity) * (multiplier)) * MediaConstants.DUST_UNIT);
            for (ItemStack battery : mediaHolders){
                MediaHolderItem type = (MediaHolderItem) battery.getItem();
                type.insertMedia(battery, inserted, false);
            }
            if (isLich){
                LichData data = LichdomManager.getLichData(player);
                data.insertMedia(inserted, false);
            }
        }
    }
}
