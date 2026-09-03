package net.beholderface.oneironaut.neo.platform.mixin;

import at.petrak.hexcasting.common.loot.AddPerWorldPatternToScrollFunc;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.beholderface.oneironaut.registry.OneironautTags;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AddPerWorldPatternToScrollFunc.class)
public class BlockPatternFromLootScrollMixin {
    @WrapOperation(method = "doStatic", at = @At(value = "INVOKE", target =
            "Lat/petrak/hexcasting/api/utils/HexUtils;isOfTag(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/tags/TagKey;)Z"
            ,remap = false), remap = false)
    private static boolean isAlsoOfThisTag(Registry<?> maybeHolder, ResourceKey<?> holder, TagKey<?> tag, Operation<Boolean> original){
        return original.call(maybeHolder, holder, tag)
                && !original.call(maybeHolder, holder, OneironautTags.Actions.noLootScrolls);
    }
}
