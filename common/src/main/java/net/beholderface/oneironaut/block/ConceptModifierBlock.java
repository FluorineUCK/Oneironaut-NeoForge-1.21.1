package net.beholderface.oneironaut.block;

import net.beholderface.oneironaut.block.blockentity.ConceptCoreBlockEntity;
import net.beholderface.oneironaut.block.blockentity.ConceptModifierBlockEntity;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifier;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifierManager;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

public class ConceptModifierBlock extends BlockWithEntity implements IConceptSocketed {

    private EntityAttribute attribute = null;
    public final ConceptModifier.ModifierType type;
    @Nullable
    public final Function<NbtCompound, Double> costCalulator;

    public ConceptModifierBlock(Settings settings, ConceptModifier.ModifierType type, @Nullable Function<NbtCompound, Double> costCalulator) {
        super(settings);
        this.type = type;
        this.costCalulator = costCalulator;
    }
    public ConceptModifierBlock(Settings settings, ConceptModifier.ModifierType type, EntityAttribute attribute, @Nullable Function<NbtCompound, Double> costCalulator){
        super(settings);
        this.type = type;
        this.attribute = attribute;
        this.costCalulator = costCalulator;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.FACING);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ConceptModifierBlockEntity(pos, state);
    }

    @Override
    public EnumSet<Direction> getSockets(BlockState state) {
        return EnumSet.noneOf(Direction.class);
    }

    @Override
    public @Nullable Direction getRootFace(BlockState state) {
        return state.get(Properties.FACING).getOpposite();
    }

    public EntityAttribute getAttribute(){
        return this.attribute;
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.FACING, ctx.getSide());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player){
        super.onBreak(world,pos,state,player);
        if (world instanceof ServerWorld serverWorld){
            ConceptModifierManager manager = ConceptModifierManager.getServerState(serverWorld.getServer());
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ConceptModifierBlockEntity modifierBlock){
                ConceptCoreBlockEntity core = this.getCore(state, pos, serverWorld, null);
                if (core != null){
                    manager.removeModifier(core.getStoredUUID(), modifierBlock.getConceptModifier());
                }
            }
        }
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
