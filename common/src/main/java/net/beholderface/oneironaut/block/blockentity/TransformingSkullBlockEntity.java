package net.beholderface.oneironaut.block.blockentity;

import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TransformingSkullBlockEntity extends BlockEntity {
    private static final IntProperty ROTATION = SkullBlock.ROTATION;
    public TransformingSkullBlockEntity(BlockPos pos, BlockState state) {
        super(OneironautBlockRegistry.TRANFORMING_SKULL_ENTITY.get(), pos, state);
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos pos, BlockState state, boolean isWall) {
        if (world instanceof ServerWorld serverWorld){
            ServerPlayerEntity nearestPlayer = null;
            double distance = Double.MAX_VALUE;
            Vec3d posCenter = pos.toCenterPos();
            for (ServerPlayerEntity checked : serverWorld.getPlayers()){
                Vec3d eyePos = checked.getEyePos();
                if (eyePos.distanceTo(posCenter) < distance){
                    nearestPlayer = checked;
                    distance = eyePos.distanceTo(posCenter);
                    if (distance == 0){
                        break;
                    }
                }
            }
            BlockState newState = null;
            boolean foundPlayer = false;
            BlockState playerHeadState;
            BlockState zombieHeadState;
            if (!isWall){
                playerHeadState = Blocks.PLAYER_HEAD.getDefaultState().with(ROTATION, state.get(ROTATION));
                zombieHeadState = Blocks.ZOMBIE_HEAD.getDefaultState().with(ROTATION, state.get(ROTATION));
            } else {
                playerHeadState = Blocks.PLAYER_WALL_HEAD.getDefaultState().with(WallSkullBlock.FACING, state.get(WallSkullBlock.FACING));
                zombieHeadState = Blocks.ZOMBIE_WALL_HEAD.getDefaultState().with(WallSkullBlock.FACING, state.get(WallSkullBlock.FACING));
            }
            if (nearestPlayer != null && distance <= 32){
                foundPlayer = true;
                newState = playerHeadState;
            } else {
                newState = zombieHeadState;
            }
            world.setBlockState(pos, newState);
            if (foundPlayer){
                SkullBlockEntity entity = new SkullBlockEntity(pos, newState);
                entity.setOwner(nearestPlayer.getGameProfile());
                world.addBlockEntity(entity);
            }
        }
    }
}
