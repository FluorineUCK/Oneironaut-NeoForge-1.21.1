package net.beholderface.oneironaut.casting.environments;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment;
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.beholderface.oneironaut.mixin.GeneralCastEnvInvoker;
import net.beholderface.oneironaut.mixin.PlayerCastEnvInvoker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class ExtradimensionalCastEnv extends PlayerBasedCastEnv {

    public final PlayerBasedCastEnv parentEnv;
    public final int depth;
    public final CastingVM vm;

    public ExtradimensionalCastEnv(ServerPlayerEntity caster, PlayerBasedCastEnv parent, ServerWorld target, CastingVM existingVM) {
        super(caster, parent.getCastingHand());
        this.parentEnv = parent;
        ((GeneralCastEnvInvoker)this).setWorld(target);
        if (parentEnv instanceof ExtradimensionalCastEnv extradimensionalCastEnv){
            this.depth = extradimensionalCastEnv.depth + 1;
        } else {
            this.depth = 1;
        }
        this.vm = existingVM;
    }

    public ExtradimensionalCastEnv(ServerPlayerEntity caster, PlayerBasedCastEnv parent, ServerWorld target) {
        super(caster, parent.getCastingHand());
        this.parentEnv = parent;
        ((GeneralCastEnvInvoker)this).setWorld(target);
        if (parentEnv instanceof ExtradimensionalCastEnv extradimensionalCastEnv){
            this.depth = extradimensionalCastEnv.depth + 1;
        } else {
            this.depth = 1;
        }
        this.vm = CastingVM.empty(this);
    }

    @Override
    public void postExecution(CastResult result) {
        parentEnv.postExecution(result);
    }

    @Override
    public boolean replaceItem(Predicate<ItemStack> stackOk, ItemStack replaceWith, @Nullable Hand hand) {
        return parentEnv.replaceItem(stackOk, replaceWith, hand);
    }

    @Override
    protected long extractMediaEnvironment(long cost, boolean simulate) {
        double multiplier = 1.25;
        return ((GeneralCastEnvInvoker)parentEnv).extractFromEnv((long) (cost * multiplier), simulate);
    }

    @Override
    public Hand getCastingHand() {
        return parentEnv.getCastingHand();
    }

    @Override
    public FrozenPigment getPigment() {
        return parentEnv.getPigment();
    }

    @Override
    public LivingEntity getCastingEntity() {
        return parentEnv.getCastingEntity();
    }

    @Override
    public ServerPlayerEntity getCaster() {
        return parentEnv.getCaster();
    }

    @Override
    public boolean isVecInRangeEnvironment(Vec3d vec) {
        if (this.world == parentEnv.getWorld()){
            return parentEnv.isVecInRangeEnvironment(vec);
        }
        var sentinel = HexAPI.instance().getSentinel(this.caster);
        return sentinel != null
                && sentinel.extendsRange()
                && this.world.getRegistryKey() == sentinel.dimension()
                // adding 0.00000000001 to avoid machine precision errors at specific angles
                && vec.squaredDistanceTo(sentinel.position()) <= SENTINEL_RADIUS * SENTINEL_RADIUS + 0.00000000001;
    }

    @Override
    protected boolean canOvercast() {
        return ((PlayerCastEnvInvoker)parentEnv).canItOvercast();
    }

    @Override
    public @Nullable FrozenPigment setPigment(@Nullable FrozenPigment pigment) {
        return parentEnv.setPigment(pigment);
    }

    @Override
    public void produceParticles(ParticleSpray particles, FrozenPigment pigment) {
        parentEnv.produceParticles(particles, pigment);
    }

    @Override
    public Vec3d mishapSprayPos() {
        return parentEnv.mishapSprayPos();
    }

    @Override
    public MishapEnvironment getMishapEnvironment() {
        return parentEnv.getMishapEnvironment();
    }

    protected void sendMishapMsgToPlayer(OperatorSideEffect.DoMishap mishap) {
        ((PlayerCastEnvInvoker)parentEnv).sendMishapMsgPlayer(mishap);
    }

    @Override
    protected boolean isCreativeMode() {
        // not sure what the diff between this and isCreative() is
        return ((PlayerCastEnvInvoker)parentEnv).isCreative();
    }

    @Override
    public void printMessage(Text message) {
        parentEnv.printMessage(message);
    }
}
