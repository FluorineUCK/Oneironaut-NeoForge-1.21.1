package net.beholderface.oneironaut.block;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.items.pigment.ItemDyePigment;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class InactiveSlipwayBlock extends Block {
    public InactiveSlipwayBlock(Settings settings) {
        super(settings);
    }
    private static List<Integer> colors;
    public static void init(){
        Random random = Random.create();
        List<Integer> colorList = new ArrayList<>();
        for (int i = 0; i < 32; i++){
            for(ItemDyePigment pigment : HexItems.DYE_PIGMENTS.values()){
                FrozenPigment frozen = new FrozenPigment(new ItemStack(pigment), Util.NIL_UUID);
                colorList.add(ram.talia.hexal.api.FunUtilsKt.nextColour(frozen, random));
            }
        }
        colors = colorList;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context){
        return VoxelShapes.empty();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state){
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        Vec3d particleCenter = Vec3d.ofCenter(pos);
        for(ItemDyePigment pigment : HexItems.DYE_PIGMENTS.values()){
            int color = colors.get(random.nextInt(colors.size()));
            Vec3d particlePoint = new Vec3d(
                    (particleCenter.x + 0.35 * random.nextGaussian()),
                    (particleCenter.y + 0.35 * random.nextGaussian()),
                    (particleCenter.z + 0.35 * random.nextGaussian()));
            world.addParticle(new ConjureParticleOptions(color),
                    particlePoint.x,
                    particlePoint.y,
                    particlePoint.z,
                    0.0125 * (random.nextDouble() - 0.5),
                    0.0125 * (random.nextDouble() - 0.5),
                    0.0125 * (random.nextDouble() - 0.5));
        }
    }
}
