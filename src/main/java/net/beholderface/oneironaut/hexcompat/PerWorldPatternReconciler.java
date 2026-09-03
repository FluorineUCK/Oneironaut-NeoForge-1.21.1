package net.beholderface.oneironaut.hexcompat;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.server.ScrungledPatternsSave;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

/**
 * Hex Casting persists its per-world pattern table after first generation.
 * When an addon is installed into an existing world, that saved table can
 * therefore have no entry for the addon's newly registered great spells.
 * Rebuild the table with Hex Casting's own deterministic generator when
 * Oneironaut entries are missing.
 */
public final class PerWorldPatternReconciler {
    private PerWorldPatternReconciler() {
    }

    public static void reconcile(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        Registry<ActionRegistryEntry> actions = IXplatAbstractions.INSTANCE.getActionRegistry();
        ScrungledPatternsSave savedPatterns = ScrungledPatternsSave.open(overworld);

        List<ResourceKey<ActionRegistryEntry>> expected = actions.registryKeySet().stream()
                .filter(key -> key.location().getNamespace().equals(Oneironaut.MOD_ID))
                .filter(key -> HexUtils.isOfTag(actions, key, HexTags.Actions.PER_WORLD_PATTERN))
                .toList();
        List<ResourceKey<ActionRegistryEntry>> missing = expected.stream()
                .filter(key -> savedPatterns.lookupReverse(key) == null
                        || PatternRegistryManifest.getCanonicalStrokesPerWorld(key, overworld) == null)
                .toList();

        if (missing.isEmpty()) {
            Oneironaut.LOGGER.info(
                    "[ONEIRONAUT-PROBE] per_world_patterns=PASS expected={} missing=0",
                    expected.size()
            );
            return;
        }

        Oneironaut.LOGGER.warn(
                "Hex Casting's saved per-world pattern table is missing {} Oneironaut action(s): {}. "
                        + "Recalculating it with the current registry.",
                missing.size(),
                missing.stream().map(key -> key.location().toString()).toList()
        );

        ScrungledPatternsSave rebuilt = ScrungledPatternsSave.createFromScratch(overworld.getSeed());
        overworld.getDataStorage().set(ScrungledPatternsSave.TAG_SAVED_DATA, rebuilt);

        List<ResourceKey<ActionRegistryEntry>> unresolved = expected.stream()
                .filter(key -> rebuilt.lookupReverse(key) == null
                        || PatternRegistryManifest.getCanonicalStrokesPerWorld(key, overworld) == null)
                .toList();
        if (unresolved.isEmpty()) {
            Oneironaut.LOGGER.info(
                    "[ONEIRONAUT-PROBE] per_world_patterns=PASS expected={} repaired={}",
                    expected.size(),
                    missing.size()
            );
        } else {
            Oneironaut.LOGGER.error(
                    "[ONEIRONAUT-PROBE] per_world_patterns=FAIL unresolved={}",
                    unresolved.stream().map(key -> key.location().toString()).toList()
            );
        }
    }
}
