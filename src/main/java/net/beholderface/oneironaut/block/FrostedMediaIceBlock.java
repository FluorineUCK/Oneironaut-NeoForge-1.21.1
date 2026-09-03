package net.beholderface.oneironaut.block;

import at.petrak.hexcasting.common.lib.HexBlocks;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FrostedMediaIceBlock extends FrostedIceBlock {
    public FrostedMediaIceBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    protected void melt(BlockState state, Level world, BlockPos pos) {
        if (world.dimensionType().ultraWarm()) {
            world.removeBlock(pos, false);
            return;
        }
        world.setBlockAndUpdate(pos, OneironautBlockRegistry.THOUGHT_SLURRY_BLOCK.get().defaultBlockState());
        world.neighborChanged(pos, OneironautBlockRegistry.THOUGHT_SLURRY_BLOCK.get(), pos);
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        super.playerDestroy(world, player, pos, state, blockEntity, stack);
        var silkTouch = world.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SILK_TOUCH);
        if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, stack) == 0) {
            if (world.dimensionType().ultraWarm()) {
                world.removeBlock(pos, false);
                return;
            }
            BlockState material = world.getBlockState(pos.below());
            if (material.blocksMotion() || material.liquid()) {
                world.setBlockAndUpdate(pos, OneironautBlockRegistry.THOUGHT_SLURRY_BLOCK.get().defaultBlockState());
            }
        }
    }
}
