package net.beholderface.oneironaut.casting.iotatypes;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.utils.HexUtils;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.beholderface.oneironaut.MiscClientAPIKt;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.registry.OneironautIotaTypeRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class DimIota extends Iota {
    public DimIota(@NotNull String dim){
        super(OneironautIotaTypeRegistry.DIM, dim);
    }
    public DimIota(@NotNull ServerWorld world){
        super(OneironautIotaTypeRegistry.DIM, world.getRegistryKey().getValue().toString());
    }
    public DimIota(@NotNull RegistryKey<World> worldRegistryKey){
        super(OneironautIotaTypeRegistry.DIM, worldRegistryKey.getValue().toString());
    }
    public static final String DIM_KEY = "dim_key";

    /*public NbtElement getKey(){
        var ctag = HexUtils.downcast(this.payload, NbtCompound.TYPE);
        return (RegistryKey<World>) ctag.get("dim_key");
    }*/

    @Override
    public boolean isTruthy() {
        return true;
    }

    protected boolean toleratesOther(Iota that) {
        if (that.getType().equals(this.type)){
            DimIota other = (DimIota) that;
            return this.payload.equals(other.payload);
        }
        return false;
    }

    public String getDimString(){
        return this.payload.toString();
    }
    public RegistryKey<World> getWorldKey(){
        return RegistryKey.of(RegistryKeys.WORLD, new Identifier(this.payload.toString()));
    }
    public ServerWorld toWorld(MinecraftServer server){
        return server.getWorld(this.getWorldKey());
    }

    public @NotNull NbtElement serialize() {
        var data = new NbtCompound();
        var payload = this.payload;
        data.putString(DIM_KEY, (String) payload);
        return data;
    }

    private static final Map<UUID, TextTransformer> styleTransformers = new HashMap<>();

    public static void registerTransformer(TextTransformer transformer){
        styleTransformers.put(transformer.uuid, transformer);
    }
    public static TextTransformer getTransformer(UUID uuid){
        return styleTransformers.get(uuid);
    }
    public static void removeTransformer(UUID uuid){
        styleTransformers.remove(uuid);
    }

    public static IotaType<DimIota> TYPE = new IotaType<>() {
        @Override
        public DimIota deserialize(NbtElement tag, ServerWorld world) throws IllegalArgumentException {
            var ctag = HexUtils.downcast(tag, NbtCompound.TYPE);
            return new DimIota(ctag.getString(DIM_KEY));
        }

        static {
            registerTransformer(TextTransformer.colorizer("minecraft:overworld", 0x00aa00));
            registerTransformer(TextTransformer.colorizer("minecraft:the_nether", 0xaa0000));
            registerTransformer(TextTransformer.colorizer("minecraft:the_end", 0xffff55));
            registerTransformer(new TextTransformer(UUID.randomUUID(), (t, s)->{
                if (s.equals("oneironaut:noosphere")){
                    return t.copy().setStyle(t.getStyle().withColor(0xaa00aa).withBold(true));
                }
                return t;
            }));
            registerTransformer(new TextTransformer(UUID.randomUUID(), (t, s)->{
                if (s.equals("oneironaut:deep_noosphere")){
                    return t.copy().setStyle(randomizedFormatting(t.getStyle().withColor(0xb300de)));
                }
                return t;
            }));
        }

        @Override
        public Text display(NbtElement tag) {
            var ctag = HexUtils.downcast(tag, NbtCompound.TYPE);
            String worldKey = ctag.getString(DIM_KEY);
            Text text = Text.of(worldKey).copy();
            String originalString = text.getString();
            text = text.copy().setStyle(text.getStyle().withColor(0x5555ff)); //default coloring
            for (TextTransformer transformer : styleTransformers.values()){
                text = transformer.transform(text, worldKey);
            }
            //don't let people change the actual string of the text server-side, it might break hexes that rely on stuff like Scrivener's
            if (Oneironaut.getCachedServer() != null && Thread.currentThread() == Oneironaut.getCachedServer().getThread()
                    && !text.getString().equals(originalString)){
                text = Text.of(worldKey).copy().setStyle(text.getStyle());
            }

            return text;
        }

        private static Style randomizedFormatting(Style original){
            if (Platform.getEnvironment() == Env.CLIENT){
                Random random = Random.create(MiscClientAPIKt.getClientTime() / 5);
                if (random.nextInt(3) == 0){
                    original = original.withBold(true);
                }
                if (random.nextInt(4) == 0){
                    original = original.withItalic(true);
                }
                if (random.nextInt(4) == 0){
                    original = original.withStrikethrough(true);
                }
                if (random.nextInt(4) == 0){
                    original = original.withUnderline(true);
                }
                //usually make it illegible
                if (random.nextInt(10) != 0){
                    int choice = random.nextInt(3);
                    if (choice == 0){
                        original = original.withObfuscated(true);
                    } else {
                        String fontID = switch (choice){
                            case 1: yield "minecraft:alt";
                            case 2: yield "minecraft:illageralt";
                            default: yield "minecraft:uniform"; //pretty sure this will never actually come up
                        };
                        original = original.withFont(new Identifier(fontID));
                    }
                }
                return original;
            } else {
                return original.withBold(true);
            }
        }

        @Override
        public int color() {
            return 0xff_5555FF;
        }
    };

    public static class TextTransformer {
        public final UUID uuid;
        protected final BiFunction<Text, String, Text> function;
        public TextTransformer(UUID uuid, BiFunction<Text, String, Text> function){
            this.uuid = uuid;
            this.function = function;
        }
        public Text transform(Text text, String worldKey){
            return function.apply(text, worldKey);
        }

        public static TextTransformer colorizer(String keyToColorize, int color){
            BiFunction<Text, String, Text> transformer = (original, string) ->{
                if (string.equals(keyToColorize)){
                    return original.copy().setStyle(original.getStyle().withColor(color));
                } else {
                    return original;
                }
            };
            return new TextTransformer(UUID.randomUUID(), transformer);
        }
    }
}
