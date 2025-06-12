package net.beholderface.oneironaut.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class MonkfruitItemJam extends Item {
    public static final int DEFAULT_DURATION_JAM = 100;
    public static final int DEFAULT_AMPLIFIER_JAM = 2;
    public MonkfruitItemJam(Settings settings) {
        super(settings);
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        super.finishUsing(stack, world, user);

        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        } else {
            if (user instanceof PlayerEntity player) {
                MonkfruitItem.applyRumination(player, DEFAULT_DURATION_JAM, DEFAULT_AMPLIFIER_JAM);
                if(!(player).getAbilities().creativeMode){
                    ItemStack itemStack = new ItemStack(Items.GLASS_BOTTLE);
                    if (!player.getInventory().insertStack(itemStack)) {
                        player.dropItem(itemStack, false);
                    }
                }
            }
            return stack;
        }
    }
}
