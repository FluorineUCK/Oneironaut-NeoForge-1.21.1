package net.beholderface.oneironaut.block;

import net.beholderface.oneironaut.block.blockentity.ConceptCoreBlockEntity;
import net.beholderface.oneironaut.block.blockentity.ConceptModifierBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface IConceptSocketed {
    public EnumSet<Direction> getSockets(BlockState state);

    public default List<ConceptModifierBlockEntity> getConnectedModifiers(BlockState state, BlockPos pos, Level world, @Nullable Set<BlockPos> alreadyChecked){
        List<ConceptModifierBlockEntity> modifiers = new ArrayList<>();
        if (alreadyChecked == null){
            alreadyChecked = new HashSet<>();
        }
        for (Direction dir : this.getSockets(state)){
            BlockPos checkedPos = pos.relative(dir);
            if (!alreadyChecked.contains(checkedPos)){
                alreadyChecked.add(checkedPos);
                BlockState checkedState = world.getBlockState(checkedPos);
                if (checkedState.getBlock() instanceof IConceptSocketed socketed){
                    modifiers.addAll(socketed.getConnectedModifiers(checkedState, checkedPos, world, alreadyChecked));
                }
                if (checkedState.getBlock() instanceof ConceptModifierBlock modifierBlock){
                    modifiers.add((ConceptModifierBlockEntity) world.getBlockEntity(checkedPos));
                }
            }
        }
        return modifiers;
    }

    @Nullable
    public Direction getRootFace(BlockState state);

    @Nullable
    public default ConceptCoreBlockEntity getCore(BlockState state, BlockPos pos, Level world, @Nullable Set<BlockPos> alreadyVisited){
        if (alreadyVisited == null){
            alreadyVisited = new HashSet<>();
        }
        Block blockType = state.getBlock();
        Direction dir = null;
        while ((!(blockType instanceof ConceptCoreBlock)) && !alreadyVisited.contains(pos)){
            if (blockType instanceof IConceptSocketed socketed){
                dir = socketed.getRootFace(state);
                if (dir != null){
                    pos = pos.relative(dir);
                    state = world.getBlockState(pos);
                    blockType = state.getBlock();
                    if (!canAcceptConnection(pos, dir, world)){
                        break;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        if (blockType instanceof ConceptCoreBlock && canAcceptConnection(pos, dir, world)){
            return (ConceptCoreBlockEntity) world.getBlockEntity(pos);
        }
        return null;
    }

    public static boolean canAcceptConnection(BlockPos checked, Direction from, Level world){
        if (from != null && world.getBlockState(checked).getBlock() instanceof IConceptSocketed socketed){
            return socketed.getSockets(world.getBlockState(checked)).contains(from.getOpposite());
        }
        return false;
    }
}
