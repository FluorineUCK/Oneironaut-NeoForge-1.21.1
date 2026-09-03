package net.beholderface.oneironaut.block;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.beholderface.oneironaut.network.ParticleBurstPacket;

import java.util.UUID;

public class MediaGelBlock extends HalfTransparentBlock {
    public MediaGelBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties settings) {
        super(settings);
    }
    protected static final VoxelShape COLLISION_SHAPE = Block.box(2.0, 2.0, 2.0, 14.0, 12.0, 14.0);
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context){
        return Shapes.block();
    }

    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter world, BlockPos pos) {
        return Shapes.block();
    }

    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }
    public boolean canPathfindThrough(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        return false;
    }

    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 0.2F;
    }

    private static final class PurpleColorizerHolder {
        private static final FrozenPigment INSTANCE = new FrozenPigment(
                HexItems.DYE_PIGMENTS.get(DyeColor.PURPLE).get().getDefaultInstance(),
                new UUID(0, 0)
        );
    }

    private static FrozenPigment purpleColorizer() {
        return PurpleColorizerHolder.INSTANCE;
    }
    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
        /*double d = Math.abs(entity.getVelocity().y);
        if (d < 0.1 && !entity.bypassesSteppingEffects()) {
            double e = 0.4 + d * 0.2;
            entity.setVelocity(entity.getVelocity().multiply(e, 1.0, e));
        }*/
        if(!entity.isSteppingCarefully() && !world.isClientSide && (world.getGameTime() % 10) == 0 && entity.showVehicleHealth()){
            Vec3 targetPos = entity.position().add(0, 0.2, 0);
            IXplatAbstractions.INSTANCE.sendPacketNear(
                    targetPos,
                    32.0,
                    (ServerLevel) world,
                    new ParticleBurstPacket(targetPos, new Vec3(0, -0.02, 0), 0.2, 0,
                            purpleColorizer(), 20, false)
                    );
        }
        super.stepOn(world, pos, state, entity);
    }
}
