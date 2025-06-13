package net.beholderface.oneironaut.item;

import net.beholderface.oneironaut.registry.OneironautMiscRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class MonkfruitItem extends AliasedBlockItem {
    public static final int DEFAULT_DURATION_RAW = 100;
    public MonkfruitItem(Block block, Settings settings) {
        super(block, settings);
    }

    public static void applyRumination(PlayerEntity player, int duration, int amplifier){
        var effects = player.getActiveStatusEffects();
        StatusEffect rumination = OneironautMiscRegistry.RUMINATION.get();
        if (effects.containsKey(rumination)){
            StatusEffectInstance instance = effects.get(rumination);
            //adjust preexisting duration if changing effect level
            int adjustedDuration = instance.getAmplifier() != amplifier ? (int) ((double)instance.getDuration() * ((((double)instance.getAmplifier() / 4.0) + 1.0) / (((double) amplifier / 4.0) + 1.0))) : instance.getDuration();
            effects.put(rumination, new StatusEffectInstance(rumination, adjustedDuration + duration, amplifier, false, false, true));
        } else {
            player.addStatusEffect(new StatusEffectInstance(rumination, duration, amplifier, false, false, true));
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player){
            applyRumination(player, DEFAULT_DURATION_RAW, 0);
        }
        return user.eatFood(world, stack);
    }
}
