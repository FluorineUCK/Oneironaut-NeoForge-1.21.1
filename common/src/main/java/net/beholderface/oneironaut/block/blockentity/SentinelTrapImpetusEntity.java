package net.beholderface.oneironaut.block.blockentity;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.utils.NBTHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import kotlin.collections.CollectionsKt;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SentinelTrapImpetusEntity extends BlockEntityAbstractImpetus {
    public static final String TAG_STORED_PLAYER = "stored_player";
    public static final String TAG_STORED_PLAYER_PROFILE = "stored_player_profile";

    private GameProfile storedPlayerProfile = null;
    private UUID storedPlayer = null;
    public static final String TAG_TARGET_PLAYER = "target_player";
    private UUID targetPlayer = null;

    private GameProfile cachedDisplayProfile = null;
    private ItemStack cachedDisplayStack = null;


    public SentinelTrapImpetusEntity(BlockPos pos, BlockState state){
        super(OneironautBlockRegistry.SENTINEL_TRAP_ENTITY.get(), pos, state);
    }

    /*@Override
    public boolean activatorAlwaysInRange() {
        return true;
    }*/

    protected @Nullable
    GameProfile getPlayerName() {
        PlayerEntity player = getStoredPlayer();
        if (player != null) {
            return player.getGameProfile();
        }

        return this.storedPlayerProfile;
    }

    public void setPlayer(GameProfile profile, UUID player) {
        this.storedPlayerProfile = profile;
        this.storedPlayer = player;
        this.markDirty();
    }

    public void clearPlayer() {
        this.storedPlayerProfile = null;
        this.storedPlayer = null;
    }

    public void updatePlayerProfile() {
        PlayerEntity player = getStoredPlayer();
        if (player != null) {
            GameProfile newProfile = player.getGameProfile();
            if (!newProfile.equals(this.storedPlayerProfile)) {
                this.storedPlayerProfile = newProfile;
                this.markDirty();
            }
        } else {
            this.storedPlayerProfile = null;
        }
    }
    public @Nullable
    PlayerEntity getStoredPlayer() {
        assert this.world != null;
        if (this.storedPlayer != null){
            return this.world.getPlayerByUuid(this.storedPlayer);
        } else {
            return null;
        }
        //return this.storedPlayer;
    }

    public @Nullable PlayerEntity getTargetPlayer(){
        assert this.world != null;
        if (this.targetPlayer != null){
            return this.world.getPlayerByUuid(this.targetPlayer);
        } else {
            return null;
        }
    }

    public void setTargetPlayer(UUID player) {
        //Oneironaut.LOGGER.info("Setting impetus target player to " + player);
        this.targetPlayer = player;
        this.markDirty();
    }

    public void applyScryingLensOverlay(List<Pair<ItemStack, Text>> lines,
                                        BlockState state, BlockPos pos, PlayerEntity observer,
                                        World world,
                                        Direction hitFace) {
        super.applyScryingLensOverlay(lines, state, pos, observer, world, hitFace);

        var name = this.getPlayerName();
        if (name != null) {
            if (!name.equals(cachedDisplayProfile) || cachedDisplayStack == null) {
                cachedDisplayProfile = name;
                var head = new ItemStack(Items.PLAYER_HEAD);
                NBTHelper.put(head, "SkullOwner", net.minecraft.nbt.NbtHelper.writeGameProfile(new NbtCompound(), name));
                head.getItem().postProcessNbt(head.getOrCreateNbt());
                cachedDisplayStack = head;
            }
            lines.add(new Pair<>(cachedDisplayStack,
                    Text.translatable("hexcasting.tooltip.lens.impetus.redstone.bound", name.getName())));
        } else {
            lines.add(new Pair<>(new ItemStack(Items.BARRIER),
                    Text.translatable("hexcasting.tooltip.lens.impetus.redstone.bound.none")));
        }
    }
    @Override
    protected void saveModData(NbtCompound tag) {
        super.saveModData(tag);
        if (this.storedPlayer != null) {
            tag.putUuid(TAG_STORED_PLAYER, this.storedPlayer);
        }
        if (this.targetPlayer != null){
            tag.putUuid(TAG_TARGET_PLAYER, this.targetPlayer);
        }
        if (this.storedPlayerProfile != null) {
            tag.put(TAG_STORED_PLAYER_PROFILE, net.minecraft.nbt.NbtHelper.writeGameProfile(new NbtCompound(), storedPlayerProfile));
        }
    }

    @Override
    protected void loadModData(NbtCompound tag) {
        super.loadModData(tag);
        if (tag.contains(TAG_STORED_PLAYER, NbtElement.INT_ARRAY_TYPE)) {
            this.storedPlayer = tag.getUuid(TAG_STORED_PLAYER);
        } else {
            this.storedPlayer = null;
        }
        if (tag.contains(TAG_TARGET_PLAYER, NbtElement.INT_ARRAY_TYPE)){
            this.targetPlayer = tag.getUuid(TAG_TARGET_PLAYER);
        } else {
            this.targetPlayer = null;
        }
        if (tag.contains(TAG_STORED_PLAYER_PROFILE, NbtElement.COMPOUND_TYPE)) {
            this.storedPlayerProfile = net.minecraft.nbt.NbtHelper.toGameProfile(tag.getCompound(TAG_STORED_PLAYER_PROFILE));
        } else {
            this.storedPlayerProfile = null;
        }
    }

    public static Map<RegistryKey<World>, Map<BlockPos, Vec3d>> trapLocationMap = new HashMap<>();
    //@Override
    public void tick(World world, BlockPos pos, BlockState state) {
        RegistryKey<World> worldKey = world.getRegistryKey();
        if (!(trapLocationMap.containsKey(worldKey))){
            Map<BlockPos, Vec3d> newMap = new HashMap<BlockPos, Vec3d>();
            newMap.put(pos, new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
            trapLocationMap.put(worldKey, newMap);
            //Oneironaut.LOGGER.info("Created map and did a thing");
        } else {
            Map<BlockPos, Vec3d> existingMap = trapLocationMap.get(worldKey);
            if (!(existingMap.containsKey(pos))){
                existingMap.put(pos, new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                //Oneironaut.LOGGER.info("did a thing with existing map");
            }
        }
    }

    @Override
    public void startExecution(@Nullable ServerPlayerEntity player) {
        super.startExecution(player);
        if (this.executionState != null && this.getTargetPlayer() != null){
            //Oneironaut.LOGGER.info("Attempting to set target player");
            CastingImage oldImage = this.executionState.currentImage;
            this.executionState.currentImage = oldImage.copy(CollectionsKt.listOf(new EntityIota(this.getTargetPlayer())), 0, CollectionsKt.emptyList(), false, 0L, new NbtCompound());
            this.executionState.save();
        }
    }
}
