package net.beholderface.oneironaut.block;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.utils.NBTHelper;
import com.mojang.datafixers.util.Pair;
import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.*;

public class ExtradimensionalBoundaryLocus extends Block implements ICircleComponent {

    public static final String TAG_BOUNDARY_LIST = "oneironaut:corners";

    public ExtradimensionalBoundaryLocus(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(BlockCircleComponent.ENERGIZED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(BlockCircleComponent.ENERGIZED);
        builder.add(Properties.FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        return this.getDefaultState().with(Properties.FACING, pContext.getSide());
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        NbtCompound nbt = imageIn.component6();
        Set<BlockPos> visited = toPositionSet(nbt.getList(TAG_BOUNDARY_LIST, NbtElement.COMPOUND_TYPE));
        visited.add(pos);
        NbtList serialized = toSerializedList(visited);
        //Oneironaut.LOGGER.info(serialized);
        NBTHelper.putList(nbt, TAG_BOUNDARY_LIST, serialized);

        List<Pair<BlockPos, Direction>> outputs = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if (d != enterDir.getOpposite() && d != bs.get(Properties.FACING)) {
                outputs.add(this.exitPositionFromDirection(pos, d));
            }
        }
        return new ControlFlow.Continue(imageIn, outputs);
    }

    @Override
    public boolean canEnterFromDirection(Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world) {
        return enterDir != bs.get(Properties.FACING).getOpposite();
    }

    @Override
    public EnumSet<Direction> possibleExitDirections(BlockPos pos, BlockState bs, World world) {
        EnumSet<Direction> enumset = EnumSet.allOf(Direction.class);
        enumset.remove(bs.get(Properties.FACING));
        return enumset;
    }

    @Override
    public BlockState startEnergized(BlockPos pos, BlockState bs, World world) {
        BlockState newState = bs.with(BlockCircleComponent.ENERGIZED, true);
        world.setBlockState(pos, newState);
        return newState;
    }

    @Override
    public boolean isEnergized(BlockPos pos, BlockState bs, World world) {
        return bs.get(BlockCircleComponent.ENERGIZED);
    }

    @Override
    public BlockState endEnergized(BlockPos pos, BlockState bs, World world) {
        BlockState newState = bs.with(BlockCircleComponent.ENERGIZED, false);
        world.setBlockState(pos, newState);
        return newState;
    }

    public static Set<BlockPos> toPositionSet(NbtList list) {
        Set<BlockPos> output = new HashSet<>();
        for (NbtElement element : list){
            output.add(NbtHelper.toBlockPos((NbtCompound) element));
        }
        return output;
    }

    public static NbtList toSerializedList(Set<BlockPos> set){
        NbtList output = new NbtList();
        for (BlockPos pos : set){
            output.add(NbtHelper.fromBlockPos(pos));
        }
        return output;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context){
        return switch (state.get(Properties.FACING)){
            case DOWN -> VoxelShapes.cuboid(0.0 / 16, 8.0 / 16, 0.0 / 16, 16.0 / 16, 16.0 / 16, 16.0 / 16);
            case UP -> VoxelShapes.cuboid(0.0 / 16, 0.0 / 16, 0.0 / 16, 16.0 / 16, 8.0 / 16, 16.0 / 16);
            case NORTH -> VoxelShapes.cuboid(0.0 / 16, 0.0 / 16, 8.0 / 16, 16.0 / 16, 16.0 / 16, 16.0 / 16);
            case SOUTH -> VoxelShapes.cuboid(0.0 / 16, 0.0 / 16, 0.0 / 16, 16.0 / 16, 16.0 / 16, 8.0 / 16);
            case WEST -> VoxelShapes.cuboid(8.0 / 16, 0.0 / 16, 0.0 / 16, 16.0 / 16, 16.0 / 16, 16.0 / 16);
            case EAST -> VoxelShapes.cuboid(0.0 / 16, 0.0 / 16, 0.0 / 16, 8.0 / 16, 16.0 / 16, 16.0 / 16);
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context){
        return switch (state.get(Properties.FACING)){
            case DOWN -> VoxelShapes.cuboid(0.0 / 16, 8.0 / 16, 0.0 / 16, 16.0 / 16, 16.0 / 16, 16.0 / 16);
            case UP -> VoxelShapes.cuboid(0.0 / 16, 0.0 / 16, 0.0 / 16, 16.0 / 16, 8.0 / 16, 16.0 / 16);
            case NORTH -> VoxelShapes.cuboid(0.0 / 16, 0.0 / 16, 8.0 / 16, 16.0 / 16, 16.0 / 16, 16.0 / 16);
            case SOUTH -> VoxelShapes.cuboid(0.0 / 16, 0.0 / 16, 0.0 / 16, 16.0 / 16, 16.0 / 16, 8.0 / 16);
            case WEST -> VoxelShapes.cuboid(8.0 / 16, 0.0 / 16, 0.0 / 16, 16.0 / 16, 16.0 / 16, 16.0 / 16);
            case EAST -> VoxelShapes.cuboid(0.0 / 16, 0.0 / 16, 0.0 / 16, 8.0 / 16, 16.0 / 16, 16.0 / 16);
        };
    }
}