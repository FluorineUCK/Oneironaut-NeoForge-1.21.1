package net.beholderface.oneironaut.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ConceptConnectorBlock extends Block implements IConceptSocketed {
    public ConceptConnectorBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.FACING);
    }

    @Override
    public EnumSet<Direction> getSockets(BlockState state) {
        return switch (state.getValue(BlockStateProperties.FACING).getAxis()){
            case X -> EnumSet.of(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH);
            case Y -> EnumSet.of(Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH);
            case Z -> EnumSet.of(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST);
        };
    }

    @Override
    public @Nullable Direction getRootFace(BlockState state) {
        return state.getValue(BlockStateProperties.FACING).getOpposite();
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(BlockStateProperties.FACING, ctx.getClickedFace());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context){
        return switch (state.getValue(BlockStateProperties.FACING)){
            case DOWN, UP -> Shapes.box(0.0 / 16, 3.0 / 16, 0.0 / 16, 16.0 / 16, 13.0 / 16, 16.0 / 16);
            case NORTH, SOUTH -> Shapes.box(0.0 / 16, 0.0 / 16, 3.0 / 16, 16.0 / 16, 16.0 / 16, 13.0 / 16);
            case WEST, EAST -> Shapes.box(3.0 / 16, 0.0 / 16, 0.0 / 16, 13.0 / 16, 16.0 / 16, 16.0 / 16);
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context){
        return switch (state.getValue(BlockStateProperties.FACING)){
            case DOWN, UP -> Shapes.box(0.0 / 16, 3.0 / 16, 0.0 / 16, 16.0 / 16, 13.0 / 16, 16.0 / 16);
            case NORTH, SOUTH -> Shapes.box(0.0 / 16, 0.0 / 16, 3.0 / 16, 16.0 / 16, 16.0 / 16, 13.0 / 16);
            case WEST, EAST -> Shapes.box(3.0 / 16, 0.0 / 16, 0.0 / 16, 13.0 / 16, 16.0 / 16, 16.0 / 16);
        };
    }
}
