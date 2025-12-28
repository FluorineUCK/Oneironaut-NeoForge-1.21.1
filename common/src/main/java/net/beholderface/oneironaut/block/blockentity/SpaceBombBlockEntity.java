package net.beholderface.oneironaut.block.blockentity;

import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.OvercastDamageEnchant;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.beholderface.oneironaut.registry.OneironautTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.common.lib.HexalBlocks;

import java.util.List;

import static java.lang.Math.pow;
import static java.lang.Math.signum;

public class SpaceBombBlockEntity extends BlockEntity {
    public static final int COUNTDOWN_LENGTH = 20 * 30;
    public static final String COUNTDOWN_TAG = "countdown";
    private int countdown = COUNTDOWN_LENGTH;
    public static final String POSITION_TAG = "cached_position";
    private BlockPos cachedPos = null;
    public SpaceBombBlockEntity(BlockPos pos, BlockState state) {
        super(OneironautBlockRegistry.SPACE_BOMB_ENTITY.get(), pos, state);
        if (this.cachedPos == null){
            this.cachedPos = pos;
        }
    }

    public void tick(World world, BlockPos pos, BlockState state){
        //no cardboard box fuckery for you
        if (!this.pos.equals(cachedPos)){
            this.countdown = COUNTDOWN_LENGTH;
            cachedPos = this.pos;
            if (state.get(BlockSlate.ENERGIZED)){
                world.setBlockState(pos, state.with(BlockSlate.ENERGIZED, false));
            }
        }
        if (this.world != null){
            Vec3d doublePos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            boolean imminent = this.countdown < COUNTDOWN_LENGTH / (COUNTDOWN_LENGTH / 100);
            boolean preparing = state.get(BlockSlate.ENERGIZED);
            if (preparing){
                this.tickCountdown();
                if (imminent){
                    List<Entity> nearbyLiving = world.getOtherEntities(null, new Box(pos).expand(16.0),
                            (it)-> it.getPos().distanceTo(doublePos) <= 16.0 && it instanceof LivingEntity);
                    for (Entity entity : nearbyLiving){
                        entity.addVelocity(entity.getEyePos().subtract(doublePos).normalize().multiply(0.025));
                    }
                    if (this.countdown <= 0){
                        if (!world.isClient){
                            this.explode(nearbyLiving);
                        }
                        for (Entity entity : nearbyLiving){
                            LivingEntity livingEntity = (LivingEntity) entity;
                            livingEntity.addVelocity(livingEntity.getEyePos().subtract(doublePos).normalize().multiply(2));
                        }
                    }
                }
            } else {
                if (this.world.getTime() % 10 == 0 && this.countdown < COUNTDOWN_LENGTH){
                    this.countdown++;
                }
            }
            if (this.countdown < COUNTDOWN_LENGTH && world.isClient){
                showParticles(preparing, imminent);
            }
        }
    }

    private void showParticles(boolean preparing, boolean imminent){
        boolean exploding = this.countdown < 15;
        assert this.world != null;
        assert this.world.isClient;
        Vec3d doublePos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Random rand = this.world.random;
        double offset = preparing ? 0.0 : 0.5;
        int direction = preparing ? (imminent ? (exploding ? 25 : 5) : 1) : -1;
        int i = 0;
        //doesn't just kill people, also kills their GPUs sometimes
        double risingParticles = (Math.pow(100 - this.countdown, 1.5)) + 8;
        while (i < ((preparing ? (imminent ? risingParticles : 8) : 2))){
            i++;
            double speedMultiplier = 0.025;
            double gaussX = rand.nextGaussian();
            double gaussY = rand.nextGaussian();
            double gaussZ = rand.nextGaussian();
            while (gaussX == 0){
                gaussX = rand.nextGaussian();
            }
            double gaussNormalize = 1 / (pow((pow(gaussX, 2) + pow(gaussY, 2) + pow(gaussZ, 2)), 0.5));
            gaussX = gaussX * gaussNormalize;
            gaussY = gaussY * gaussNormalize;
            gaussZ = gaussZ * gaussNormalize;
            double particlePosX = doublePos.x + gaussX * (rand.nextDouble() + offset);
            double particlePosY = doublePos.y + gaussY * (rand.nextDouble() + offset);
            double particlePosZ = doublePos.z + gaussZ * (rand.nextDouble() + offset);
            double particleVelX = (signum(particlePosX - doublePos.x) * speedMultiplier) * rand.nextDouble();
            double particleVelY = (signum(particlePosY - doublePos.y) * speedMultiplier) * rand.nextDouble();
            double particleVelZ = (signum(particlePosZ - doublePos.z) * speedMultiplier) * rand.nextDouble();
            boolean blue = rand.nextInt(4) == 0;
            world.addParticle(
                    new ConjureParticleOptions(preparing ? (blue ? 0x3296ff : 0xae31d2) : 0x6a31d2),
                    particlePosX, particlePosY, particlePosZ,
                    particleVelX * direction, particleVelY * direction, particleVelZ * direction
            );
        }
        if (imminent){
            world.playSound(null, this.pos, SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.BLOCKS, 2.0f, 1.0f);
        }
    }

    public int getCountdown(){
        return this.countdown;
    }

    private void tickCountdown(){
        this.countdown--;
        assert this.world != null;
        if (this.countdown % 20 == 0 && this.countdown > 0){
            boolean imminent = this.countdown < COUNTDOWN_LENGTH / (COUNTDOWN_LENGTH / 100);
            SoundEvent sound = imminent ? SoundEvents.BLOCK_BELL_USE : SoundEvents.UI_BUTTON_CLICK.value();
            float volume = imminent ? 0.75f : 1.0f;
            this.world.playSound(null, this.pos, sound, SoundCategory.BLOCKS, volume, imminent ?
                    1.0f : ((float)COUNTDOWN_LENGTH - this.countdown) /*between 0 and 600*// ((float) COUNTDOWN_LENGTH / 1.25f)
            );
        }
    }

    public void explode(List<Entity> living){
        assert this.world != null;
        Vec3d doublePos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        BlockState createdBlockstate = !Oneironaut.isWorldNoosphere((ServerWorld) this.world) ? HexalBlocks.SLIPWAY.getDefaultState() : OneironautBlockRegistry.NOOSPHERE_GATE.get().getDefaultState();
        this.world.setBlockState(this.pos, Blocks.AIR.getDefaultState());
        this.world.createExplosion(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), 5.0f, World.ExplosionSourceType.BLOCK);
        for (Entity entity : living){
            //check for warded obsidian in the path
            Vec3d entityPos = entity.getPos();
            boolean shielded = false;
            double d = 0.0;
            Vec3d checkedPos = doublePos.lerp(entityPos, d);
            for (; doublePos.distanceTo(checkedPos) <= doublePos.distanceTo(entityPos); d += 1.0/64.0){
                if (this.world.getBlockState(BlockPos.ofFloored(checkedPos.x, checkedPos.y, checkedPos.z)).getBlock() == OneironautBlockRegistry.HEX_RESISTANT_BLOCK.get()){
                    shielded = true;
                    break;
                }
                checkedPos = doublePos.lerp(entityPos, d);
            }
            if (!shielded){
                LivingEntity livingEntity = (LivingEntity) entity;
                OvercastDamageEnchant.applyMindDamage(null, livingEntity,
                        (int)Math.floor(16 - entityPos.distanceTo(doublePos)) * 2,
                        livingEntity.getType().isIn(OneironautTags.Entities.mindRenderAutospare));
            }
        }
        this.world.playSound(null, this.pos, SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 2.0f, 0.75f);
        this.world.setBlockState(this.pos, createdBlockstate);
    }

    public void sync() {
        this.markDirty();
        this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), 3);
    }
    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt(COUNTDOWN_TAG, this.countdown);
        int[] posArray = {this.cachedPos.getX(), this.cachedPos.getY(), this.cachedPos.getZ()};
        nbt.putIntArray(POSITION_TAG, posArray);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.countdown = nbt.getInt(COUNTDOWN_TAG);
        int[] posArray = nbt.getIntArray(POSITION_TAG);
        this.cachedPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
    }
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
