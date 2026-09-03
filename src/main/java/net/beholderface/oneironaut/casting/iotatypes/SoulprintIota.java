package net.beholderface.oneironaut.casting.iotatypes;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.beholderface.oneironaut.casting.idea.IdeaKeyable;
import net.beholderface.oneironaut.item.BottomlessMediaItem;
import net.beholderface.oneironaut.registry.OneironautIotaTypeRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/** A stable soulprint consisting of the entity UUID plus its display name at capture time. */
public class SoulprintIota extends Iota implements IdeaKeyable {
    public static final String TAG_UUID = "iota_uuid";
    public static final String TAG_NAME = "entity_name";

    private final UUID entity;
    private final String entityName;

    public SoulprintIota(@NotNull Tuple<UUID, String> payload) {
        this(payload.getA(), payload.getB());
    }

    public SoulprintIota(@NotNull UUID entity, @NotNull String entityName) {
        super(() -> OneironautIotaTypeRegistry.UUID);
        this.entity = entity;
        this.entityName = entityName;
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    protected boolean toleratesOther(Iota that) {
        // Player names can change, so equality is intentionally UUID-only.
        return that instanceof SoulprintIota other && entity.equals(other.entity);
    }

    public @NotNull UUID getEntity() {
        return entity;
    }

    public @NotNull String getEntityName() {
        return entityName;
    }

    @Override
    public Component display() {
        Component original = Component.translatable("hexcasting.iota.oneironaut:uuid.label", entityName);
        ItemStack soulglimmerStack = HexItems.UUID_PIGMENT.get().getDefaultInstance();
        FrozenPigment soulglimmerColor = new FrozenPigment(soulglimmerStack, entity);
        Style coloredStyle = original.getStyle().withColor(
                IXplatAbstractions.INSTANCE.getColorProvider(soulglimmerColor)
                        .getColor(BottomlessMediaItem.time, Vec3.ZERO));
        return original.copy().setStyle(coloredStyle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity);
    }

    public static final IotaType<SoulprintIota> TYPE = new IotaType<>() {
        private static final MapCodec<SoulprintIota> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                UUIDUtil.CODEC.fieldOf(TAG_UUID).forGetter(SoulprintIota::getEntity),
                Codec.STRING.fieldOf(TAG_NAME).forGetter(SoulprintIota::getEntityName)
        ).apply(instance, SoulprintIota::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, SoulprintIota> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public SoulprintIota decode(RegistryFriendlyByteBuf buffer) {
                return new SoulprintIota(buffer.readUUID(), buffer.readUtf());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, SoulprintIota value) {
                buffer.writeUUID(value.getEntity());
                buffer.writeUtf(value.getEntityName());
            }
        };

        @Override
        public MapCodec<SoulprintIota> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SoulprintIota> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public int color() {
            return 0xff_7a63bc;
        }
    };

    @Override
    public String getKey() {
        return entity + "soul";
    }

    @Override
    public boolean isValidKey(CastingEnvironment env) {
        return true;
    }
}
