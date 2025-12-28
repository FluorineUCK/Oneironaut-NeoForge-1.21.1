package net.beholderface.oneironaut.mixin.IotaKeyableMixins;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.casting.idea.IdeaKeyable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityIota.class)
public abstract class EntityIotaKeyableMixin implements IdeaKeyable {

    @Shadow public abstract Entity getEntity();

    @Override
    public String getKey() {
        return this.getEntity().getUuidAsString();
    }

    @Override
    public boolean isValidKey(CastingEnvironment env) {
        Entity entity = this.getEntity();
        env.assertEntityInRange(entity);
        return  (entity instanceof ServerPlayerEntity player && MiscAPIKt.isPlayerEnlightened(player)) ||
                (entity instanceof VillagerEntity villager && IXplatAbstractions.INSTANCE.isBrainswept(villager)) ||
                (entity instanceof WanderingTraderEntity trader && IXplatAbstractions.INSTANCE.isBrainswept(trader));
    }
}
