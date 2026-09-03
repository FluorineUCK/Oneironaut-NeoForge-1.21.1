package net.beholderface.oneironaut.neo.probe;

import at.petrak.hexcasting.api.casting.iota.Iota;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.iotatypes.DimIota;
import net.beholderface.oneironaut.item.RiftResidueItem;
import net.beholderface.oneironaut.registry.OneironautItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

/** Development-only regression for the no-server creative-tooltip crash. */
public final class OneironautClientProbe {
    private static int ticks;
    private static boolean finished;

    private OneironautClientProbe() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(OneironautClientProbe::onClientTick);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        if (finished || ++ticks < 80) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof TitleScreen)) {
            if (ticks >= 600) {
                fail(new IllegalStateException("Title screen was not reached; screen=" + minecraft.screen));
            }
            return;
        }

        finished = true;
        try {
            ItemStack stack = new ItemStack(OneironautItemRegistry.RIFT_RESIDUE.get());
            Iota iota = ((RiftResidueItem) stack.getItem()).readIota(stack);
            require(iota instanceof DimIota, "rift residue did not return a DimIota");
            DimIota dimension = (DimIota) iota;
            require(
                "oneironaut:deep_noosphere".equals(dimension.getDimString()),
                "rift residue returned the wrong dimension: " + dimension.getDimString()
            );

            // This is the same ItemStack path used by creative-search and recipe-viewer
            // tooltip indexing while a multiplayer client has no local MinecraftServer.
            List<Component> tooltip = stack.getTooltipLines(
                Item.TooltipContext.EMPTY,
                minecraft.player,
                TooltipFlag.NORMAL
            );
            require(!tooltip.isEmpty(), "rift residue tooltip was empty");
            require(
                tooltip.stream().anyMatch(line ->
                    line.getString().contains("oneironaut:deep_noosphere")
                        || line.getString().contains("deep_noosphere")
                ),
                "rift residue tooltip did not expose its dimension: " + tooltip
            );

            Oneironaut.LOGGER.info(
                "[ONEIRONAUT-PROBE] rift_residue_tooltip=PASS no_server=PASS dimension={} lines={}",
                dimension.getDimString(),
                tooltip.size()
            );
            stop(minecraft, 0);
        } catch (Throwable throwable) {
            fail(throwable);
        }
    }

    private static void fail(Throwable throwable) {
        finished = true;
        Oneironaut.LOGGER.error("[ONEIRONAUT-PROBE] rift_residue_tooltip=FAIL", throwable);
        stop(Minecraft.getInstance(), 1);
    }

    private static void stop(Minecraft minecraft, int exitCode) {
        Thread hardStop = new Thread(() -> {
            try {
                Thread.sleep(5_000L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            Runtime.getRuntime().halt(exitCode);
        }, "oneironaut-client-probe-hard-stop");
        hardStop.setDaemon(false);
        hardStop.start();
        minecraft.stop();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
