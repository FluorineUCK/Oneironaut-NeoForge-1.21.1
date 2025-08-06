package net.beholderface.oneironaut.fabric.mixin;

import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.beholderface.oneironaut.block.Slurryloggable;
import net.beholderface.oneironaut.block.ThoughtSlurry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//wow this mixin really doesn't like other mods
//@Mixin(BlockSlate.class)
public class SlateSlurryloggingMixin implements Slurryloggable {

    /*@WrapMethod(method = "getFluidState")
    public FluidState getNewState(BlockState state, Operation<FluidState> original){
        if (state.get(Slurryloggable.slurrylogged)){
            return ThoughtSlurry.STILL_FLUID.getStill(false);
        } else {
            return original.call(state);
        }
    }

    @WrapMethod(method = "isTransparent")
    public boolean isItIdk(BlockState state, BlockView reader, BlockPos pos, Operation<Boolean> original){
        return state.get(Slurryloggable.slurrylogged) || original.call(state, reader, pos);
    }

    @Inject(method = "appendProperties", at = @At(value = "TAIL"))
    public void addSlurrylogged(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci){
        builder.add(Slurryloggable.slurrylogged);
    }

    @ModifyReturnValue(method = "getPlacementState", at = @At(value = "RETURN", ordinal = 0))
    public BlockState newState(BlockState state, @Local FluidState fluidState, @Local(argsOnly = true) ItemPlacementContext context){
        if (fluidState.getFluid() == ThoughtSlurry.STILL_FLUID.getStill()){
            BlockState newState = state.with(Properties.WATERLOGGED, false).with(Slurryloggable.slurrylogged, true);
            if (state.canPlaceAt(context.getWorld(), context.getBlockPos())){
                return newState;
            }
        }
        return state;
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lat/petrak/hexcasting/common/blocks/circles/BlockSlate;setDefaultState(Lnet/minecraft/block/BlockState;)V"))
    public void notSlurryLoggedByDefault(BlockSlate instance, BlockState blockState, Operation<Void> original){
        try {
            BlockState newState = blockState.with(Slurryloggable.slurrylogged, false);
            original.call(instance, newState);
        } catch (Exception e){
            original.call(instance, blockState);
        }
    }

    @Inject(method = "getStateForNeighborUpdate",at = @At(value = "HEAD"))
    public void scheduleSlurryTick(BlockState pState, Direction pFacing, BlockState pFacingState, WorldAccess pLevel, BlockPos pCurrentPos, BlockPos pFacingPos, CallbackInfoReturnable<BlockState> cir){
        if (pState.get(Slurryloggable.slurrylogged)){
            pLevel.scheduleFluidTick(pCurrentPos, ThoughtSlurry.STILL_FLUID.getStill(), ThoughtSlurry.STILL_FLUID.getStill().getTickRate(pLevel));
        }
    }*/


}
