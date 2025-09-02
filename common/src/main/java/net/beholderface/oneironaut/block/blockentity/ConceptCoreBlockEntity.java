package net.beholderface.oneironaut.block.blockentity;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.utils.MediaHelper;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.items.magic.ItemCreativeUnlocker;
import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.block.ConceptCoreBlock;
import net.beholderface.oneironaut.block.ConceptModifierBlock;
import net.beholderface.oneironaut.item.WriteableBlockItem;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus.TAG_MEDIA;
import static at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRedstoneImpetus.TAG_STORED_PLAYER;
import static at.petrak.hexcasting.common.blocks.circles.impetuses.BlockEntityRedstoneImpetus.TAG_STORED_PLAYER_PROFILE;
import static net.beholderface.oneironaut.item.BottomlessCastingItem.DUST_AMOUNT;

public class ConceptCoreBlockEntity extends HexBlockEntity implements SidedInventory {

    public static final long MAX_MEDIA_CAPACITY = MediaConstants.DUST_UNIT * 1000000;

    private GameProfile storedPlayerProfile = null;
    private ItemStack storedPlayerHeadStack = null;
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
        tag.putLong(TAG_MEDIA, this.storedMedia);
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

    public void insertMedia(ItemStack stack) {
        if (storedMedia >= 0 && !stack.isEmpty() && stack.getItem() == HexItems.CREATIVE_UNLOCKER) {
            this.setInfiniteMedia();
            stack.decrement(1);
        } else {
            var mediamount = extractMediaFromInsertedItem(stack, false);
            if (mediamount > 0) {
                this.storedMedia = Math.min(mediamount + this.storedMedia, MAX_MEDIA_CAPACITY);
                this.sync();
            }
        }
    }

    public void extractMedia(long toSubtract){
        long newMedia = this.storedMedia - Math.max(0L, toSubtract);
        this.storedMedia = Math.max(0L, newMedia);
    }

    public long getStoredMedia() {
        return storedMedia;
    }

    protected @Nullable
    GameProfile getPlayerName() {
        if (this.world instanceof ServerWorld) {
            PlayerEntity player = getStoredPlayer();
            if (player != null) {
                return player.getGameProfile();
            }
        }

        return this.storedPlayerProfile;
    }

    public static void applyScryingLensOverlay(List<Pair<ItemStack, Text>> lines,
                                               BlockState state, BlockPos pos, PlayerEntity observer, World world, Direction hitFace){
        if (world.getBlockEntity(pos) instanceof ConceptCoreBlockEntity core) {
            if (core.getStoredMedia() < 0) {
                lines.add(new Pair<>(new ItemStack(HexItems.AMETHYST_DUST), ItemCreativeUnlocker.infiniteMedia(world)));
            } else {
                var dustCount = (float) core.getStoredMedia() / (float) MediaConstants.DUST_UNIT;
                var dustCmp = Text.translatable("hexcasting.tooltip.media",
                        DUST_AMOUNT.format(dustCount));
                lines.add(new Pair<>(new ItemStack(HexItems.AMETHYST_DUST), dustCmp));
            }
            var name = core.getPlayerName();
            if (name != null) {
                if (!name.equals(core.storedPlayerProfile) || core.storedPlayerHeadStack == null) {
                    core.storedPlayerProfile = name;
                    var head = new ItemStack(Items.PLAYER_HEAD);
                    NBTHelper.put(head, "SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), name));
                    head.getItem().postProcessNbt(head.getOrCreateNbt());
                    core.storedPlayerHeadStack = head;
                }
                lines.add(new Pair<>(core.storedPlayerHeadStack,
                        Text.translatable("hexcasting.tooltip.lens.impetus.redstone.bound", name.getName())));
            } else {
                lines.add(new Pair<>(new ItemStack(Items.BARRIER),
                        Text.translatable("hexcasting.tooltip.lens.impetus.redstone.bound.none")));
            }
        }
    }

    private static final int[] SLOTS = {0};

    @Override
    public int[] getAvailableSlots(Direction var1) {
        return SLOTS;
    }

    @Override
    public boolean canInsert(int index, ItemStack stack, @Nullable Direction dir) {
        return this.isValid(index, stack);
    }

    @Override
    public boolean canExtract(int var1, ItemStack var2, Direction var3) {
        return false;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ItemStack getStack(int index) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public ItemStack removeStack(int index, int count) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public ItemStack removeStack(int index) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        insertMedia(stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void clear() {
        // NO-OP
    }

    public long remainingMediaCapacity() {
        if (this.storedMedia < 0) {
            return 0;
        }
        return Math.max(0, MAX_MEDIA_CAPACITY - this.storedMedia);
    }

    public long extractMediaFromInsertedItem(ItemStack stack, boolean simulate) {
        if (this.storedMedia < 0) {
            return 0;
        }
        return MediaHelper.extractMedia(stack, remainingMediaCapacity(), true, simulate);
    }

    public void setInfiniteMedia() {
        this.storedMedia = -1;
        this.sync();
    }

    @Override
    public boolean isValid(int index, ItemStack stack) {
        if (remainingMediaCapacity() == 0) {
            return false;
        }

        if (stack.isOf(HexItems.CREATIVE_UNLOCKER)) {
            return true;
        }

        var mediamount = extractMediaFromInsertedItem(stack, true);
        return mediamount > 0;
    }
}
