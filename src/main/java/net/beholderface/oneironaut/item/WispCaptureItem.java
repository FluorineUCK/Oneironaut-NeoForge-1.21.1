package net.beholderface.oneironaut.item;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.common.msgs.MsgCastParticleS2C;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import kotlin.collections.CollectionsKt;
import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.common.entities.BaseCastingWisp;
import ram.talia.hexal.common.entities.ProjectileWisp;
import ram.talia.hexal.common.entities.TickingWisp;
import ram.talia.hexal.common.lib.HexalEntities;
import ram.talia.hexal.common.network.MsgParticleLines;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.function.Predicate;

/** Stores a Hexal casting wisp in the stack's custom-data component. */
public class WispCaptureItem extends ItemMediaHolder {
    public static final ResourceLocation FILLED_PREDICATE =
            ResourceLocation.fromNamespaceAndPath(Oneironaut.MOD_ID, "contains_wisp");

    public static final String WISP_DATA_TAG = "contained_wisp";
    public static final String WISP_TYPE_TAG = "wisp_type";
    private static final String WISP_TYPE_TICKING = "ticking";
    private static final String WISP_TYPE_PROJECTILE = "projectile";
    private static final String WISP_PIGMENT_TAG = "pigment";
    private static final int COOLDOWN = 20;
    private static final boolean DEBUG_MESSAGES = false;

    public WispCaptureItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);

        // A fresh wrangler learns its capacity by consuming one of its owner's wisps.
        if (!stack.has(HexDataComponents.MEDIA_MAX.get())) {
            if (getMedia(stack) > 0) {
                stack.set(HexDataComponents.MEDIA_MAX.get(), MediaConstants.DUST_UNIT * 640L);
                return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
            }

            BaseCastingWisp initialWisp = wispRaycast(user);
            if (initialWisp == null || !user.getUUID().equals(initialWisp.owner())) {
                return InteractionResultHolder.fail(stack);
            }
            if (!world.isClientSide) {
                long wispMedia = initialWisp.getMedia();
                long roundedMax = (long) Math.ceil((double) wispMedia / MediaConstants.DUST_UNIT)
                        * MediaConstants.DUST_UNIT;
                stack.set(HexDataComponents.MEDIA_MAX.get(), roundedMax);
                setMedia(stack, Math.max(getMedia(stack), wispMedia - MediaConstants.SHARD_UNIT));
                initialWisp.kill();
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }

        if (!hasWisp(stack)) {
            BaseCastingWisp wisp = wispRaycast(user);
            if (wisp == null) {
                Oneironaut.boolLogger("Raycast did not find anything." + world.isClientSide, DEBUG_MESSAGES);
                return InteractionResultHolder.pass(stack);
            }
            user.getCooldowns().addCooldown(this, COOLDOWN);
            boolean captured = world.isClientSide || captureWisp(stack, wisp, user);
            return captured
                    ? InteractionResultHolder.sidedSuccess(stack, world.isClientSide)
                    : InteractionResultHolder.fail(stack);
        }

        if (user.isShiftKeyDown()) {
            user.getCooldowns().addCooldown(this, COOLDOWN / 2);
            if (!world.isClientSide) {
                discardWisp(stack, user);
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }

        if (getWispType(stack) == HexalEntities.PROJECTILE_WISP) {
            user.getCooldowns().addCooldown(this, COOLDOWN);
            boolean released = world.isClientSide || releaseWisp(
                    stack,
                    user.getEyePosition().add(user.getLookAngle().scale(0.25)),
                    user
            );
            return released
                    ? InteractionResultHolder.sidedSuccess(stack, world.isClientSide)
                    : InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Nullable
    private BaseCastingWisp wispRaycast(Player user) {
        // Keep the upstream reach behavior, including entity-scale compensation.
        Vec3 rayVec = user.getLookAngle().scale(
                (user.isCreative() ? 5.2 : 4.5)
                        * (user.getBbHeight() / (user.isShiftKeyDown() ? 1.5 : 1.8))
        );
        Vec3 endPos = user.getEyePosition().add(rayVec);
        AABB box = AABB.unitCubeFromLowerCorner(user.getEyePosition()).inflate(rayVec.length() + 1);
        Predicate<Entity> predicate = entity -> entity instanceof TickingWisp || entity instanceof ProjectileWisp;
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                user,
                user.getEyePosition(),
                endPos,
                box,
                predicate,
                999999
        );
        return hit == null ? null : (BaseCastingWisp) hit.getEntity();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player user = context.getPlayer();
        if (user == null || user.isShiftKeyDown() || !hasWisp(stack)) {
            return InteractionResult.PASS;
        }

        user.getCooldowns().addCooldown(this, COOLDOWN);
        Vec3 spawnPos = Vec3.atCenterOf(context.getClickedPos().relative(context.getClickedFace()));
        boolean released = context.getLevel().isClientSide || releaseWisp(stack, spawnPos, user);
        return released ? InteractionResult.sidedSuccess(context.getLevel().isClientSide) : InteractionResult.FAIL;
    }

    private boolean captureWisp(ItemStack stack, BaseCastingWisp wisp, @NotNull Player user) {
        if (!(user.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        long cost = wisp.getCaster() == user
                ? MediaConstants.SHARD_UNIT
                : (long) Math.ceil(wisp.getMedia() * 1.5);
        if ((getMedia(stack) < cost && !user.isCreative()) || wisp.getSeon()) {
            serverLevel.playSeededSound(null, user, HexSounds.CAST_FAILURE,
                    SoundSource.PLAYERS, 1f, 1f, serverLevel.random.nextLong());
            return false;
        }

        deductMedia(stack, cost, user);
        CompoundTag stackData = getCustomData(stack);
        stackData.put(WISP_DATA_TAG, wisp.saveWithoutId(new CompoundTag()));
        stackData.putString(WISP_TYPE_TAG,
                wisp instanceof TickingWisp ? WISP_TYPE_TICKING : WISP_TYPE_PROJECTILE);
        setCustomData(stack, stackData);

        Vec3 wispPos = wisp.position();
        FrozenPigment pigment = wisp.pigment();
        wisp.kill();
        Oneironaut.boolLogger("Captured wisp for " + cost / MediaConstants.DUST_UNIT + " dust", DEBUG_MESSAGES);
        IXplatAbstractions.INSTANCE.sendPacketNear(user.getEyePosition(), 128.0, serverLevel,
                new MsgParticleLines(CollectionsKt.listOf(
                        user.getEyePosition(), wispPos.add(0.0, 0.05, 0.0)), pigment));
        serverLevel.playSeededSound(null, user, HexSounds.CAST_HERMES,
                SoundSource.PLAYERS, 1f, 1f, serverLevel.random.nextLong());
        return true;
    }

    private boolean releaseWisp(ItemStack stack, Vec3 spawnPos, @NotNull Player user) {
        if (!(user.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (getMedia(stack) < MediaConstants.SHARD_UNIT && !user.isCreative()) {
            Oneironaut.boolLogger("Insufficient media to release wisp", DEBUG_MESSAGES);
            serverLevel.playSeededSound(null, user, HexSounds.CAST_FAILURE,
                    SoundSource.PLAYERS, 1f, 1f, serverLevel.random.nextLong());
            return false;
        }

        CompoundTag storedData = getWispData(stack);
        EntityType<?> wispType = getWispType(stack);
        if (storedData == null || wispType == null) {
            return false;
        }

        BaseCastingWisp wisp;
        if (wispType == HexalEntities.TICKING_WISP) {
            wisp = new TickingWisp(HexalEntities.TICKING_WISP, serverLevel);
        } else if (wispType == HexalEntities.PROJECTILE_WISP) {
            wisp = new ProjectileWisp(HexalEntities.PROJECTILE_WISP, serverLevel);
        } else {
            return false;
        }

        wisp.load(storedData.copy());
        wisp.setPos(spawnPos);
        if (wisp instanceof ProjectileWisp projectileWisp) {
            double speed = projectileWisp.getDeltaMovement().length();
            projectileWisp.setDeltaMovement(user.getLookAngle().scale(speed));
        }

        deductMedia(stack, MediaConstants.SHARD_UNIT, user);
        clearWispData(stack);
        serverLevel.addFreshEntity(wisp);
        serverLevel.playSeededSound(null, user, HexSounds.CAST_HERMES,
                SoundSource.PLAYERS, 1f, 1f, serverLevel.random.nextLong());
        IXplatAbstractions.INSTANCE.sendPacketNear(user.getEyePosition(), 128.0, serverLevel,
                new MsgParticleLines(CollectionsKt.listOf(
                        user.getEyePosition(), wisp.position().add(0.0, 0.05, 0.0)), wisp.pigment()));
        Oneironaut.boolLogger("Released contained wisp", DEBUG_MESSAGES);
        return true;
    }

    private void discardWisp(ItemStack stack, @Nullable Player user) {
        CompoundTag formerWispData = getWispData(stack);
        if (formerWispData == null) {
            return;
        }
        FrozenPigment pigment = decodePigment(formerWispData);
        clearWispData(stack);

        if (user != null && user.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSeededSound(null, user, HexSounds.ABACUS_SHAKE,
                    SoundSource.PLAYERS, 1f, 1f, serverLevel.random.nextLong());
            IXplatAbstractions.INSTANCE.sendPacketNear(user.getEyePosition(), 128.0, serverLevel,
                    new MsgCastParticleS2C(
                            ParticleSpray.burst(user.position().add(0.0, 0.125, 0.0), 1.0, 64),
                            pigment));
        }
    }

    @Nullable
    public CompoundTag getWispData(ItemStack stack) {
        CompoundTag data = getCustomData(stack);
        return data.contains(WISP_DATA_TAG, Tag.TAG_COMPOUND)
                ? data.getCompound(WISP_DATA_TAG)
                : null;
    }

    @Nullable
    public EntityType<?> getWispType(ItemStack stack) {
        String type = getCustomData(stack).getString(WISP_TYPE_TAG);
        return switch (type) {
            case WISP_TYPE_TICKING -> HexalEntities.TICKING_WISP;
            case WISP_TYPE_PROJECTILE -> HexalEntities.PROJECTILE_WISP;
            default -> null;
        };
    }

    private void deductMedia(ItemStack stack, long amount, Player player) {
        if (!player.isCreative()) {
            setMedia(stack, getMedia(stack) - amount);
        }
    }

    public boolean hasWisp(ItemStack stack) {
        return getWispData(stack) != null;
    }

    private static CompoundTag getCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void setCustomData(ItemStack stack, CompoundTag data) {
        CustomData.set(DataComponents.CUSTOM_DATA, stack, data);
    }

    private static void clearWispData(ItemStack stack) {
        CompoundTag data = getCustomData(stack);
        data.remove(WISP_DATA_TAG);
        data.remove(WISP_TYPE_TAG);
        setCustomData(stack, data);
    }

    private static FrozenPigment decodePigment(CompoundTag wispData) {
        Tag pigmentTag = wispData.get(WISP_PIGMENT_TAG);
        if (pigmentTag == null) {
            // Compatibility with Oneironaut's older Hexal save key.
            pigmentTag = wispData.get("colouriser");
        }
        if (pigmentTag == null) {
            return FrozenPigment.DEFAULT.get();
        }
        return FrozenPigment.CODEC.parse(NbtOps.INSTANCE, pigmentTag)
                .result()
                .orElseGet(FrozenPigment.DEFAULT);
    }

    @Override
    public boolean canProvideMedia(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canRecharge(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        CompoundTag wispData = getWispData(stack);
        if (wispData != null) {
            String hashString = "???";
            Tag hexData = wispData.get("hex");
            if (hexData != null) {
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    hashString = HexFormat.of().formatHex(
                            digest.digest(hexData.toString().getBytes(StandardCharsets.UTF_8)), 0, 4);
                } catch (NoSuchAlgorithmException ignored) {
                    // SHA-256 is required by the Java runtime.
                }
            }

            Component unstyled = Component.translatable(
                    "oneironaut.tooltip.wispcapturedevice.haswisp",
                    wispData.getLong("media") / MediaConstants.DUST_UNIT,
                    hashString);
            Level level = context.level();
            if (level != null) {
                Style coloredStyle = unstyled.getStyle().withColor(
                        decodePigment(wispData).getColorProvider().getColor(level.getGameTime(), Vec3.ZERO));
                tooltip.add(unstyled.copy().setStyle(coloredStyle));
            } else {
                tooltip.add(unstyled);
            }
        } else if (stack.has(HexDataComponents.MEDIA_MAX.get())) {
            tooltip.add(Component.translatable("oneironaut.tooltip.wispcapturedevice.nowisp"));
        } else {
            tooltip.add(Component.translatable("oneironaut.tooltip.wispcapturedevice.uninitialized"));
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
