package net.beholderface.oneironaut.neo.platform.mixin;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.server.ScrungledPatternsSave;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(FishingHook.class)
public class FishUpScrollMixin {
    @WrapOperation(method = "retrieve", at = @At(
            value = "INVOKE", remap = true,
            //concat is just to make it more readable
            target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)" +
                    "Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"
    ),
            remap = true
    )
    public ObjectArrayList<ItemStack> replaceWithScroll(LootTable instance, LootParams parameters, Operation<ObjectArrayList<ItemStack>> original){
        if (parameters.hasParam(LootContextParams.THIS_ENTITY)){
            FishingHook entity = (FishingHook) parameters.getParameter(LootContextParams.THIS_ENTITY);
            ServerLevel world = (ServerLevel) entity.level();
            RandomSource rand = world.random;
            Predicate<FluidState> predicate = (state)->{
                return MiscAPIKt.isThoughtSlurry(state.getType());
            };
            //chance of 5% when in the noosphere, 2.5% in the overworld, and 10% when in the deep noosphere
            double baseChance = 5;
            double dimensionModifier = Oneironaut.isWorldNoosphere(world) ? (Oneironaut.isWorldDeepNoosphere(world) ? baseChance : 0) : -2.5;
            double threshold = 100.0 - (baseChance + dimensionModifier);
            double roll = rand.nextDouble() * 100;
            double luckModifier = parameters.getLuck() * 2.5; //additional 2.5% chance per point of luck
            if (roll + luckModifier >= threshold){
                if (world.isFluidAtPosition(entity.blockPosition(), predicate)
                        || predicate.test(world.getFluidState(entity.blockPosition().below()))){
                    var save = ScrungledPatternsSave.open(world.getServer().overworld());
                    ItemScroll scroll = HexItems.SCROLL_LARGE.get();
                    ItemStack stack = scroll.getDefaultInstance();
                    Registry<ActionRegistryEntry> regi = IXplatAbstractions.INSTANCE.getActionRegistry();
                    List<Pair<String, ScrungledPatternsSave.PerWorldEntry>> foundSpells = new ArrayList<>();
                    for (var entry : regi.entrySet()){
                        var key = entry.getKey();
                        if (HexUtils.isOfTag(regi, key, HexTags.Actions.PER_WORLD_PATTERN)){
                            Pair<String, ScrungledPatternsSave.PerWorldEntry> found = save.lookupReverse(key);
                            if (found != null) {
                                foundSpells.add(found);
                            }
                        }
                    }
                    if (foundSpells.isEmpty()) {
                        return original.call(instance, parameters);
                    }
                    var found = foundSpells.get(rand.nextIntBetweenInclusive(0, foundSpells.size() - 1));
                    stack = ItemScroll.withPerWorldPattern(stack, found.getSecond().key());
                    return ObjectArrayList.of(stack);
                }
            }
        }
        return original.call(instance, parameters);
    }
}
