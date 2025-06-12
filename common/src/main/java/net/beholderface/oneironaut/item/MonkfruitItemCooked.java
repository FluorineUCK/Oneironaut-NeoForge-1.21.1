package net.beholderface.oneironaut.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import static net.beholderface.oneironaut.item.MonkfruitItem.applyRumination;

public class MonkfruitItemCooked extends Item {
    public static final int DEFAULT_DURATION_COOKED = 80;
    public MonkfruitItemCooked(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player){
            applyRumination(player, DEFAULT_DURATION_COOKED, 0);
        }
        return user.eatFood(world, stack);
    }
}
