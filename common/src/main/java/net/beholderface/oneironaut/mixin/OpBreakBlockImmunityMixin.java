package net.beholderface.oneironaut.mixin;

import at.petrak.hexcasting.fabric.xplat.FabricXplatImpl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.Oneironaut;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


//this should have a significantly wider-reaching effect
@Mixin(FabricXplatImpl.class)
public abstract class OpBreakBlockImmunityMixin {

    @Unique
    private static final TagKey<Block> oneironaut$tag = MiscAPIKt.getBlockTagKey(new Identifier(Oneironaut.MOD_ID, "hexbreakimmune"));

    @Inject(method = "isBreakingAllowed", at = @At(value = "HEAD", remap = false), remap = false, cancellable = true)
    public void dontBreakIfImmune(ServerWorld world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfoReturnable<Boolean> cir){
        if (state.isIn(oneironaut$tag)){
            cir.setReturnValue(false);
        }
    }
}