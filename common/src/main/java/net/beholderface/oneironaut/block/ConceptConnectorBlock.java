package net.beholderface.oneironaut.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ConceptConnectorBlock extends Block implements IConceptSocketed {
    public ConceptConnectorBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.FACING);
    }

    @Override
    public EnumSet<Direction> getSockets(BlockState state) {
        return switch (state.get(Properties.FACING).getAxis()){
            case X -> EnumSet.of(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH);
            case Y -> EnumSet.of(Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH);
            case Z -> EnumSet.of(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST);
        };
    }

    @Override
    public @Nullable Direction getRootFace(BlockState state) {
        return state.get(Properties.FACING).getOpposite();
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.FACING, ctx.getSide());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context){
        return switch (state.get(Properties.FACING)){
            case DOWN, UP -> VoxelShapes.cuboid(0.0 / 16, 3.0 / 16, 0.0 / 16, 16.0 / 16, 13.0 / 16, 16.0 / 16);
            case NORTH, SOUTH -> VoxelShapes.cuboid(0.0 / 16, 0.0 / 16, 3.0 / 16, 16.0 / 16, 16.0 / 16, 13.0 / 16);
            case WEST, EAST -> VoxelShapes.cuboid(3.0 / 16, 0.0 / 16, 0.0 / 16, 13.0 / 16, 16.0 / 16, 16.0 / 16);
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context){
        return switch (state.get(Properties.FACING)){
            case DOWN, UP -> VoxelShapes.cuboid(0.0 / 16, 3.0 / 16, 0.0 / 16, 16.0 / 16, 13.0 / 16, 16.0 / 16);
            case NORTH, SOUTH -> VoxelShapes.cuboid(0.0 / 16, 0.0 / 16, 3.0 / 16, 16.0 / 16, 16.0 / 16, 13.0 / 16);
            case WEST, EAST -> VoxelShapes.cuboid(3.0 / 16, 0.0 / 16, 0.0 / 16, 13.0 / 16, 16.0 / 16, 16.0 / 16);
        };
    }
}
