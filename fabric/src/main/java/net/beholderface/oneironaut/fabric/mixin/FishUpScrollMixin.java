package net.beholderface.oneironaut.fabric.mixin;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.math.HexPattern;
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
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(FishingBobberEntity.class)
public class FishUpScrollMixin {
    @WrapOperation(method = "use", at = @At(
            value = "INVOKE", remap = true,
            //concat is just to make it more readable
            target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContextParameterSet;)" +
                    "Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"
    ),
            remap = true
    )
    public ObjectArrayList<ItemStack> replaceWithScroll(LootTable instance, LootContextParameterSet parameters, Operation<ObjectArrayList<ItemStack>> original){
        if (parameters.contains(LootContextParameters.THIS_ENTITY)){
            FishingBobberEntity entity = (FishingBobberEntity) parameters.get(LootContextParameters.THIS_ENTITY);
            ServerWorld world = (ServerWorld) entity.getWorld();
            Random rand = world.random;
            Predicate<FluidState> predicate = (state)->{
                return MiscAPIKt.isThoughtSlurry(state.getFluid());
            };
            //should start off as a 5% chance, adds 2.5% per level of luck (usually just luck of the sea, I think)
            int chance = 20;
            int roll = rand.nextBetweenExclusive(1, chance + 1);
            if (roll >= chance - (parameters.getLuck() / 2)){
                if (world.testFluidState(entity.getBlockPos(), predicate) || world.testFluidState(entity.getBlockPos().down(), predicate)){
                    var save = ScrungledPatternsSave.open(world.getServer().getOverworld());
                    ItemScroll scroll = HexItems.SCROLL_LARGE;
                    ItemStack stack = scroll.getDefaultStack();
                    Registry<ActionRegistryEntry> regi = IXplatAbstractions.INSTANCE.getActionRegistry();
                    List<Pair<String, ScrungledPatternsSave.PerWorldEntry>> foundSpells = new ArrayList<>();
                    for (var entry : regi.getEntrySet()){
                        var key = entry.getKey();
                        if (HexUtils.isOfTag(regi, key, HexTags.Actions.PER_WORLD_PATTERN)){
                            foundSpells.add(save.lookupReverse(key));
                        }
                    }
                    var found = foundSpells.get(rand.nextBetween(0, foundSpells.size() - 1));
                    var signature = found.getFirst();
                    var startDir = found.getSecond().canonicalStartDir();
                    var pat = HexPattern.fromAngles(signature, startDir);
                    var tag = new NbtCompound();
                    tag.putString(ItemScroll.TAG_OP_ID, found.getSecond().key().getValue().toString());
                    tag.put(ItemScroll.TAG_PATTERN, pat.serializeToNBT());
                    stack.setNbt(tag);
                    return ObjectArrayList.of(stack);
                }
            }
        }
        return original.call(instance, parameters);
    }
}
