package net.beholderface.oneironaut.casting.environments;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.block.ExtradimensionalBoundaryLocus;
import net.beholderface.oneironaut.mixin.GeneralCastEnvInvoker;
import net.beholderface.oneironaut.mixin.PlayerCastEnvInvoker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv.SENTINEL_RADIUS;

public class ExtradimensionalCircleCastEnv extends CircleCastEnv {

    public final CircleCastEnv parentEnv;
    public final int depth;
    public final CastingVM vm;
    public final Box targetDimBounds;
    public final ServerWorld originalWorld;

    public ExtradimensionalCircleCastEnv(CircleCastEnv parent, ServerWorld target, @Nullable CastingVM existingVM,
                                         CircleExecutionState existingState, Set<BlockPos> visitedLoci) {
        super(parent.getWorld(), existingState);
        this.parentEnv = parent;
        ((GeneralCastEnvInvoker)this).setWorld(target);
        if (parentEnv instanceof ExtradimensionalCircleCastEnv extradimensionalCastEnv){
            this.depth = extradimensionalCastEnv.depth + 1;
            this.originalWorld = extradimensionalCastEnv.originalWorld;
        } else {
            this.depth = 1;
            this.originalWorld = parent.getWorld();
        }
        this.vm = existingVM != null ? existingVM : CastingVM.empty(this);
        Box reallyFuckingTinyBox = new Box(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        if (visitedLoci.size() != 8){
            this.targetDimBounds = reallyFuckingTinyBox;
        } else {
            List<BlockPos> lociList = visitedLoci.stream().toList();
            BlockPos corner1 = lociList.get(0);
            double volumeRecord = -1.0;
            Box biggestBoxFound = reallyFuckingTinyBox;
            for (BlockPos pos : lociList){
                Box testBox = new Box(corner1, pos);
                if (MiscAPIKt.volume(testBox) > volumeRecord){
                    biggestBoxFound = testBox;
                    volumeRecord = MiscAPIKt.volume(testBox);
                }
            }
            boolean validBox = true;
            for (Vec3d vec : MiscAPIKt.corners(biggestBoxFound)){
                BlockPos pos = new BlockPos(MiscAPIKt.toVec3i(vec));
                if (!(visitedLoci.contains(pos) && originalWorld.getBlockState(pos).getBlock() instanceof ExtradimensionalBoundaryLocus)){
                    validBox = false;
                    break;
                }
            }
            if (validBox){
                double scaleMultiplier = this.parentEnv.getWorld().getDimension().coordinateScale() / this.world.getDimension().coordinateScale();
                Vec3d targetMinCoord = new Vec3d(biggestBoxFound.minX * scaleMultiplier, biggestBoxFound.minY, biggestBoxFound.minZ * scaleMultiplier);
                Vec3d targetMaxCoord = new Vec3d(biggestBoxFound.maxX * scaleMultiplier, biggestBoxFound.maxY, biggestBoxFound.maxZ * scaleMultiplier);
                this.targetDimBounds = new Box(targetMinCoord, targetMaxCoord);
            } else {
                this.targetDimBounds = reallyFuckingTinyBox;
            }
        }
        /*if (this.targetDimBounds == reallyFuckingTinyBox){
            Oneironaut.LOGGER.info("tiny-ass box");
        } else {
            Oneironaut.LOGGER.info("successful box");
        }*/
    }

    @Override
    public void postExecution(CastResult result) {
        parentEnv.postExecution(result);
    }

    @Override
    public boolean replaceItem(Predicate<ItemStack> stackOk, ItemStack replaceWith, @Nullable Hand hand) {
        return parentEnv.replaceItem(stackOk, replaceWith, hand);
    }

    @Override
    public long extractMediaEnvironment(long cost, boolean simulate) {
        double multiplier = 1.25;
        return ((GeneralCastEnvInvoker)parentEnv).extractFromEnv((long) (cost * multiplier), simulate);
    }

    @Override
    public Hand getCastingHand() {
        return parentEnv.getCastingHand();
    }

    @Override
    public FrozenPigment getPigment() {
        return parentEnv.getPigment();
    }

    @Override
    public LivingEntity getCastingEntity() {
        return parentEnv.getCastingEntity();
    }

    @Override
    public ServerPlayerEntity getCaster() {
        return parentEnv.getCaster();
    }

    @Override
    public @Nullable BlockEntityAbstractImpetus getImpetus() {
        var entity = this.originalWorld.getBlockEntity(execState.impetusPos);
        if (entity instanceof BlockEntityAbstractImpetus)
            return (BlockEntityAbstractImpetus) entity;
        return null;
    }

    @Override
    public boolean isVecInRangeEnvironment(Vec3d vec) {
        ServerPlayerEntity caster = this.execState.getCaster(this.world);
        if (this.world == parentEnv.getWorld()){
            return parentEnv.isVecInRangeEnvironment(vec);
        }
        boolean withinSentinel = false;
        if (caster != null){
            var sentinel = HexAPI.instance().getSentinel(caster);
            withinSentinel = sentinel != null
                    && sentinel.extendsRange()
                    && this.world.getRegistryKey() == sentinel.dimension()
                    // adding 0.00000000001 to avoid machine precision errors at specific angles
                    && vec.squaredDistanceTo(sentinel.position()) <= SENTINEL_RADIUS * SENTINEL_RADIUS + 0.00000000001;
        }
        return withinSentinel || this.targetDimBounds.contains(vec);
    }

    @Override
    public @Nullable FrozenPigment setPigment(@Nullable FrozenPigment pigment) {
        return parentEnv.setPigment(pigment);
    }

    @Override
    public void produceParticles(ParticleSpray particles, FrozenPigment pigment) {
        parentEnv.produceParticles(particles, pigment);
    }

    @Override
    public Vec3d mishapSprayPos() {
        return parentEnv.mishapSprayPos();
    }

    @Override
    public MishapEnvironment getMishapEnvironment() {
        return parentEnv.getMishapEnvironment();
    }

    @Override
    protected boolean isCreativeMode() {
        // not sure what the diff between this and isCreative() is
        return ((PlayerCastEnvInvoker)parentEnv).isCreative();
    }

    @Override
    public void printMessage(Text message) {
        parentEnv.printMessage(message);
    }

    @Override
    public boolean hasEditPermissionsAtEnvironment(BlockPos pos){
        if (this.getCaster() != null){
            return this.getCaster().interactionManager.getGameMode() != GameMode.ADVENTURE && this.world.canPlayerModifyAt(this.getCaster(), pos);
        } else {
            return true;
        }
    }
}
