package net.beholderface.oneironaut.casting.conceptmodification;

import at.petrak.hexcasting.api.utils.NBTHelper;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.block.ConceptCoreBlock;
import net.beholderface.oneironaut.block.ConceptModifierBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ConceptModifierManager extends PersistentState {

    public static final String ID = Oneironaut.MOD_ID + "_conceptmodification";

    private final Map<UUID, Map<BlockPos, ConceptModifier>> modifierMap = new HashMap<>();

    @Nullable
    public ConceptModifier getModifier(UUID playerID, UUID modifierID){
        Map<BlockPos, ConceptModifier> map = this.modifierMap.get(playerID);
        if (map != null){
            return map.get(BlockPos.fromLong(modifierID.getLeastSignificantBits()));
        }
        return null;
    }
    @Nullable
    public ConceptModifier getModifier(ServerPlayerEntity player, UUID id){
        return this.getModifier(player.getUuid(), id);
    }

    public List<ConceptModifier> getAllModifiers(UUID playerID){
        List<ConceptModifier> modifiers = new ArrayList<>();
        if (this.modifierMap.containsKey(playerID)){
            return this.modifierMap.get(playerID).values().stream().toList();
        }
        return modifiers;
    }
    public List<ConceptModifier> getAllModifiers(ServerPlayerEntity player){
        return this.getAllModifiers(player.getUuid());
    }

    public boolean hasModifierType(UUID playerID, ConceptModifier.ModifierType type){
        Map<BlockPos, ConceptModifier> map = this.modifierMap.get(playerID);
        if (map != null){
            for (ConceptModifier modifier : map.values()){
                if (modifier.type == type){
                    return true;
                }
            }
        }
        return false;
    }
    public boolean hasModifierType(ServerPlayerEntity player, ConceptModifier.ModifierType type){
        return this.hasModifierType(player.getUuid(), type);
    }

    public void addModifier(UUID playerID, ConceptModifier modifier){
        Map<BlockPos, ConceptModifier> map;
        if (this.modifierMap.containsKey(playerID)){
            map = this.modifierMap.get(playerID);
        } else {
            map = new HashMap<>();
            this.modifierMap.put(playerID, map);
        }
        map.put(modifier.hostPos, modifier);
        this.markDirty();
    }
    public void addModifier(ServerPlayerEntity player, ConceptModifier modifier){
        this.addModifier(player.getUuid(), modifier);
    }

    public void removeModifier(UUID playerID, BlockPos modifierPos){
        Map<BlockPos, ConceptModifier> map = this.modifierMap.get(playerID);
        if (map != null){
            if (Oneironaut.getCachedServer() != null){
                ServerPlayerEntity player = Oneironaut.getCachedServer().getPlayerManager().getPlayer(playerID);
                ConceptModifier modifier = map.get(modifierPos);
                if (player != null && modifier != null){
                    modifier.onRemove(player);
                }
            }
            map.remove(modifierPos);
            this.markDirty();
        }
    }
    public void removeModifier(UUID playerID, UUID modifierID){
        this.removeModifier(playerID, BlockPos.fromLong(modifierID.getLeastSignificantBits()));
    }
    public void removeModifier(UUID playerID, ConceptModifier modifier){
        this.removeModifier(playerID, modifier.hostPos);
    }

    public int clearPlayerModifiers(UUID playerID){
        Map<BlockPos, ConceptModifier> map = this.modifierMap.get(playerID);
        if (map != null){
            Iterator<ConceptModifier> iterator = map.values().iterator();
            while (iterator.hasNext()){
                ConceptModifier entry = iterator.next();
                this.removeModifier(playerID, entry);
            }
            return map.size();
        }
        return -1;
    }

    public int removeAllModifiers(){
        int i = 0;
        for (Map<BlockPos, ConceptModifier> map : this.modifierMap.values()){
            i += map.size();
        }
        this.modifierMap.clear();
        this.markDirty();
        return i;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        for (UUID playerID : modifierMap.keySet()){
            NbtCompound playerNbt = new NbtCompound();
            Map<BlockPos, ConceptModifier> playerMap = modifierMap.get(playerID);
            for (BlockPos pos : playerMap.keySet()){
                NBTHelper.putCompound(playerNbt, String.valueOf(pos.asLong()), playerMap.get(pos).serialize());
            }
            NBTHelper.putCompound(nbt, playerID.toString(), playerNbt);
        }
        return nbt;
    }

    public static ConceptModifierManager createFromNbt(NbtCompound nbt){
        ConceptModifierManager manager = new ConceptModifierManager();
        for (String s : nbt.getKeys()){
            NbtCompound playerNBT = nbt.getCompound(s);
            for (String s2 : playerNBT.getKeys()){
                ConceptModifier modifier = ConceptModifier.deserialize(playerNBT.getCompound(s2));
                manager.addModifier(UUID.fromString(s), modifier);
            }
        }
        return manager;
    }

    public static ConceptModifierManager getServerState(MinecraftServer server){
        PersistentStateManager stateManager = Oneironaut.getDeepNoosphere().getPersistentStateManager();
        ConceptModifierManager manager = stateManager.getOrCreate(ConceptModifierManager::createFromNbt,
                ConceptModifierManager::new, ID);
        manager.markDirty();
        return manager;
    }

    public void verifyModifiers(){
        ServerWorld world = Oneironaut.getDeepNoosphere();
        if (world == null){
            return;
        }
        int i = 0;
        for (UUID playerID : this.modifierMap.keySet()){
            Iterator<ConceptModifier> iterator = this.modifierMap.get(playerID).values().iterator();
            ConceptModifier modifier = null;
            while (iterator.hasNext()){
                modifier = iterator.next();
                if (!(world.getBlockState(modifier.hostPos).getBlock() instanceof ConceptModifierBlock) ||
                        !(world.getBlockState(modifier.corePos).getBlock() instanceof ConceptCoreBlock)){
                    iterator.remove();
                    i++;
                    /*if (modifier.type == ConceptModifier.ModifierType.ATTRIBUTE){
                        ServerPlayerEntity hostPlayer = Oneironaut.getCachedServer().getPlayerManager().getPlayer(playerID);
                        if (hostPlayer != null)
                    }*/
                }
            }
            /*this.modifierMap.get(playerID).values().removeIf(modifier ->
                    !(world.getBlockState(modifier.hostPos).getBlock() instanceof ConceptModifierBlock) ||
                    !(world.getBlockState(modifier.corePos).getBlock() instanceof ConceptCoreBlock));*/
        }
        this.markDirty();
        Oneironaut.LOGGER.info("Removed {} invalid concept modifiers.", i);
    }
}
