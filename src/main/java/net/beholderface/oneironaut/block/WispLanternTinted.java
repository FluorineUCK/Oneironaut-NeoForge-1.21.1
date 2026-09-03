package net.beholderface.oneironaut.block;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.beholderface.oneironaut.block.blockentity.WispLanternEntityTinted;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public class WispLanternTinted extends UnitCodecEntityBlock/* implements ISplatoonableBlock*/ {

    public WispLanternTinted(net.minecraft.world.level.block.state.BlockBehaviour.Properties settings){
        super(settings);
        //setDefaultState(getDefaultState().setValue(COLOR, 0));
    }
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        //Oneironaut.LOGGER.info("Creating blockentity.");
        return new WispLanternEntityTinted(pos, state);
    }
    @Override
    public RenderShape getRenderShape(BlockState state){
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context){
        VoxelShape glass = Shapes.box(4f / 16, 0f / 16, 4f / 16, 12f / 16, 9f / 16, 12f / 16);
        VoxelShape lid = Shapes.box(5f / 16, 8f / 16, 5f / 16, 11f / 16, 10f / 16, 11f / 16);
        return Shapes.or(glass, lid);
    }

    public void splatPigmentOntoBlock(Level world, BlockPos pos, FrozenPigment pigment){
        WispLanternEntityTinted be = (WispLanternEntityTinted) (world.getBlockEntity(pos));
        assert be != null;
        be.setColor(pigment.item(), world.getPlayerByUUID(pigment.owner()));
        be.setChanged();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack item, BlockState state, Level world, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit){
        //int color = state.get(COLOR);
        if (IXplatAbstractions.INSTANCE.isPigment(item)){
            WispLanternEntityTinted be = (WispLanternEntityTinted) world.getBlockEntity(pos);
            assert be != null;
            be.setColor(item, player);
            be.setChanged();
            return ItemInteractionResult.SUCCESS;
        } else {
            return ItemInteractionResult.FAIL;
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide ? (_world, _pos, _state, _be) -> ((WispLanternEntityTinted)_be).tick(_world, _pos, _state) : null;
    }
}
