package net.beholderface.oneironaut.casting.idea;

import at.petrak.hexcasting.api.casting.iota.GarbageIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.OneironautConfig;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IdeaInscriptionManager extends PersistentState {

    public static final String ID = Oneironaut.MOD_ID + "_ideainscription";
    //setup for Idea Inscription
    private static Map<String, IdeaEntry<?>> entryMap = new HashMap<>();
    private static final int minuteInTicks = 20 * 60;
    private static final int hourInTicks = minuteInTicks * 60;
    protected static final int lifetime = OneironautConfig.getServer().getIdeaLifetime();
    //save NBT of the map
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        Iterator<Map.Entry<String, IdeaEntry<?>>> iotaIterator = entryMap.entrySet().iterator();
        Map.Entry<String, IdeaEntry<?>> nextEntry;
        while (iotaIterator.hasNext()){
            nextEntry = iotaIterator.next();
            if (nextEntry.getValue() == null){
                continue;
            }
            nbt.put(nextEntry.getKey(), nextEntry.getValue().serialize());
        }
        return nbt;
    }

    //reassemble the map from NBT
    public static IdeaInscriptionManager createFromNbt(NbtCompound nbt){
        IdeaInscriptionManager ideas = new IdeaInscriptionManager();
        Map<String, IdeaEntry<?>> reconstructedIotaMap = new HashMap<>();
        Iterator<String> ideaIterator = nbt.getKeys().iterator();
        String currentIdeaKey;
        while (ideaIterator.hasNext()){
            currentIdeaKey = ideaIterator.next();
            NbtCompound currentNbt = nbt.getCompound(currentIdeaKey);
            if (currentNbt.contains(IdeaEntry.TAG_ENTRY_TYPE)){
                IdeaEntry.EntryType type = IdeaEntry.EntryType.valueOf(currentNbt.getString(IdeaEntry.TAG_ENTRY_TYPE));
                reconstructedIotaMap.put(currentIdeaKey, IdeaEntry.deserialize(currentNbt, Oneironaut.getCachedServer().getOverworld()));
            } else {
                reconstructedIotaMap.put(currentIdeaKey, IdeaEntry.deserializeLegacyEntry(currentNbt, Oneironaut.getCachedServer().getOverworld()));
            }
        }
        entryMap = reconstructedIotaMap;
        return ideas;
    }

    public static IdeaInscriptionManager getServerState(MinecraftServer server){
        PersistentStateManager stateManager = server.getOverworld().getPersistentStateManager();
        IdeaInscriptionManager ideas = stateManager.getOrCreate(IdeaInscriptionManager::createFromNbt, IdeaInscriptionManager::new, ID);
        ideas.markDirty();
        return ideas;
    }

    public static void cleanMap(MinecraftServer server, IdeaInscriptionManager ideaState){
        Set<String> keysToRemove= new HashSet<>();
        //remove map entries that correspond to old entities
        Iterator<String> keys = entryMap.keySet().iterator();
        long overworldTime = server.getOverworld().getTime();
        Oneironaut.LOGGER.info("Cleaning expired idea entries, current time is {}", overworldTime);
        String currentKey;
        IdeaEntry<?> currentData;
        long timestamp;
        while (keys.hasNext()){
            //Oneironaut.LOGGER.info("About to iterate key");
            currentKey = keys.next();
            currentData = entryMap.get(currentKey);
            if (currentData == null){
                continue;
            }
            timestamp = currentData.creationTimestamp;
            //Oneironaut.LOGGER.info("Key " + currentKey + " iterated");
            if (currentData.isExpired(overworldTime)){
                Oneironaut.LOGGER.info("Found expired key {}, expired by {} ticks.", currentKey, overworldTime - timestamp);
                keysToRemove.add(currentKey);
            }
        }
        if (!keysToRemove.isEmpty()){
            Iterator<String> stringIter = keysToRemove.iterator();
            String currentString;
            while (stringIter.hasNext()){
                currentString = stringIter.next();
                //Oneironaut.LOGGER.info("Removing key " + currentString);
                entryMap.remove(currentString);
            }
            ideaState.markDirty();
            Oneironaut.LOGGER.info("Removed {} expired entries.", keysToRemove.size());
        }
    }

    public static void writeEntry(IdeaKeyable key, IdeaEntry<?> entry){
        if (entry.type == IdeaEntry.EntryType.IOTA && entry.payload instanceof Iota iota){
            if (!(iota.getType().equals(GarbageIota.TYPE))){
                entryMap.put(key.getKey(), entry);
            } else {
                eraseEntry(key);
            }
        } else {
            entryMap.put(key.getKey(), entry);
        }
    }

    public static void eraseEntry(IdeaKeyable key){
        if (key.getKey().equals("everything")){
            entryMap.clear();
        } else {
            entryMap.remove(key.getKey());
        }
    }

    @Nullable
    public static IdeaEntry<?> getEntry(IdeaKeyable key, ServerWorld world, @Nullable IdeaEntry.EntryType type){
        String keyString = key.getKey();
        IdeaEntry<?> entry = getValidEntry(keyString, world);
        if (entry != null){
            if (type == null || entry.type == type){
                return entry;
            }
        }
        return null;
    }

    @Nullable
    public static IdeaEntry<?> getEntry(IdeaKeyable key, ServerWorld world){
        return getEntry(key, world, null);
    }

    public static double getEntryTimestamp(IdeaKeyable key, ServerWorld world){
        String keyString = key.getKey();
        IdeaEntry<?> entry = getValidEntry(keyString, world);
        if (entry != null){
            return entry.creationTimestamp;
        } else {
            return -1;
        }
    }
    public static UUID getEntryWriter(IdeaKeyable key, ServerWorld world){
        String keyString = key.getKey();
        IdeaEntry<?> entry = getValidEntry(keyString, world);
        if (entry != null){
            return entry.writerID;
        }
        return null;
    }

    private static IdeaEntry<?> getValidEntry(String key, ServerWorld world){
        IdeaEntry<?> entry = entryMap.get(key);
        if (entry != null){
            if (entry.isExpired(world.getTime())){
                entryMap.remove(key);
            } else {
                return entry;
            }
        }
        //also return null if it wasn't there in the first place
        return null;
    }
}
