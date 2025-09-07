package net.beholderface.oneironaut.block;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.beholderface.oneironaut.block.blockentity.SentinelTrapImpetusEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import at.petrak.hexcasting.common.lib.HexSounds;

public class SentinelTrapImpetus extends BlockAbstractImpetus {

    public SentinelTrapImpetus(Settings settings){
        super(settings);
    }
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SentinelTrapImpetusEntity(pos,state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : (_world, _pos, _state, _be) -> ((SentinelTrapImpetusEntity)_be).tick(_world, _pos, _state);
    }

    @Override
    public ActionResult onUse(BlockState pState, World world, BlockPos pPos, PlayerEntity pPlayer, Hand pHand,
                            BlockHitResult pHit) {
        if (world.getBlockEntity(pPos) instanceof SentinelTrapImpetusEntity tile) {
            var usedStack = pPlayer.getStackInHand(pHand);
            var datumContainer = IXplatAbstractions.INSTANCE.findDataHolder(usedStack);
            if (datumContainer != null) {
                if (!world.isClient && world instanceof ServerWorld level)
                    /*if (!world.isClient) */{
                        var stored = datumContainer.readIota(level);
                        if (stored instanceof EntityIota eieio) {
                            var entity = eieio.getEntity();
                            if (entity instanceof PlayerEntity player) {
                                // phew, we got something
                                tile.setPlayer(player.getGameProfile(), entity.getUuid());
                                level.updateListeners(pPos, pState, pState, Block.NOTIFY_LISTENERS);

                                world.playSound(pPlayer, pPos, HexSounds.IMPETUS_REDSTONE_DING,
                                        SoundCategory.BLOCKS, 1f, 1f);
                            }
                        }
                    }
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }
}
