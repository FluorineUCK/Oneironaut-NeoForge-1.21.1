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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConceptCoreBlock extends BlockWithEntity implements IConceptSocketed {
    public ConceptCoreBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(Properties.POWERED, false));
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ConceptCoreBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.AXIS);
        builder.add(Properties.POWERED);
    }

    @Override
    public void neighborUpdate(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos,
                               boolean pIsMoving) {
        super.neighborUpdate(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);

        if (pLevel instanceof ServerWorld world) {
            boolean prevPowered = pState.get(Properties.POWERED);
            boolean isPowered = pLevel.isReceivingRedstonePower(pPos);

            if (prevPowered != isPowered) {
                pLevel.setBlockState(pPos, pState.with(Properties.POWERED, isPowered));

                if (isPowered && pLevel.getBlockEntity(pPos) instanceof ConceptCoreBlockEntity be) {
                    ServerPlayerEntity player = be.getStoredPlayer();
                    if (player != null){
                        List<ConceptModifierBlockEntity> modifierBlocks = be.findConceptBlocks();
                        ConceptModifierManager manager = ConceptModifierManager.getServerState(player.server);
                        manager.clearPlayerModifiers(player.getUuid());
                        try {
                            EnumSet<ConceptModifier.ModifierType> encounteredTypes = EnumSet.noneOf(ConceptModifier.ModifierType.class);
                            Set<EntityAttribute> attributes = new HashSet<>();
                            long mediaTotal = 0L;
                            for (ConceptModifierBlockEntity entity : modifierBlocks){
                                ConceptModifier modifier = entity.getConceptModifier();
                                boolean shouldApply = false;
                                if (modifier.type == ConceptModifier.ModifierType.ATTRIBUTE){
                                    EntityAttribute attribute = modifier.getAttributeType();
                                    if (!attributes.contains(attribute)){
                                        attributes.add(attribute);
                                        shouldApply = true;
                                    }
                                } else if (!encounteredTypes.contains(modifier.type)){
                                    encounteredTypes.add(modifier.type);
                                    shouldApply = true;
                                }
                                if (shouldApply){
                                    manager.addModifier(player, modifier);
                                    modifier.onApply(player);
                                    mediaTotal += modifier.getMediaCost();
                                }
                            }
                            if (mediaTotal > 0){
                                be.extractMedia(mediaTotal);
                            }
                        } catch (Exception e){
                            //nothing
                        }
                    }
                }
            }
        }
    }

    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player){
        super.onBreak(world,pos,state,player);
        if (world instanceof ServerWorld serverWorld){
            ConceptModifierManager manager = ConceptModifierManager.getServerState(serverWorld.getServer());
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ConceptCoreBlockEntity core){
                List<ConceptModifier> modifiers = manager.getAllModifiers(core.getStoredUUID());
                if (!modifiers.isEmpty() && modifiers.get(0).corePos.equals(pos)){
                    manager.clearPlayerModifiers(core.getStoredUUID());
                }
            }
        }
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.AXIS, ctx.getSide().getAxis());
    }

    @Override
    public EnumSet<Direction> getSockets(BlockState state) {
        return switch (state.get(Properties.AXIS)) {
            case X -> EnumSet.of(Direction.EAST, Direction.WEST);
            case Y -> EnumSet.of(Direction.UP, Direction.DOWN);
            case Z -> EnumSet.of(Direction.SOUTH, Direction.NORTH);
        };
    }

    @Override
    public @Nullable Direction getRootFace(BlockState state) {
        return null;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
