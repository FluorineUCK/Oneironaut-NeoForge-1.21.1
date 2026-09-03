package net.beholderface.oneironaut.block.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.api.utils.TreeList;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.nbt.NbtOps;
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
        Player player = getStoredPlayer();
        if (player != null) {
            return player.getGameProfile();
        }

        return this.storedPlayerProfile;
    }

    public void setPlayer(GameProfile profile, UUID player) {
        this.storedPlayerProfile = profile;
        this.storedPlayer = player;
        this.setChanged();
    }

    public void clearPlayer() {
        this.storedPlayerProfile = null;
        this.storedPlayer = null;
    }

    public void updatePlayerProfile() {
        Player player = getStoredPlayer();
        if (player != null) {
            GameProfile newProfile = player.getGameProfile();
            if (!newProfile.equals(this.storedPlayerProfile)) {
                this.storedPlayerProfile = newProfile;
                this.setChanged();
            }
        } else {
            this.storedPlayerProfile = null;
        }
    }
    public @Nullable
    Player getStoredPlayer() {
        assert this.level != null;
        if (this.storedPlayer != null){
            return this.level.getPlayerByUUID(this.storedPlayer);
        } else {
            return null;
        }
        //return this.storedPlayer;
    }

    public @Nullable Player getTargetPlayer(){
        assert this.level != null;
        if (this.targetPlayer != null){
            return this.level.getPlayerByUUID(this.targetPlayer);
        } else {
            return null;
        }
    }

    public void setTargetPlayer(UUID player) {
        //Oneironaut.LOGGER.info("Setting impetus target player to " + player);
        this.targetPlayer = player;
        this.setChanged();
    }

    @Override
    public void applyScryingLensOverlay(List<Pair<ItemStack, Component>> lines,
                                        BlockState state, BlockPos pos, Player observer,
                                        Level world,
                                        Direction hitFace) {
        super.applyScryingLensOverlay(lines, state, pos, observer, world, hitFace);

        var name = this.getPlayerName();
        if (name != null) {
            if (!name.equals(cachedDisplayProfile) || cachedDisplayStack == null) {
                cachedDisplayProfile = name;
                var head = new ItemStack(Items.PLAYER_HEAD);
                head.set(DataComponents.PROFILE, new ResolvableProfile(name));
                cachedDisplayStack = head;
            }
            lines.add(Pair.of(cachedDisplayStack,
                    Component.translatable("hexcasting.tooltip.lens.impetus.redstone.bound", name.getName())));
        } else {
            lines.add(Pair.of(new ItemStack(Items.BARRIER),
                    Component.translatable("hexcasting.tooltip.lens.impetus.redstone.bound.none")));
        }
    }
    @Override
    protected void saveModData(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveModData(tag, registries);
        if (this.storedPlayer != null) {
            tag.putUUID(TAG_STORED_PLAYER, this.storedPlayer);
        }
        if (this.targetPlayer != null){
            tag.putUUID(TAG_TARGET_PLAYER, this.targetPlayer);
        }
        if (this.storedPlayerProfile != null) {
            ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, new ResolvableProfile(storedPlayerProfile))
                    .result()
                    .ifPresent(profileTag -> tag.put(TAG_STORED_PLAYER_PROFILE, profileTag));
        }
    }

    @Override
    protected void loadModData(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadModData(tag, registries);
        if (tag.contains(TAG_STORED_PLAYER, Tag.TAG_INT_ARRAY)) {
            this.storedPlayer = tag.getUUID(TAG_STORED_PLAYER);
        } else {
            this.storedPlayer = null;
        }
        if (tag.contains(TAG_TARGET_PLAYER, Tag.TAG_INT_ARRAY)){
            this.targetPlayer = tag.getUUID(TAG_TARGET_PLAYER);
        } else {
            this.targetPlayer = null;
        }
        if (tag.contains(TAG_STORED_PLAYER_PROFILE)) {
            this.storedPlayerProfile = ResolvableProfile.CODEC
                    .parse(NbtOps.INSTANCE, tag.get(TAG_STORED_PLAYER_PROFILE))
                    .result()
                    .map(ResolvableProfile::gameProfile)
                    .orElse(null);
        } else {
            this.storedPlayerProfile = null;
        }
    }

    public static Map<ResourceKey<Level>, Map<BlockPos, Vec3>> trapLocationMap = new HashMap<>();
    //@Override
    public void tick(Level world, BlockPos pos, BlockState state) {
        ResourceKey<Level> worldKey = world.dimension();
        if (!(trapLocationMap.containsKey(worldKey))){
            Map<BlockPos, Vec3> newMap = new HashMap<BlockPos, Vec3>();
            newMap.put(pos, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
            trapLocationMap.put(worldKey, newMap);
            //Oneironaut.LOGGER.info("Created map and did a thing");
        } else {
            Map<BlockPos, Vec3> existingMap = trapLocationMap.get(worldKey);
            if (!(existingMap.containsKey(pos))){
                existingMap.put(pos, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                //Oneironaut.LOGGER.info("did a thing with existing map");
            }
        }
    }

    @Override
    public void startExecution(@Nullable ServerPlayer player) {
        super.startExecution(player);
        if (this.executionState != null && this.getTargetPlayer() != null){
            //Oneironaut.LOGGER.info("Attempting to set target player");
            CastingImage oldImage = this.executionState.currentImage;
            this.executionState.currentImage = oldImage.copy(
                    TreeList.from(Collections.singletonList(new EntityIota(this.getTargetPlayer()))),
                    0,
                    TreeList.empty(),
                    false,
                    false,
                    0L,
                    new CompoundTag());
            this.executionState.save();
        }
    }
}
