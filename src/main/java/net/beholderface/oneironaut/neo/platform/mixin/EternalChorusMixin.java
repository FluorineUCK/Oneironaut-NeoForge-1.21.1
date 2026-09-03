package net.beholderface.oneironaut.neo.platform.mixin;

import net.minecraft.world.level.block.state.BlockState;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.OneironautConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.beholderface.oneironaut.registry.OneironautBlockRegistry.ETERNAL;

//my mega chorus on hexxycraft may have been griefed, but its spirit shall live on
@Mixin(ChorusFlowerBlock.class)
public class EternalChorusMixin {

    @Shadow @Final public static IntegerProperty AGE;
    @Unique private static final boolean debugMessages = false;

    @Inject(method = "createBlockStateDefinition", at = @At(value = "HEAD", remap = true), remap = true)
    public void addEternal(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci){
        builder.add(ETERNAL);
    }
    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/ChorusFlowerBlock;placeGrownFlower(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;I)V",
    remap = true), remap = true)
    private void growForever(ChorusFlowerBlock instance, Level world, BlockPos pos, int age, Operation<Void> original, @Local(ordinal = 0) BlockState state){
        boolean allowEternal = OneironautConfig.getServer().getInfusionEternalChorus();
        boolean isEternal = state.getValue(ETERNAL);
        if (!isEternal || !allowEternal){
            Oneironaut.boolLogger("not eternal", debugMessages);
            original.call(instance, world, pos, age);
        } else {
            Oneironaut.boolLogger("eternal!", debugMessages);
            growEternally(instance, world, pos);
            /*world.setBlockState(pos, (BlockState)instance.defaultBlockState().setValue(AGE, 0).setValue(ETERNAL, true), 2);
            world.syncWorldEvent(1033, pos, 0);*/
        }
    }

    //cutting down a true mega chorus results in weird floating bits for some reason, but at least they destroy themselves when they try to grow
    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/ChorusFlowerBlock;placeDeadFlower(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
            remap = true, ordinal = 0), remap = true)
    private void stopDying1(ChorusFlowerBlock instance, Level world, BlockPos pos, Operation<Void> original, @Local(ordinal = 0) BlockState state){
        boolean allowEternal = OneironautConfig.getServer().getInfusionEternalChorus();
        boolean isEternal = state.getValue(ETERNAL);
        if (!isEternal || !allowEternal){
            Oneironaut.boolLogger("not eternal", debugMessages);
            original.call(instance, world, pos);
        } else {
            //do nothing
            Oneironaut.boolLogger("eternal!", debugMessages);
        }
    }

    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/ChorusFlowerBlock;placeDeadFlower(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
            remap = true, ordinal = 1), remap = true)
    private void stopDying2(ChorusFlowerBlock instance, Level world, BlockPos pos, Operation<Void> original, @Local(ordinal = 0) BlockState state){
        boolean allowEternal = OneironautConfig.getServer().getInfusionEternalChorus();
        boolean isEternal = state.getValue(ETERNAL);
        if (!isEternal || !allowEternal){
            Oneironaut.boolLogger("not eternal", debugMessages);
            original.call(instance, world, pos);
        } else {
            Oneironaut.boolLogger("eternal!", debugMessages);
            growEternally(instance, world, pos);
        }
    }

    @ModifyReceiver(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"))
    public BlockState notUsuallyEternal(BlockState instance, Property property, Comparable comparable){
        return instance.setValue(ETERNAL, false);
    }

    @Unique private void growEternally(ChorusFlowerBlock instance, Level world, BlockPos pos){
        world.setBlock(pos, (BlockState)instance.defaultBlockState().setValue(AGE, 0).setValue(ETERNAL, true), 2);
        world.levelEvent(1033, pos, 0);

    }

}
