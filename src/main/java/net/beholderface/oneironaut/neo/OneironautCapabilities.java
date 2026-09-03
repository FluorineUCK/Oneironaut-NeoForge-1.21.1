package net.beholderface.oneironaut.neo;

import at.petrak.hexcasting.forge.cap.HexCapabilities;
import net.beholderface.oneironaut.item.ItemStolenMediaProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** NeoForge capability registrations for Oneironaut's non-Hex item classes. */
public final class OneironautCapabilities {
    private OneironautCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        for (ItemStolenMediaProvider item : ItemStolenMediaProvider.allStolenMediaItems) {
            event.registerItem(
                    HexCapabilities.Item.MEDIA,
                    (stack, context) -> item.getProvider(stack),
                    item
            );
        }
    }
}
