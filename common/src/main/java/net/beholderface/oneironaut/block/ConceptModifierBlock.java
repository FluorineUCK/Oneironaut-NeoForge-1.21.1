package net.beholderface.oneironaut.block;

import net.beholderface.oneironaut.block.blockentity.ConceptCoreBlockEntity;
import net.beholderface.oneironaut.block.blockentity.ConceptModifierBlockEntity;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifier;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifierManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class ConceptModifierBlock extends BlockWithEntity implements IConceptSocketed {

    private EntityAttribute attribute = null;
    public final ConceptModifier.ModifierType type;

    public ConceptModifierBlock(Settings settings, ConceptModifier.ModifierType type) {
        super(settings);
        this.type = type;
    }
    public ConceptModifierBlock(Settings settings, ConceptModifier.ModifierType type, EntityAttribute attribute){
        super(settings);
        this.type = type;
        this.attribute = attribute;
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
}
