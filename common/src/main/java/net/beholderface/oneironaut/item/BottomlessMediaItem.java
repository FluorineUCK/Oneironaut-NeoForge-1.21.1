package net.beholderface.oneironaut.item;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder;
import com.mojang.datafixers.util.Pair;
import net.beholderface.oneironaut.OneironautConfig;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.beholderface.oneironaut.Oneironaut;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BottomlessMediaItem extends ItemMediaHolder {

    public static final int priority = 10000;

    public BottomlessMediaItem(Settings settings){
        super(settings);
    }
    @Override
    public boolean isItemBarVisible(ItemStack pStack) {
        return false;
    }

    @Override
    public void appendTooltip(ItemStack pStack, @Nullable World pLevel, List<Text> pTooltipComponents, TooltipContext pIsAdvanced) {
        //do nothing
    }

    public static double arbitraryLog(double base, double num){
        return Math.log(num) / Math.log(base);
    }

    private static final Map<Entity, Pair<List<UUID>, Long>> playerPhialList = new HashMap<>();
    private static final Map<UUID, Pair<Entity, Long>> phialOwners = new HashMap<>();
    public static long time;

    private long logMedia(ItemStack stack){
        NbtCompound nbt = stack.getOrCreateNbt();
        UUID uuid = nbt.getUuid("uuid");
        if (uuid == null){
            Oneironaut.LOGGER.info("Inexhaustible phial stack NBT does not contain a UUID tag.");
            return 0;
        }
        long lastCheckIn = phialOwners.get(uuid).getSecond();
        int lastPhialCount = playerPhialList.get(phialOwners.get(uuid).getFirst()).getFirst().size();
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
        return (long) Math.max(media * mediaMultiplier, 1);
    }

    private void resetLists(Pair<List<UUID>, Long> pair, UUID uuid, Entity entity){
        List<UUID> list = pair.getFirst();
        list.clear();
        list.add(uuid);
        playerPhialList.put(entity, new Pair<>(list, time));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient && entity instanceof PlayerEntity){
            //time = world.getTime();
            NbtCompound stackNbt = stack.getOrCreateNbt();
            UUID uuid;
            if (!stackNbt.contains("uuid")){
                uuid = UUID.randomUUID();
                stackNbt.putUuid("uuid", uuid);
            } else {
                uuid = stackNbt.getUuid("uuid");
            }
            if (!phialOwners.containsKey(uuid)){
                phialOwners.put(uuid, new Pair<>(entity, time));
            }
            phialOwners.put(uuid, new Pair<>(entity, time));
            if (!playerPhialList.containsKey(entity)){
                playerPhialList.put(entity, new Pair<>(new ArrayList<>(), time));
            }
            Pair<List<UUID>, Long> currentData = playerPhialList.get(entity);
            List<UUID> list = currentData.getFirst();
            if (/*Math.abs(currentData.getSecond() - time) <= 1*/ currentData.getSecond() != time){
                resetLists(currentData, uuid, entity);
            } else {
                /*if (entity.isSneaking()){
                    Oneironaut.LOGGER.info(list.toString());
                }*/
                if (list.contains(uuid)){
                    uuid = UUID.randomUUID();
                    stackNbt.putUuid("uuid", uuid);
                }
                list.add(uuid);
            }
        }
    }
    @Override
    public void onCraft(ItemStack stack, World world, PlayerEntity player) {
        //stack.getOrCreateNbt().putInt("foundPhials", 1);
        UUID uuid = UUID.randomUUID();
        stack.getOrCreateNbt().putUuid("uuid", uuid);
        phialOwners.put(uuid, new Pair<>((Entity) player, time));
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
