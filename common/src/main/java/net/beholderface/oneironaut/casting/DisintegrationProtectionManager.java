package net.beholderface.oneironaut.casting;

import at.petrak.hexcasting.api.utils.NBTHelper;
import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DisintegrationProtectionManager extends PersistentState {

    private final List<DisintegrationProtectionEntry> entries = new ArrayList<>();
    public static final String TAG_ENTRIES = "entries";

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (DisintegrationProtectionEntry entry : entries){
            list.add(entry.serialize());
        }
        NBTHelper.putList(nbt, TAG_ENTRIES, list);
        return nbt;
    }

    public static DisintegrationProtectionManager createFromNbt(NbtCompound nbt){
        DisintegrationProtectionManager manager = new DisintegrationProtectionManager();
        NbtList list = NBTHelper.getList(nbt, TAG_ENTRIES, NbtList.COMPOUND_TYPE);
        if (list == null){
            throw new IllegalStateException("NbtCompound supplied to DisintegrationProtectionManager#createFromNbt did not contain the proper list");
        }
        manager.entries.clear();
        for (NbtElement element : list){
            if (element instanceof NbtCompound compound){
                manager.entries.add(DisintegrationProtectionEntry.deserialize(compound));
            }
        }
        return manager;
    }

    public static DisintegrationProtectionManager getServerState(MinecraftServer server){
        PersistentStateManager stateManager = server.getOverworld().getPersistentStateManager();
        DisintegrationProtectionManager manager = stateManager.getOrCreate(DisintegrationProtectionManager::createFromNbt, DisintegrationProtectionManager::new, Oneironaut.MOD_ID);
        manager.markDirty();
        return manager;
    }

    public void cleanEntries(){
        var iterator = this.entries.iterator();
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

    public boolean addEntry(DisintegrationProtectionEntry entry){
        return entries.add(entry);
    }

    @Nullable
    public DisintegrationProtectionEntry getProtectionEntry(Vec3d pos){
        var iterator = this.entries.iterator();
        DisintegrationProtectionEntry entry;
        while (iterator.hasNext()){
            entry = iterator.next();
            if (!entry.isBroken()){
                if (entry.canProtect(pos)){
                    return entry;
                }
            } else {
                iterator.remove();
            }
        }
        return null;
    }

    public static class DisintegrationProtectionEntry {
        private Box bounds;
        private long hits = 0;
        private long durability = 1;

        public DisintegrationProtectionEntry(BlockPos cornerA, BlockPos cornerB, long durability){
            new DisintegrationProtectionEntry(new Box(cornerA, cornerB), 0, durability);
        }

        private DisintegrationProtectionEntry(Box bounds, long hits, long durability){
            this.bounds = bounds;
            this.hits = hits;
            this.durability = durability;
        }

        public Box getBounds(){
            return bounds;
        }

        public boolean canProtect(Vec3d pos){
            return this.bounds.contains(pos);
        }

        public boolean canProtect(Vec3i pos){
            return canProtect(Vec3d.of(pos));
        }

        public long getHits() {
            return hits;
        }

        public long getDurability() {
            return durability;
        }

        public void hit(long addedHits){
            this.hits += addedHits;
        }

        public void hit(){
            this.hit(1);
        }

        public boolean isBroken(){
            return this.getHits() >= this.getDurability();
        }

        public static final String TAG_HITS = "hits";
        public static final String TAG_DURABILITY = "durability";
        public static final String TAG_CORNER_1 = "corner1";
        public static final String TAG_CORNER_2 = "corner2";
        public NbtCompound serialize(){
            NbtCompound nbt = new NbtCompound();
            nbt.putLong(TAG_HITS, this.getHits());
            NBTHelper.putCompound(nbt, TAG_CORNER_1, NbtHelper.fromBlockPos(new BlockPos((int) this.bounds.minX, (int) this.bounds.minY, (int) this.bounds.minZ)));
            NBTHelper.putCompound(nbt, TAG_CORNER_2, NbtHelper.fromBlockPos(new BlockPos((int) this.bounds.maxX, (int) this.bounds.maxY, (int) this.bounds.maxZ)));
            return nbt;
        }

        public static DisintegrationProtectionEntry deserialize(NbtCompound compound){
            long hits = compound.getLong(TAG_HITS);
            NbtCompound compoundA = NBTHelper.getCompound(compound, TAG_CORNER_1);
            NbtCompound compoundB = NBTHelper.getCompound(compound, TAG_CORNER_2);
            if (compoundA == null || compoundB == null){
                throw new IllegalStateException("NbtCompound supplied to DisintegrationProtectionEntry#deserialize did not contain one or both corner tags.");
            }
            BlockPos cornerA = NbtHelper.toBlockPos(compoundA);
            BlockPos cornerB = NbtHelper.toBlockPos(compoundB);
            long durability = compound.getLong(TAG_DURABILITY);
            return new DisintegrationProtectionEntry(new Box(cornerA, cornerB), hits, durability);
        }
    }
}
