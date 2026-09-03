package net.beholderface.oneironaut.mixin;

import at.petrak.hexcasting.forge.xplat.ForgeXplatImpl;
import net.beholderface.oneironaut.registry.OneironautTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.Oneironaut;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


//this should have a significantly wider-reaching effect
@Mixin(ForgeXplatImpl.class)
public abstract class OpBreakBlockImmunityMixin {

    @Inject(method = "isBreakingAllowed", at = @At(value = "HEAD", remap = false), remap = false, cancellable = true)
    public void dontBreakIfImmune(ServerLevel world, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<Boolean> cir){
        if (state.is(OneironautTags.Blocks.breakImmune)){
            cir.setReturnValue(false);
        }
    }
}
