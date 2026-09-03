package net.beholderface.oneironaut.neo;

import net.beholderface.oneironaut.network.FireballUpdatePacket;
import net.beholderface.oneironaut.network.HoverliftAntiDesyncPacket;
import net.beholderface.oneironaut.network.ItemUpdatePacket;
import net.beholderface.oneironaut.network.ParticleBurstPacket;
import net.beholderface.oneironaut.network.SpoopyScreamPacket;
import net.beholderface.oneironaut.network.UnBrainsweepPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class OneironautNetworking {
    private OneironautNetworking() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(OneironautNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(ParticleBurstPacket.TYPE, ParticleBurstPacket.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> payload.handleClient(context.player())));
        registrar.playToClient(FireballUpdatePacket.TYPE, FireballUpdatePacket.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> payload.handleClient(context.player())));
        registrar.playToClient(ItemUpdatePacket.TYPE, ItemUpdatePacket.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> payload.handleClient(context.player())));
        registrar.playToClient(UnBrainsweepPacket.TYPE, UnBrainsweepPacket.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> payload.handleClient(context.player())));
        registrar.playToClient(SpoopyScreamPacket.TYPE, SpoopyScreamPacket.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> payload.handleClient(context.player())));
        registrar.playToClient(HoverliftAntiDesyncPacket.TYPE, HoverliftAntiDesyncPacket.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> payload.handleClient(context.player())));
    }
}
