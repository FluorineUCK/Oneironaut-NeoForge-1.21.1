package net.beholderface.oneironaut.block;

import net.beholderface.oneironaut.block.blockentity.ConceptCoreBlockEntity;
import net.beholderface.oneironaut.block.blockentity.ConceptModifierBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface IConceptSocketed {
    public EnumSet<Direction> getSockets(BlockState state);

    public default List<ConceptModifierBlockEntity> getConnectedModifiers(BlockState state, BlockPos pos, World world, @Nullable Set<BlockPos> alreadyChecked){
        List<ConceptModifierBlockEntity> modifiers = new ArrayList<>();
        if (alreadyChecked == null){
            alreadyChecked = new HashSet<>();
        }
        for (Direction dir : this.getSockets(state)){
            BlockPos checkedPos = pos.offset(dir);
            if (!alreadyChecked.contains(checkedPos)){
                alreadyChecked.add(checkedPos);
                BlockState checkedState = world.getBlockState(checkedPos);
                if (checkedState.getBlock() instanceof IConceptSocketed socketed){
                    modifiers.addAll(socketed.getConnectedModifiers(state, pos, world, alreadyChecked));
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
    public default ConceptCoreBlockEntity getCore(BlockState state, BlockPos pos, World world, @Nullable Set<BlockPos> alreadyVisited){
        if (alreadyVisited == null){
            alreadyVisited = new HashSet<>();
        }
        Block blockType = state.getBlock();
        while ((!(blockType instanceof ConceptCoreBlock)) && !alreadyVisited.contains(pos)){
            if (blockType instanceof IConceptSocketed socketed){
                Direction dir = socketed.getRootFace(state);
                if (dir != null){
                    pos = pos.offset(dir);
                    state = world.getBlockState(pos);
                    blockType = state.getBlock();
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        if (blockType instanceof ConceptCoreBlock){
            return (ConceptCoreBlockEntity) world.getBlockEntity(pos);
        }
        return null;
    }
}
