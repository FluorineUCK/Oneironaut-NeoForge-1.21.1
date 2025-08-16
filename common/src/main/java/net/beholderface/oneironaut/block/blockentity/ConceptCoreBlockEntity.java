package net.beholderface.oneironaut.block.blockentity;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.misc.MediaConstants;
import com.mojang.authlib.GameProfile;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.block.ConceptCoreBlock;
import net.beholderface.oneironaut.block.ConceptModifierBlock;
import net.beholderface.oneironaut.item.WriteableBlockItem;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus.TAG_MEDIA;
import static at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRedstoneImpetus.TAG_STORED_PLAYER;
import static at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRedstoneImpetus.TAG_STORED_PLAYER_PROFILE;

public class ConceptCoreBlockEntity extends HexBlockEntity {

    public static final long MAX_MEDIA_CAPACITY = MediaConstants.DUST_UNIT * 1000000;

    private GameProfile storedPlayerProfile = null;
    private UUID storedPlayer = null;
    private long storedMedia = 0L;

    public ConceptCoreBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(OneironautBlockRegistry.CONCEPT_CORE_ENTITY.get(), pWorldPosition, pBlockState);
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
        ServerPlayerEntity player = getStoredPlayer();
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

    @Nullable
    public ServerPlayerEntity getStoredPlayer() {
        if (this.storedPlayer == null) {
            return null;
        }
        if (!(this.world instanceof ServerWorld slevel)) {
            Oneironaut.LOGGER.error("Called getStoredPlayer on the client");
            return null;
        }
        var e = slevel.getEntity(this.storedPlayer);
        if (e instanceof ServerPlayerEntity player) {
            return player;
        } else {
            return null;
        }
    }
    @Nullable
    public UUID getStoredUUID(){
        return this.storedPlayer;
    }

    public List<ConceptModifierBlockEntity> findConceptBlocks(){
        List<ConceptModifierBlockEntity> output = new ArrayList<>();
        if (this.world != null){
            BlockState state = this.world.getBlockState(this.pos);
            output = ((ConceptCoreBlock)state.getBlock()).getConnectedModifiers(state, this.pos, this.world, null);
        }
        return output;
    }

    @Override
    protected void saveModData(NbtCompound tag) {
        if (this.storedPlayer != null) {
            tag.putUuid(TAG_STORED_PLAYER, this.storedPlayer);
        }
        if (this.storedPlayerProfile != null) {
            tag.put(TAG_STORED_PLAYER_PROFILE, NbtHelper.writeGameProfile(new NbtCompound(), storedPlayerProfile));
        }
    }

    @Override
    protected void loadModData(NbtCompound tag) {
        if (tag.contains(WriteableBlockItem.TAG_IOTA) && this.world != null && this.world instanceof ServerWorld){
            EntityIota iota = (EntityIota) IotaType.deserialize(tag.getCompound(WriteableBlockItem.TAG_IOTA), (ServerWorld) world);
            if (iota.getEntity() instanceof PlayerEntity player){
                this.storedPlayer = player.getUuid();
                this.storedPlayerProfile = player.getGameProfile();
            }
            return;
        }
        if (tag.contains(TAG_STORED_PLAYER, NbtElement.INT_ARRAY_TYPE)) {
            this.storedPlayer = tag.getUuid(TAG_STORED_PLAYER);
        } else {
            this.storedPlayer = null;
        }
        if (tag.contains(TAG_STORED_PLAYER_PROFILE, NbtElement.COMPOUND_TYPE)) {
            this.storedPlayerProfile = NbtHelper.toGameProfile(tag.getCompound(TAG_STORED_PLAYER_PROFILE));
        } else {
            this.storedPlayerProfile = null;
        }
        if (tag.contains(TAG_MEDIA, NbtElement.LONG_TYPE)) {
            this.storedMedia = tag.getLong(TAG_MEDIA);
        } else {
            this.storedMedia = 0L;
        }
    }

    public void insertMedia(long toAdd){
        if (this.storedMedia <= MAX_MEDIA_CAPACITY){
            long newMedia = this.storedMedia + Math.max(0L, toAdd);
            this.storedMedia = Math.max(0L, Math.min(MAX_MEDIA_CAPACITY, newMedia));
        }
    }
    public void extractMedia(long toSubtract){
        long newMedia = this.storedMedia - Math.max(0L, toSubtract);
        this.storedMedia = Math.max(0L, newMedia);
    }

    public long getStoredMedia() {
        return storedMedia;
    }
}
