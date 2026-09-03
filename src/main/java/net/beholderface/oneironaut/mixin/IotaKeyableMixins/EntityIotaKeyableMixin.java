package net.beholderface.oneironaut.mixin.IotaKeyableMixins;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.casting.idea.IdeaKeyable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(EntityIota.class)
public abstract class EntityIotaKeyableMixin implements IdeaKeyable {

    @Shadow public abstract UUID getEntityId();

    @Override
    public String getKey() {
        return this.getEntityId().toString();
    }

    @Override
    public boolean isValidKey(CastingEnvironment env) {
        Entity entity = ((EntityIota) (Object) this).getEntity(env.getWorld());
        if (entity == null) {
            return false;
        }
        env.assertEntityInRange(entity);
        return  (entity instanceof ServerPlayer player && MiscAPIKt.isPlayerEnlightened(player)) ||
                (entity instanceof Villager villager && IXplatAbstractions.INSTANCE.isBrainswept(villager)) ||
                (entity instanceof WanderingTrader trader && IXplatAbstractions.INSTANCE.isBrainswept(trader));
    }
}
