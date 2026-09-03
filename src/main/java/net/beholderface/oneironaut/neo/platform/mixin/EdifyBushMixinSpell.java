package net.beholderface.oneironaut.neo.platform.mixin;

import at.petrak.hexcasting.common.casting.actions.spells.OpEdifySapling;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = OpEdifySapling.class, remap = false)
public class EdifyBushMixinSpell {
    @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", remap = false), remap = false)
    public boolean allowBush(BlockState state, TagKey<Block> tagKey, Operation<Boolean> original){
        if (state.getBlock() == Blocks.SWEET_BERRY_BUSH){
            return true;
        }
        return original.call(state, tagKey);
    }
}
