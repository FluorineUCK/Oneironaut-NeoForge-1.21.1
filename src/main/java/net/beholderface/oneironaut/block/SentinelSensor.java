package net.beholderface.oneironaut.block;

import net.minecraft.world.phys.shapes.CollisionContext;

import net.minecraft.world.level.block.state.BlockState;

import net.beholderface.oneironaut.block.blockentity.SentinelSensorEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SentinelSensor extends UnitCodecEntityBlock {
    public static final BooleanProperty GREAT = BooleanProperty.create("great");
    public SentinelSensor(net.minecraft.world.level.block.state.BlockBehaviour.Properties settings){
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(GREAT, false));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.LEVEL);
        builder.add(GREAT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SentinelSensorEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return !(world.isClientSide) ? (_world, _pos, _state, _be) -> ((SentinelSensorEntity)_be).tick(_world, _pos, _state) : null;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
        return pState.getValue(GREAT) ? 15 : 0;
    }

    public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return state.getValue(BlockStateProperties.LEVEL);
    }

    @Override
    public RenderShape getRenderShape(BlockState state){
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context){
        return Shapes.box(0.0 / 16, 0.0 / 16, 0.0 / 16, 16.0 / 16, 6.0 / 16, 16.0 / 16);
    }
}
