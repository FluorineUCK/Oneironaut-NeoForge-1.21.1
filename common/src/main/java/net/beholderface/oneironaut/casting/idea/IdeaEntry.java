package net.beholderface.oneironaut.casting.idea;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.OneironautConfig;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public class IdeaEntry<T> {

    public static final String TAG_ENTRY_TYPE = "entryType";
    public static final String TAG_ENTRY_DATA = "entryData";
    public static final String TAG_ENTRY_TIMESTAMP = "entryTimestamp";
    public static final String TAG_ENTRY_WRITER_ID = "entryWriterID";
    public static final String TAG_ENTRY_WRITER_NAME = "entryWriterName";

    public final T payload;
    public final EntryType type;
    public final long creationTimestamp;
    public final long lifetime;
    @Nullable
    public final UUID writerID;
    @Nullable
    public final Text writerName;

    public IdeaEntry(T payload, long timestamp, @Nullable Entity writer){
        assert payload != null;
        this.payload = payload;
        EntryType type = EntryType.getTypeFromObject(payload);
        if (type == null){
            throw new IllegalArgumentException("Could not find appropriate idea entry type for payload " + payload);
        }
        this.type = type;
        this.creationTimestamp = timestamp;
        this.lifetime = (long) (OneironautConfig.getServer().getIdeaLifetime() * type.lifetimeMultiplier);
        if (writer != null){
            this.writerID = writer.getUuid();
            this.writerName = writer.getName();
        } else {
            this.writerName = Text.empty();
            this.writerID = Util.NIL_UUID;
        }
    }
    public IdeaEntry(T payload, long timestamp, Text writerName, UUID writerID){
        assert payload != null;
        this.payload = payload;
        EntryType type = EntryType.getTypeFromObject(payload);
        if (type == null){
            throw new IllegalArgumentException("Could not find appropriate idea entry type for payload " + payload);
        }
        this.type = type;
        this.creationTimestamp = timestamp;
        this.lifetime = (long) (OneironautConfig.getServer().getIdeaLifetime() * type.lifetimeMultiplier);
        this.writerName = writerName != null ? writerName : Text.empty();
        this.writerID = writerID != null ? writerID : Util.NIL_UUID;
    }

    public NbtCompound serialize(){
        NbtCompound inner = this.type.serializer.apply(this);
        if (inner == null){
            return null;
        }
        NbtCompound outer = new NbtCompound();
        NBTHelper.putCompound(outer, TAG_ENTRY_DATA, inner);
        outer.putString(TAG_ENTRY_TYPE, this.type.toString());
        outer.putLong(TAG_ENTRY_TIMESTAMP, this.creationTimestamp);
        outer.putUuid(TAG_ENTRY_WRITER_ID, this.writerID);
        outer.putString(TAG_ENTRY_WRITER_NAME, Text.Serializer.toJson(this.writerName));
        return outer;
    }

    public static IdeaEntry<?> deserialize(NbtCompound nbt, ServerWorld world){
        return EntryType.valueOf(nbt.getString(TAG_ENTRY_TYPE)).deserializer.apply(nbt, world);
    }

    public boolean isExpired(long currentTime){
        return this.creationTimestamp + this.lifetime < currentTime;
    }

    public enum EntryType {
        IOTA(IdeaEntry::deserializeIotaEntry, IdeaEntry::serializeIotaEntry, (checked)->checked instanceof Iota, 1.0),
        ENTITY(IdeaEntry::deserializeEntityEntry, IdeaEntry::serializeEntityEntry, (checked)->checked instanceof Entity, 1.0);

        public final BiFunction<NbtCompound, ServerWorld, IdeaEntry<?>> deserializer;
        public final Function<IdeaEntry<?>, NbtCompound> serializer;
        public final Function<Object, Boolean> checker;
        public final double lifetimeMultiplier;

        EntryType(BiFunction<NbtCompound, ServerWorld, IdeaEntry<?>> deserializer, Function<IdeaEntry<?>, NbtCompound> serializer,
                 Function<Object, Boolean> checker, double lifetimeMultiplier){
            this.deserializer = deserializer;
            this.serializer = serializer;
            this.checker = checker;
            this.lifetimeMultiplier = lifetimeMultiplier;
        }

        @Nullable
        public static EntryType getTypeFromObject(Object checked){
            for (EntryType type : EnumSet.allOf(EntryType.class)){
                if (type.checker.apply(checked)){
                    return type;
                }
            }
            return null;
        }
    }

    public static IdeaEntry<Iota> deserializeLegacyEntry(NbtCompound nbt, ServerWorld world){
        NbtCompound iotaNbt = nbt.getCompound("iota");
        if ((iotaNbt.getLong("timestamp") + IdeaInscriptionManager.lifetime) >= world.getTime()){
            Iota iota = IotaType.deserialize(iotaNbt/*.getCompound("iota")*/, world);
            UUID uuid = nbt.getUuid("writer");
            Text name = Text.literal("???");
            if (world.getServer().getPlayerManager().getPlayer(uuid) != null){
                name = world.getServer().getPlayerManager().getPlayer(uuid).getName(); //why is intellij complaining here
            }
            return new IdeaEntry<Iota>(iota, nbt.getLong("timestamp"), name, uuid);
        }
        return null;
    }

    public static final String TAG_IOTA_DATA = "iotaData";

    protected static IdeaEntry<Iota> deserializeIotaEntry(NbtCompound nbt, ServerWorld world){
        NbtCompound inner = nbt.getCompound(TAG_ENTRY_DATA);
        return new IdeaEntry<Iota>(IotaType.deserialize(inner.getCompound(TAG_IOTA_DATA), world), nbt.getLong(TAG_ENTRY_TIMESTAMP),
                Text.Serializer.fromJson(nbt.getString(TAG_ENTRY_WRITER_NAME)), nbt.getUuid(TAG_ENTRY_WRITER_ID));
    }
    protected static NbtCompound serializeIotaEntry(IdeaEntry<?> entry){
        if (entry.payload instanceof Iota payload){
            NbtCompound out = new NbtCompound();
            NBTHelper.putCompound(out, TAG_IOTA_DATA, IotaType.serialize(payload));
            return out;
        } else {
            return null;
        }
    }

    public static final String TAG_ENTITY_TYPE = "entityType";
    public static final String TAG_ENTITY_DATA = "entityData";

    protected static IdeaEntry<Entity> deserializeEntityEntry(NbtCompound nbt, ServerWorld world){
        NbtCompound inner = nbt.getCompound(TAG_ENTRY_DATA);
        Entity entity = Registries.ENTITY_TYPE.get(new Identifier(inner.getString(TAG_ENTITY_TYPE))).create(world);
        if (entity == null){
            return null;
        } else {
            entity.readNbt(inner.getCompound(TAG_ENTITY_DATA));
        }
        return new IdeaEntry<Entity>(entity, nbt.getLong(TAG_ENTRY_TIMESTAMP),
                Text.Serializer.fromJson(nbt.getString(TAG_ENTRY_WRITER_NAME)), nbt.getUuid(TAG_ENTRY_WRITER_ID));
    }
    protected static NbtCompound serializeEntityEntry(IdeaEntry<?> entry){
        if (entry.payload instanceof Entity payload){
            NbtCompound entityData = payload.writeNbt(new NbtCompound());
            NbtCompound out = new NbtCompound();
            NBTHelper.putCompound(out, TAG_ENTITY_DATA, entityData);
            out.putString(TAG_ENTRY_TYPE, payload.getType().toString());
            return out;
        } else {
            return null;
        }
    }
}
