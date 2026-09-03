package net.beholderface.oneironaut.neo;

import com.mojang.serialization.Codec;
import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/** Persistent replacement for Oneironaut's Cardinal Components wisp flag. */
public final class OneironautAttachments {
    private static final DeferredRegister<AttachmentType<?>> TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Oneironaut.MOD_ID);

    public static final Supplier<AttachmentType<Boolean>> WISP_DECORATIVE = TYPES.register(
        "wisp_decorative",
        () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build()
    );

    private OneironautAttachments() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }

    public static boolean isDecorative(Entity entity) {
        return entity.getData(WISP_DECORATIVE);
    }

    public static void markDecorative(Entity entity) {
        entity.setData(WISP_DECORATIVE, true);
    }
}
