package net.beholderface.oneironaut.registry;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.iotatypes.DimIota;
import net.beholderface.oneironaut.casting.iotatypes.SoulprintIota;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

import java.util.HashMap;
import java.util.Map;

public class OneironautIotaTypeRegistry {
    public static final Registry<IotaType<?>> REGISTRY = IXplatAbstractions.INSTANCE.getIotaTypeRegistry();
    /*public static void registerTypes() {
        BiConsumer<IotaType<?>, Identifier> r = (type, id) -> Registry.register(REGISTRY, id, type);
        for (var e : TYPES.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }*/
    public static Map<ResourceLocation, IotaType<?>> TYPES = new HashMap<>();
    public static final IotaType<DimIota> DIM = type("dim", DimIota.TYPE);
    //public static final IotaType<PotionIota> POTION = type("potion", PotionIota.TYPE);
    public static final IotaType<SoulprintIota> UUID = type("uuid", SoulprintIota.TYPE);

    public static void init() {
        for (Map.Entry<ResourceLocation, IotaType<?>> entry : TYPES.entrySet()) {
            Registry.register(HexIotaTypes.REGISTRY, entry.getKey(), entry.getValue());
        }
    }

    private static <U extends Iota, T extends IotaType<U>> T type(String name, T type) {
        IotaType<?> old = TYPES.put(Oneironaut.id(name), type);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return type;
    }
}
