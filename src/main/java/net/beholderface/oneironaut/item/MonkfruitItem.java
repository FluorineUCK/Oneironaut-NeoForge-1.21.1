package net.beholderface.oneironaut.item;

import net.beholderface.oneironaut.registry.OneironautMiscRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MonkfruitItem extends ItemNameBlockItem {
    public static final int DEFAULT_DURATION_RAW = 100;
    public MonkfruitItem(Block block, net.minecraft.world.item.Item.Properties settings) {
        super(block, settings);
    }

    public static void applyRumination(Player player, int duration, int amplifier){
        var rumination = OneironautMiscRegistry.RUMINATION;
        MobEffectInstance instance = player.getEffect(rumination);
        if (instance != null){
            //adjust preexisting duration if changing effect level
            int adjustedDuration = instance.getAmplifier() != amplifier ? (int) ((double)instance.getDuration() * ((((double)instance.getAmplifier() / 4.0) + 1.0) / (((double) amplifier / 4.0) + 1.0))) : instance.getDuration();
            player.addEffect(new MobEffectInstance(rumination, adjustedDuration + duration, amplifier, false, false, true));
        } else {
            player.addEffect(new MobEffectInstance(rumination, duration, amplifier, false, false, true));
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (user instanceof Player player){
            applyRumination(player, DEFAULT_DURATION_RAW, 0);
        }
        return super.finishUsingItem(stack, world, user);
    }
}
