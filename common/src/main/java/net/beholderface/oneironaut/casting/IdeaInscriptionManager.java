package net.beholderface.oneironaut.casting;

import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.GarbageIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.OneironautConfig;

import java.util.*;

public class IdeaInscriptionManager extends PersistentState {

    public static final String ID = Oneironaut.MOD_ID + "_ideainscription";
    //setup for Idea Inscription
    private static Map<String, NbtCompound> iotaMap = new HashMap<>();
    private static final int minuteInTicks = 20 * 60;
    private static final int hourInTicks = minuteInTicks * 60;
    private static final int lifetime = OneironautConfig.getServer().getIdeaLifetime();
    //save NBT of the map
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        Iterator<Map.Entry<String, NbtCompound>> iotaIterator = iotaMap.entrySet().iterator();
        Map.Entry<String, NbtCompound> nextIota;
        while (iotaIterator.hasNext()){
            nextIota = iotaIterator.next();
            nbt.put(nextIota.getKey(), nextIota.getValue());
        }
        return nbt;
    }

    //reassemble the map from NBT
    public static IdeaInscriptionManager createFromNbt(NbtCompound nbt){
        IdeaInscriptionManager ideas = new IdeaInscriptionManager();
        Map<String, NbtCompound> reconstructedIotaMap = new HashMap<>();
        Iterator<String> ideaIterator = nbt.getKeys().iterator();
        String currentIdeaKey;
        while (ideaIterator.hasNext()){
            currentIdeaKey = ideaIterator.next();
            reconstructedIotaMap.put(currentIdeaKey, nbt.getCompound(currentIdeaKey));
        }
        iotaMap = reconstructedIotaMap;
        return ideas;
    }

    public static IdeaInscriptionManager getServerState(MinecraftServer server){
        PersistentStateManager stateManager = server.getOverworld().getPersistentStateManager();
        //allow old idea entries to be carried over to the new ID
        PersistentState oldIdeas = stateManager.readFromFile(IdeaInscriptionManager::createFromNbt, Oneironaut.MOD_ID);
        IdeaInscriptionManager ideas = stateManager.getOrCreate(IdeaInscriptionManager::createFromNbt, IdeaInscriptionManager::new, ID);
        ideas.markDirty();
        return ideas;
    }

    public static void cleanMap(MinecraftServer server, IdeaInscriptionManager ideaState){
        ArrayList<String> KeysToRemove= new ArrayList<String>();
        //remove map entries that correspond to old entities
        Iterator<String> keys = iotaMap.keySet().iterator();
        long overworldTime = server.getOverworld().getTime();
        Oneironaut.LOGGER.info("Cleaning expired idea entries, current time is "+overworldTime);
        String currentKey;
        NbtCompound currentData;
        long timestamp;
        while (keys.hasNext()){
            //Oneironaut.LOGGER.info("About to iterate key");
            currentKey = keys.next();
            currentData = iotaMap.get(currentKey);
            timestamp = currentData.getLong("timestamp");
            //Oneironaut.LOGGER.info("Key " + currentKey + " iterated");
            if ((timestamp + lifetime) < overworldTime){
                Oneironaut.LOGGER.info("Found expired key " + currentKey + ", expired by " + (overworldTime - timestamp) + " ticks.");
                KeysToRemove.add(currentKey);
            }
        }
        if (!KeysToRemove.isEmpty()){
            Iterator<String> stringIter = KeysToRemove.iterator();
            String currentString;
            while (stringIter.hasNext()){
                currentString = stringIter.next();
                //Oneironaut.LOGGER.info("Removing key " + currentString);
                iotaMap.remove(currentString);
            }
            ideaState.markDirty();
            Oneironaut.LOGGER.info("Removed " + KeysToRemove.size() + " expired entries.");
        }
    }

    public static void writeIota(Object key, Iota iota, ServerPlayerEntity player, ServerWorld world){
        if (!(iota.getType().equals(GarbageIota.TYPE))){
            NbtCompound iotaNbt = new NbtCompound();
            iotaNbt.putLong("timestamp", world.getTime());
            iotaNbt.put("iota", IotaType.serialize(iota));
            iotaNbt.putUuid("writer", player.getUuid());
            iotaMap.put(key.toString(), iotaNbt);
        } else {
            eraseIota(key);
        }
    }

    public static void eraseIota(Object key){
        if (key.equals("everything")){
            iotaMap.clear();
        } else {
            iotaMap.remove(key.toString());
        }
    }

    public static Iota readIota(Object key, ServerWorld world){
        String keyString = key.toString();
        Iota iota = new GarbageIota();
        NbtCompound iotaNbt = getValidEntry(keyString, world);
        if (iotaNbt != null){
            if ((iotaNbt.getLong("timestamp") + lifetime) >= world.getTime()){
                iota = IotaType.deserialize(iotaNbt.getCompound("iota"), world);
            }
        }
        return iota;
    }

    public static double getIotaTimestamp(Object key, ServerWorld world){
        String keyString = key.toString();
        NbtCompound iotaNbt = getValidEntry(keyString, world);
        if (iotaNbt != null){
            return iotaNbt.getLong("timestamp");
        } else {
            return -1;
        }
    }
    public static Iota getIotaWriter(Object key, UUID suspectID, ServerWorld world){
        String keyString = key.toString();
        boolean foundSuspect = false;
        NbtCompound iotaNbt = getValidEntry(keyString, world);
        if (iotaNbt != null){
            foundSuspect = suspectID.equals(iotaNbt.getUuid("writer"));
        }
        return new BooleanIota(foundSuspect);
    }

    private static NbtCompound getValidEntry(String key, ServerWorld world){
        NbtCompound iotaNbt = iotaMap.getOrDefault(key, null);
        if (iotaNbt != null){
            if ((iotaNbt.getLong("timestamp") + lifetime) < world.getTime()){
                iotaMap.remove(key);
                //return null if it has been erased
                return null;
            } else {
                return iotaNbt;
            }
        }
        //also return null if it wasn't there in the first place
        return null;
    }
}
