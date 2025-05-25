package net.beholderface.oneironaut.block;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.OvercastDamageEnchant;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.beholderface.oneironaut.registry.OneironautItemRegistry;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RenderBerryBushBlock extends PlantBlock implements Fertilizable {
    public RenderBerryBushBlock(Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0).with(THOUGHTS, 0));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
        builder.add(THOUGHTS);
    }

    public static IntProperty AGE = SweetBerryBushBlock.AGE;
    public static final IntProperty THOUGHTS;
    private static final VoxelShape SMALL_SHAPE;
    private static final VoxelShape LARGE_SHAPE;

    private static final HashMap<Entity, Long> FEED_TIMESTAMP_MAP = new HashMap<>();

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
        return false;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {

    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int age = state.get(AGE);
        int thoughts = state.get(THOUGHTS);
        //small chance to grow on its own if there are non-brainswept mobs or players nearby
        boolean foundThinker = false;
        for (Entity entity : world.getOtherEntities(null, new Box(pos).expand(8, 3, 8))){
            if (entity instanceof MobEntity mob){
                foundThinker = !IXplatAbstractions.INSTANCE.isBrainswept(mob);
            } else if (entity instanceof PlayerEntity player && !player.isSpectator()){
                foundThinker = true;
            }
            if (foundThinker){
                break;
            }
        }
        //very small chance to grow from just slurry
        boolean foundSlurry = false;
        //same cuboid as if the block below it was farmland getting hydrated
        for (BlockState state2 : world.getStatesInBox(new Box(pos).expand(4, 2, 4)).toList()){
            if (state2.isIn(MiscAPIKt.getBlockTagKey(Identifier.of("oneironaut","growsmonkfruit")))){
                foundSlurry = true;
                break;
            }
        }
        int chance = Integer.MAX_VALUE;
        if (foundSlurry && foundThinker){
            chance = 4;
        } else if (foundThinker){
            chance = 5;
        } else if (foundSlurry){
            chance = 20;
        }
        if (age < 3 && random.nextInt(chance) == 0 && chance != Integer.MAX_VALUE) {
            BlockState blockState;
            if (thoughts == 3){
                blockState = state.with(AGE, age + 1).with(THOUGHTS, 0);
            } else {
                blockState = state.with(THOUGHTS, thoughts + 1);
            }
            world.setBlockState(pos, blockState, 2);
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 0.125f, 1.0f);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity livingEntity && entity.getType() != EntityType.FOX && entity.getType() != EntityType.BEE) {
            //I don't know what these numbers mean I'm just stealing this stuff from sweet berries
            entity.slowMovement(state, new Vec3d(0.800000011920929, 0.75, 0.800000011920929));
            if (!world.isClient && (entity.lastRenderX != entity.getX() || entity.lastRenderZ != entity.getZ())) {
                double d = Math.abs(entity.getX() - entity.lastRenderX);
                double e = Math.abs(entity.getZ() - entity.lastRenderZ);
                if (
                        (d >= 0.003000000026077032 || e >= 0.003000000026077032)
                        && world.getTime() >= FEED_TIMESTAMP_MAP.getOrDefault(entity, Long.MIN_VALUE) + 10
                ) {
                    FEED_TIMESTAMP_MAP.put(entity, world.getTime());
                    this.feed(state, world, pos, livingEntity);
                }
            }
        }
    }

    public void feed(BlockState state, World world, BlockPos pos, LivingEntity target){
        boolean brainswept = false;
        Random rand = world.random;
        int age = state.get(AGE);
        if (target instanceof MobEntity mob){
            brainswept = IXplatAbstractions.INSTANCE.isBrainswept(mob);
        }
        //the bush's tendrils coil in response to thought
        if (!brainswept){
            DamageSource berryDamage = target.getDamageSources().sweetBerryBush();
            target.damage(berryDamage, target.isPlayer() ? 0.001f : 0f);
            OvercastDamageEnchant.applyMindDamage(null, target, 2,
                    target.getType().isIn(MiscAPIKt.getEntityTagKey(Oneironaut.id("render_autospare"))));
            Identifier blacklistTagID = Identifier.of(Oneironaut.MOD_ID, "monkfruit_blacklist");
            assert blacklistTagID != null;
            var blacklistTag = MiscAPIKt.getEntityTagKey(blacklistTagID);
            if (target.getType().isIn(blacklistTag)){
                return;
            }
            //did that damage flay the target?
            if (target instanceof MobEntity mob){
                brainswept = IXplatAbstractions.INSTANCE.isBrainswept(mob);
                if (brainswept){
                    //grow a full stage immediately
                    world.setBlockState(pos, state.with(THOUGHTS, 0).with(AGE, Math.min(age + 1, 3)));
                    return;
                }
            }
            //make it respond less to players so that it's less trivial to just slap yourself with regen 0 and get tons of fruit
            int chance = target instanceof PlayerEntity ? 9 : 3;
            //chance to grow by a portion of a stage
            if (rand.nextBetween(1, chance) == chance && age < 3){
                if (state.get(THOUGHTS) < 3){
                    world.setBlockState(pos, state.with(THOUGHTS, state.get(THOUGHTS) + 1));
                } else {
                    world.setBlockState(pos, state.with(THOUGHTS, 0).with(AGE, age + 1));
                }
            }
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack clickStack = player.getStackInHand(hand);
        int age = state.get(AGE);
        boolean fullGrown = age == 3;
        int dropCount = 1 + world.random.nextInt(2);
        if (age > 1) {
            if (world.random.nextInt(3) == 3){
                OvercastDamageEnchant.applyMindDamage(null, player, 1, false);
            }
            boolean sheared = false;
            if (fullGrown && clickStack.getItem() == Items.SHEARS){
                dropStack(world, pos, new ItemStack(OneironautItemRegistry.RENDER_THORNS.get(), dropCount));
                clickStack.damage(1, player, (playerx) ->
                    playerx.sendToolBreakStatus(hand));
                sheared = true;
            } else {
                dropStack(world, pos, new ItemStack(OneironautItemRegistry.RENDER_FRUIT.get(), dropCount + (fullGrown ? 1 : 0)));
            }
            world.playSound(null, pos, SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, SoundCategory.BLOCKS, 1.0F, 0.8F + world.random.nextFloat() * 0.4F);
            BlockState blockState = state.with(AGE, sheared ? 0 : 1).with(THOUGHTS, 0);
            world.setBlockState(pos, blockState, 2);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, blockState));
            return ActionResult.success(world.isClient);
        } else {
            return super.onUse(state, world, pos, player, hand, hit);
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if ((Integer)state.get(AGE) == 0) {
            return SMALL_SHAPE;
        } else {
            return (Integer)state.get(AGE) < 3 ? LARGE_SHAPE : super.getOutlineShape(state, world, pos, context);
        }
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return (Integer)state.get(AGE) < 3;
    }

    static {
        AGE = Properties.AGE_3;
        THOUGHTS = IntProperty.of("thoughts", 0, 3);
        SMALL_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
        LARGE_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
    }
}
