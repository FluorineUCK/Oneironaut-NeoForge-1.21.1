package net.beholderface.oneironaut.neo.platform.mixin;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.beholderface.oneironaut.item.ItemLibraryCard;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = {
        "at.petrak.hexcasting.common.casting.actions.akashic.OpAkashicRead",
        "ram.talia.hexal.common.casting.actions.everbook.OpEverbookWrite"
})
public class MixinHonorLibraryCardReads {
    @WrapOperation(method = {
            "execute"
    },
            at = @At(value = "INVOKE", target="Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;getWorld()Lnet/minecraft/server/level/ServerLevel;", remap = false),
            remap = false)
    public ServerLevel getAlternateLibraryDim(CastingEnvironment ctx, Operation<ServerLevel> original){
        ServerLevel originalWorld = original.call(ctx);
        if(!(ctx.getCastingEntity() instanceof ServerPlayer player)) return originalWorld;
        Inventory pInv = player.getInventory();
        for(int i = 0; i < pInv.getContainerSize(); i++){
            ItemStack stack = pInv.getItem(i);
            if(stack.getItem() instanceof ItemLibraryCard libCard){
                ResourceKey<Level> dim = libCard.getDimension(stack);
                if(dim != null){
                    ServerLevel newWorld = originalWorld.getServer().getLevel(dim);
                    if(newWorld != null) return newWorld;
                }
            }
        }
        return originalWorld;
    }
}
