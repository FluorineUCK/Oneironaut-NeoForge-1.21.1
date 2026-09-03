package net.beholderface.oneironaut.item;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder;
import net.minecraft.util.Tuple;
import net.beholderface.oneironaut.OneironautConfig;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.beholderface.oneironaut.Oneironaut;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BottomlessMediaItem extends ItemMediaHolder {

    public static final int priority = 10000;

    public BottomlessMediaItem(net.minecraft.world.item.Item.Properties settings){
        super(settings);
    }

    @Override
    public int getConsumptionPriority(ItemStack stack) {
        return priority;
    }
    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext context,
                                List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        //do nothing
    }

    public static double arbitraryLog(double base, double num){
        return Math.log(num) / Math.log(base);
    }

    private static final Map<Entity, Tuple<List<UUID>, Long>> playerPhialList = new HashMap<>();
    private static final Map<UUID, Tuple<Entity, Long>> phialOwners = new HashMap<>();
    public static long time;

    private long logMedia(ItemStack stack){
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!nbt.hasUUID("uuid")) {
            Oneironaut.LOGGER.info("Inexhaustible phial stack data does not contain a UUID tag.");
            return 0;
        }
        UUID uuid = nbt.getUUID("uuid");
        long lastCheckIn = phialOwners.get(uuid).getB();
        int lastPhialCount = playerPhialList.get(phialOwners.get(uuid).getA()).getA().size();
        //dashing your hopes against the rocks
        int base = lastPhialCount <= 36 ? 6 : 12;
        //NbtCompound currentData = playerPhialCounts.get(phialOwners.get(uuid).getFirst());
        long media = 1;
        float mediaMultiplier = 1.0f;
        if (lastCheckIn != time){
            if (Math.abs(lastCheckIn - time) <= 1){
                mediaMultiplier = OneironautConfig.getServer().getStaleIPhialLenience();
            } else {
                mediaMultiplier = 0.0f;
            }
        }
        if (lastPhialCount == 1){
            media = MediaConstants.DUST_UNIT / 10;
        } else {
            media = (int) (((arbitraryLog(base, lastPhialCount) + 0.75) / lastPhialCount) * (MediaConstants.DUST_UNIT / 10.0));
        }
        //int media = foundItems > 0 ? (int) (((arbitraryLog(6.0, foundItems) + 0.75) / foundItems) * (MediaConstants.DUST_UNIT / 10)) : 0;
        //Oneironaut.LOGGER.info("Media in each of the "+ lastPhialCount + " endless phials in inventory: "+media);
        //Oneironaut.LOGGER.info(media);
        return (long) Math.max(media * mediaMultiplier, 0);
    }

    private void resetLists(Tuple<List<UUID>, Long> pair, UUID uuid, Entity entity){
        List<UUID> list = pair.getA();
        list.clear();
        list.add(uuid);
        playerPhialList.put(entity, new Tuple<>(list, time));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (!world.isClientSide && entity instanceof Player){
            //time = world.getTime();
            CompoundTag stackNbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            UUID uuid;
            if (!stackNbt.contains("uuid")){
                uuid = UUID.randomUUID();
                stackNbt.putUUID("uuid", uuid);
            } else {
                uuid = stackNbt.getUUID("uuid");
            }
            if (!phialOwners.containsKey(uuid)){
                phialOwners.put(uuid, new Tuple<>(entity, time));
            }
            phialOwners.put(uuid, new Tuple<>(entity, time));
            if (!playerPhialList.containsKey(entity)){
                playerPhialList.put(entity, new Tuple<>(new ArrayList<>(), time));
            }
            Tuple<List<UUID>, Long> currentData = playerPhialList.get(entity);
            List<UUID> list = currentData.getA();
            if (/*Math.abs(currentData.getB() - time) <= 1*/ currentData.getB() != time){
                resetLists(currentData, uuid, entity);
            } else {
                /*if (entity.isSneaking()){
                    Oneironaut.LOGGER.info(list.toString());
                }*/
                if (list.contains(uuid)){
                    uuid = UUID.randomUUID();
                    stackNbt.putUUID("uuid", uuid);
                }
                list.add(uuid);
            }
            CustomData.set(DataComponents.CUSTOM_DATA, stack, stackNbt);
        }
    }
    @Override
    public void onCraftedBy(ItemStack stack, Level world, Player player) {
        //stack.getOrCreateNbt().putInt("foundPhials", 1);
        UUID uuid = UUID.randomUUID();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putUUID("uuid", uuid));
        phialOwners.put(uuid, new Tuple<>((Entity) player, time));
        //stack.getOrCreateNbt().putLong("latestTime", world.getTime());
    }

    @Override
    public long getMedia(ItemStack stack) {
        if (stack == null){
            //Oneironaut.LOGGER.info("Inexhaustible Phial's getMedia method called with a null pointer.");
            return 0;
        }
        try {
            return logMedia(stack);
        } catch (Exception e){
            return 0;
        }
    }

    @Override
    public long getMaxMedia(ItemStack stack) {
        if (stack == null){
            Oneironaut.LOGGER.info("Inexhaustible Phial's getMedia method called with a null pointer.");
            return 0;
        }
        return logMedia(stack);
    }

    @Override
    public void setMedia(ItemStack stack, long media) {}

    @Override
    public boolean canProvideMedia(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canRecharge(ItemStack stack) {
        return false;
    }



}
