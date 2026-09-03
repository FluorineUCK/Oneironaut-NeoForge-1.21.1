package net.beholderface.oneironaut.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static net.beholderface.oneironaut.item.MonkfruitItem.applyRumination;

public class MonkfruitItemCooked extends Item {
    public static final int DEFAULT_DURATION_COOKED = 80;
    public MonkfruitItemCooked(net.minecraft.world.item.Item.Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (user instanceof Player player){
            applyRumination(player, DEFAULT_DURATION_COOKED, 0);
        }
        return super.finishUsingItem(stack, world, user);
    }
}
