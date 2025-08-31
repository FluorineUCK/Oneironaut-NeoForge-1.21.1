package net.beholderface.oneironaut.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

//it's the same as ConceptModifierBlock but strictly decorative
public class ConceptDecoratorBlock extends Block {
    public ConceptDecoratorBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.FACING);
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.FACING, ctx.getSide());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context){
        return switch (state.get(Properties.FACING)){
            case DOWN -> VoxelShapes.cuboid(3.0 / 16, 2.0 / 16, 3.0 / 16, 13.0 / 16, 16.0 / 16, 13.0 / 16);
            case UP -> VoxelShapes.cuboid(3.0 / 16, 0.0 / 16, 3.0 / 16, 13.0 / 16, 14.0 / 16, 13.0 / 16);
            case NORTH -> VoxelShapes.cuboid(3.0 / 16, 3.0 / 16, 2.0 / 16, 13.0 / 16, 13.0 / 16, 16.0 / 16);
            case SOUTH -> VoxelShapes.cuboid(3.0 / 16, 3.0 / 16, 0.0 / 16, 13.0 / 16, 13.0 / 16, 14.0 / 16);
            case WEST -> VoxelShapes.cuboid(2.0 / 16, 3.0 / 16, 3.0 / 16, 16.0 / 16, 13.0 / 16, 13.0 / 16);
            case EAST -> VoxelShapes.cuboid(0.0 / 16, 3.0 / 16, 3.0 / 16, 14.0 / 16, 13.0 / 16, 13.0 / 16);
        };
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context){
        return switch (state.get(Properties.FACING)){
            case DOWN -> VoxelShapes.cuboid(3.0 / 16, 2.0 / 16, 3.0 / 16, 13.0 / 16, 16.0 / 16, 13.0 / 16);
            case UP -> VoxelShapes.cuboid(3.0 / 16, 0.0 / 16, 3.0 / 16, 13.0 / 16, 14.0 / 16, 13.0 / 16);
            case NORTH -> VoxelShapes.cuboid(3.0 / 16, 3.0 / 16, 2.0 / 16, 13.0 / 16, 13.0 / 16, 16.0 / 16);
            case SOUTH -> VoxelShapes.cuboid(3.0 / 16, 3.0 / 16, 0.0 / 16, 13.0 / 16, 13.0 / 16, 14.0 / 16);
            case WEST -> VoxelShapes.cuboid(2.0 / 16, 3.0 / 16, 3.0 / 16, 16.0 / 16, 13.0 / 16, 13.0 / 16);
            case EAST -> VoxelShapes.cuboid(0.0 / 16, 3.0 / 16, 3.0 / 16, 14.0 / 16, 13.0 / 16, 13.0 / 16);
        };
    }
}
