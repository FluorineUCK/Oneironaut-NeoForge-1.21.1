package net.beholderface.oneironaut.item;

import net.beholderface.oneironaut.casting.OvercastDamageEnchant;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class MindScalpelItem extends Item {
    public MindScalpelItem(net.minecraft.world.item.Item.Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity target, InteractionHand hand) {
        ItemCooldowns cooldownManager = user.getCooldowns();
        if (!cooldownManager.isOnCooldown(this)){
            OvercastDamageEnchant.applyMindDamage(user, target, 2, true);
            target.hurt(target.damageSources().playerAttack(user), 0);
            user.swing(hand);
            cooldownManager.addCooldown(this, 15);
            user.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player){
            this.interactLivingEntity(stack, player, target, InteractionHand.MAIN_HAND);
        }
        return false;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
        return !miner.isCreative();
    }
}
