package net.beholderface.oneironaut.casting;

import at.petrak.hexcasting.api.utils.NBTHelper;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifier;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifierManager;
import net.beholderface.oneironaut.registry.OneironautMiscRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DisintegrationProtectionManager extends SavedData {

    public static final String ID = Oneironaut.MOD_ID + "_disintegration";
    private final Map<UUID, DisintegrationProtectionEntry> entries = new HashMap<>();
    public static final String TAG_ENTRIES = "entries";

    public DisintegrationProtectionManager(){

    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (DisintegrationProtectionEntry entry : entries.values()){
            list.add(entry.serialize());
        }
        //Oneironaut.LOGGER.info(list.asString());
        NBTHelper.putList(nbt, TAG_ENTRIES, list);
        return nbt;
    }

    public static DisintegrationProtectionManager createFromNbt(CompoundTag nbt, HolderLookup.Provider registries){
        DisintegrationProtectionManager manager = new DisintegrationProtectionManager();
        ListTag list = NBTHelper.getList(nbt, TAG_ENTRIES, Tag.TAG_COMPOUND);
        if (list == null){
            throw new IllegalStateException("NbtCompound supplied to DisintegrationProtectionManager#createFromNbt did not contain the proper list");
        }
        manager.entries.clear();
        for (Tag element : list){
            if (element instanceof CompoundTag compound){
                DisintegrationProtectionEntry entry = DisintegrationProtectionEntry.deserialize(compound);
                manager.entries.put(entry.getUuid(), entry);
            }
        }
        Oneironaut.LOGGER.info("Reconstructed protection map with {} entries", manager.entries.size());
        return manager;
    }

    public static DisintegrationProtectionManager getServerState(MinecraftServer server){
        DimensionDataStorage stateManager = Oneironaut.getDeepNoosphere().getDataStorage();
        DisintegrationProtectionManager manager = stateManager.computeIfAbsent(
                new SavedData.Factory<>(DisintegrationProtectionManager::new,
                        DisintegrationProtectionManager::createFromNbt), ID);
        manager.setDirty();
        return manager;
    }

    public void cleanEntries(){
        var iterator = this.entries.values().iterator();
        DisintegrationProtectionEntry entry;
        int removed = 0;
        while (iterator.hasNext()){
            entry = iterator.next();
            if (entry.isBroken()){
                iterator.remove();
                removed++;
            }
        }
        Oneironaut.LOGGER.info("Disintegration manager found and removed {} dead entries", removed);
    }

    public void removeEntry(DisintegrationProtectionEntry entry){
        this.entries.remove(entry.getUuid());
        this.setDirty();
    }

    public void addEntry(DisintegrationProtectionEntry entry){
        entries.put(entry.getUuid(), entry);
        this.setDirty();
    }

    @Nullable
    public DisintegrationProtectionEntry getProtectionEntry(Vec3 pos){
        var iterator = this.entries.values().iterator();
        DisintegrationProtectionEntry entry;
        while (iterator.hasNext()){
            entry = iterator.next();
            if (!entry.isBroken()){
                //Oneironaut.LOGGER.info("Entry not broken");
                if (entry.canProtect(pos)){
                    //Oneironaut.LOGGER.info("Can protect {}", pos.toString());
                    return entry;
                }/* else {
                    //Oneironaut.LOGGER.info("Cannot protect {}", pos.toString());
                }*/
            } else {
                //Oneironaut.LOGGER.info("Entry broken");
                iterator.remove();
                this.setDirty();
            }
        }
        return null;
    }

    public static class DisintegrationProtectionEntry {
        private final AABB bounds;
        private long hits = 0;
        private long durability = 1;
        private final UUID uuid;

        public DisintegrationProtectionEntry(BlockPos cornerA, BlockPos cornerB, long durability){
            this.bounds = new AABB(Vec3.atLowerCornerOf(cornerA), Vec3.atLowerCornerOf(cornerB.offset(1,1,1)));
            this.hits = 0;
            this.durability = durability;
            this.uuid = UUID.randomUUID();
        }

        private DisintegrationProtectionEntry(AABB bounds, long hits, long durability, @Nullable UUID uuid){
            this.bounds = bounds;
            this.hits = hits;
            this.durability = durability;
            this.uuid = uuid != null ? uuid : UUID.randomUUID();
        }

        public DisintegrationProtectionEntry indestructible(AABB bounds){
            return new DisintegrationProtectionEntry(bounds, -1L, -1L, MiscAPIKt.toUUID(MiscAPIKt.toBlockPos(bounds.getCenter())));
        }

        public AABB getBounds(){
            return bounds;
        }

        public boolean canProtect(Vec3 pos){
            return !this.isBroken() && MiscAPIKt.containsPermissive(this.bounds, pos);
        }

        public boolean canProtect(Vec3i pos){
            return canProtect(Vec3.atLowerCornerOf(pos));
        }

        public long getHits() {
            return hits != -1 ? hits : Long.MIN_VALUE;
        }

        public long getDurability() {
            return durability != -1 ? durability : Long.MAX_VALUE;
        }

        public UUID getUuid() {
            return uuid;
        }

        private void addHits(long addedHits){
            if (this.hits != -1){
                this.hits += addedHits;
            }
        }

        public boolean hit(long addedHits, Vec3 pos, ServerLevel world){
            boolean startedBroken = this.isBroken();
            this.addHits(addedHits);
            //Oneironaut.LOGGER.info("Entry {} hit for {} points, for a total of {}", this.uuid.toString(), addedHits, this.getHits());
            boolean newlyBroken = (this.isBroken() && !startedBroken);
            if (world != null){
                ClientboundSoundPacket hitSoundMessage = getHitMessage(pos, world, newlyBroken);
                for (ServerPlayer player : world.players()){
                    player.connection.send(hitSoundMessage);
                }
            }
            return newlyBroken;
        }

        private static @NotNull ClientboundSoundPacket getHitMessage(Vec3 pos, ServerLevel world, boolean newlyBroken) {
            if (newlyBroken){
                return new ClientboundSoundPacket(Holder.direct(SoundEvents.GLASS_BREAK), SoundSource.BLOCKS,
                        pos.x, pos.y, pos.z, 0.5f, 0.2f, world.getSeed());
            } else {
                return new ClientboundSoundPacket(Holder.direct(SoundEvents.GLASS_HIT), SoundSource.BLOCKS,
                        pos.x, pos.y, pos.z, 1f, 0.5f, world.getSeed());
            }
        }

        public boolean hit(Vec3 pos, ServerLevel world){
            return this.hit(1, pos, world);
        }

        public boolean isBroken(){
            return this.getHits() >= this.getDurability();
        }

        public static final String TAG_HITS = "hits";
        public static final String TAG_DURABILITY = "durability";
        public static final String TAG_CORNER_1 = "corner1";
        public static final String TAG_CORNER_2 = "corner2";
        public static final String TAG_UUID = "uuid";
        public CompoundTag serialize(){
            CompoundTag nbt = new CompoundTag();
            nbt.putLong(TAG_HITS, this.hits);
            nbt.putLong(TAG_DURABILITY, this.durability);
            nbt.putUUID(TAG_UUID, this.getUuid());
            nbt.put(TAG_CORNER_1, NbtUtils.writeBlockPos(new BlockPos((int) this.bounds.minX, (int) this.bounds.minY, (int) this.bounds.minZ)));
            nbt.put(TAG_CORNER_2, NbtUtils.writeBlockPos(new BlockPos((int) this.bounds.maxX, (int) this.bounds.maxY, (int) this.bounds.maxZ)));
            return nbt;
        }

        public static DisintegrationProtectionEntry deserialize(CompoundTag compound){
            long hits = compound.getLong(TAG_HITS);
            Optional<BlockPos> cornerAResult = NbtUtils.readBlockPos(compound, TAG_CORNER_1);
            Optional<BlockPos> cornerBResult = NbtUtils.readBlockPos(compound, TAG_CORNER_2);
            if (cornerAResult.isEmpty() || cornerBResult.isEmpty()){
                throw new IllegalStateException("NbtCompound supplied to DisintegrationProtectionEntry#deserialize did not contain one or both corner tags.");
            }
            BlockPos cornerA = cornerAResult.get();
            BlockPos cornerB = cornerBResult.get();
            long durability = compound.getLong(TAG_DURABILITY);
            UUID uuid = compound.getUUID(TAG_UUID);
            return new DisintegrationProtectionEntry(
                    new AABB(Vec3.atLowerCornerOf(cornerA), Vec3.atLowerCornerOf(cornerB)), hits, durability, uuid);
        }
    }

    //moved here to maybe fix effect registration order issue
    private static DisintegrationProtectionManager.DisintegrationProtectionEntry latestFoundEntry = null;
    public static void handleDisintegrationTick(LivingEntity entity){
        DisintegrationProtectionManager.DisintegrationProtectionEntry entry = latestFoundEntry;
        Vec3 pos = entity.getEyePosition();
        if (!entity.level().isClientSide && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()))){
            DisintegrationProtectionManager manager = DisintegrationProtectionManager.getServerState(((ServerLevel)entity.level()).getServer());
            if (entry == null || !entry.canProtect(pos)){
                entry = manager.getProtectionEntry(pos);
            }
            boolean conceptProtected = false;
            if (entity instanceof ServerPlayer player){
                conceptProtected = ConceptModifierManager.getServerState(player.server).hasModifierType(player, ConceptModifier.ModifierType.ANTIEROSION);
            }
            if ((entry != null && !entry.isBroken()) || conceptProtected){
                MobEffectInstance instance = entity.getEffect(OneironautMiscRegistry.DISINTEGRATION_PROTECTION);
                if (instance == null || instance.getDuration() <= 40){
                    entity.addEffect(new MobEffectInstance(
                            OneironautMiscRegistry.DISINTEGRATION_PROTECTION, 100, 0, true, true));
                }
                if (entry != null && !entry.isBroken()){
                    boolean hit = entry.hit(2, pos,(ServerLevel) entity.level());
                    if (!hit){
                        latestFoundEntry = entry;
                    } else {
                        latestFoundEntry = null;
                        manager.removeEntry(entry);
                    }
                }
            }
        }
        Oneironaut.processDisintegration(entity);
    }
}
