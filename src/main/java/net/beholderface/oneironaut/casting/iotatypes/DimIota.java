package net.beholderface.oneironaut.casting.iotatypes;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.registry.OneironautIotaTypeRegistry;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

/** A stable, codec-backed reference to a dimension. */
public class DimIota extends Iota {
    public static final String DIM_KEY = "dim_key";

    private final String dimension;

    public DimIota(@NotNull String dimension) {
        super(() -> OneironautIotaTypeRegistry.DIM);
        this.dimension = dimension;
    }

    public DimIota(@NotNull ServerLevel world) {
        this(world.dimension().location().toString());
    }

    public DimIota(@NotNull ResourceKey<Level> worldRegistryKey) {
        this(worldRegistryKey.location().toString());
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    protected boolean toleratesOther(Iota that) {
        return that instanceof DimIota other && dimension.equals(other.dimension);
    }

    public String getDimString() {
        return dimension;
    }

    public ResourceKey<Level> getWorldKey() {
        ResourceLocation id = ResourceLocation.tryParse(dimension);
        if (id == null) {
            id = Level.OVERWORLD.location();
        }
        return ResourceKey.create(Registries.DIMENSION, id);
    }

    public @Nullable ServerLevel toWorld(MinecraftServer server) {
        return server.getLevel(getWorldKey());
    }

    private static final Map<UUID, TextTransformer> STYLE_TRANSFORMERS = new LinkedHashMap<>();

    public static void registerTransformer(TextTransformer transformer) {
        STYLE_TRANSFORMERS.put(transformer.uuid, transformer);
    }

    public static TextTransformer getTransformer(UUID uuid) {
        return STYLE_TRANSFORMERS.get(uuid);
    }

    public static void removeTransformer(UUID uuid) {
        STYLE_TRANSFORMERS.remove(uuid);
    }

    static {
        registerTransformer(TextTransformer.colorizer("minecraft:overworld", 0x00aa00));
        registerTransformer(TextTransformer.colorizer("minecraft:the_nether", 0xaa0000));
        registerTransformer(TextTransformer.colorizer("minecraft:the_end", 0xffff55));
        registerTransformer(new TextTransformer(UUID.randomUUID(), (text, id) -> {
            if (id.equals("oneironaut:noosphere")) {
                return text.copy().setStyle(text.getStyle().withColor(0xaa00aa).withBold(true));
            }
            return text;
        }));
        registerTransformer(new TextTransformer(UUID.randomUUID(), (text, id) -> {
            if (id.equals("oneironaut:deep_noosphere")) {
                return text.copy().setStyle(randomizedFormatting(text.getStyle().withColor(0xb300de)));
            }
            return text;
        }));
    }

    @Override
    public Component display() {
        Component text = Component.nullToEmpty(dimension).copy();
        String originalString = text.getString();
        text = text.copy().setStyle(text.getStyle().withColor(0x5555ff));
        for (TextTransformer transformer : STYLE_TRANSFORMERS.values()) {
            text = transformer.transform(text, dimension);
        }
        // A server-side transformer may alter style, but never the serialized dimension value.
        if (Oneironaut.isServerThread() && !text.getString().equals(originalString)) {
            text = Component.nullToEmpty(dimension).copy().setStyle(text.getStyle());
        }
        return text;
    }

    private static Style randomizedFormatting(Style original) {
        if (Platform.getEnvironment() != Env.CLIENT) {
            return original.withBold(true);
        }

        // Keep this class safe on dedicated servers: no reference to a client-only class.
        RandomSource random = RandomSource.create(Util.getMillis() / 250L);
        if (random.nextInt(3) == 0) original = original.withBold(true);
        if (random.nextInt(4) == 0) original = original.withItalic(true);
        if (random.nextInt(4) == 0) original = original.withStrikethrough(true);
        if (random.nextInt(4) == 0) original = original.withUnderlined(true);
        if (random.nextInt(10) != 0) {
            int choice = random.nextInt(3);
            if (choice == 0) {
                original = original.withObfuscated(true);
            } else {
                String fontId = choice == 1 ? "minecraft:alt" : "minecraft:illageralt";
                original = original.withFont(ResourceLocation.parse(fontId));
            }
        }
        return original;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension);
    }

    public static final IotaType<DimIota> TYPE = new IotaType<>() {
        private static final MapCodec<DimIota> CODEC = Codec.STRING.fieldOf(DIM_KEY)
                .xmap(DimIota::new, DimIota::getDimString);
        private static final StreamCodec<RegistryFriendlyByteBuf, DimIota> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public DimIota decode(RegistryFriendlyByteBuf buffer) {
                return new DimIota(buffer.readUtf());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, DimIota value) {
                buffer.writeUtf(value.getDimString());
            }
        };

        @Override
        public MapCodec<DimIota> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DimIota> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public int color() {
            return 0xff_5555ff;
        }
    };

    public static class TextTransformer {
        public final UUID uuid;
        protected final BiFunction<Component, String, Component> function;

        public TextTransformer(UUID uuid, BiFunction<Component, String, Component> function) {
            this.uuid = uuid;
            this.function = function;
        }

        public Component transform(Component text, String worldKey) {
            return function.apply(text, worldKey);
        }

        public static TextTransformer colorizer(String keyToColorize, int color) {
            return new TextTransformer(UUID.randomUUID(), (original, id) ->
                    id.equals(keyToColorize)
                            ? original.copy().setStyle(original.getStyle().withColor(color))
                            : original);
        }
    }
}
