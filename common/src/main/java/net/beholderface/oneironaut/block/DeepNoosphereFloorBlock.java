package net.beholderface.oneironaut.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DeepNoosphereFloorBlock extends Block {
    public DeepNoosphereFloorBlock(Settings settings) {
        super(settings);
    }

    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.fullCube();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context){
        return VoxelShapes.fullCube();
    }

    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.fullCube();
    }

    public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.fullCube();
    }

    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        double y = pos.getY() < 0.0 ? 256.0 : 0.0;
        entity.teleport(pos.getX(), y, pos.getZ());
    }

    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        double y = pos.getY() < 0.0 ? 256.0 : 0.0;
        entity.teleport(pos.getX(), y, pos.getZ());
    }

    public void onEntityLand(BlockView world, Entity entity) {
        Vec3d pos = entity.getPos();
        double y = pos.getY() < 0.0 ? 256.0 : 0.0;
        entity.teleport(pos.getX(), y, pos.getZ());
    }

    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        double y = pos.getY() < 0.0 ? 256.0 : 0.0;
        entity.teleport(pos.getX(), y, pos.getZ());
    }
}
