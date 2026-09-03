package net.beholderface.oneironaut.item;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.api.misc.MediaConstants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PseudoamethystShard extends Item /*implements ADMediaHolder*/ {

    public PseudoamethystShard(net.minecraft.world.item.Item.Properties settings){
        super(settings);
    }
    private static final int PSEUDOSHARD_UNIT = (int) (MediaConstants.SHARD_UNIT * 1.5);

    /*@Override
    public static int getMedia() {
        return PSEUDOSHARD_UNIT;
    }

    @Override
    public int getMaxMedia() {
        return PSEUDOSHARD_UNIT;
    }

    @Override
    public void setMedia(int media) {

    }

    @Override
    public boolean canRecharge() {
        return false;
    }

    @Override
    public boolean canProvide() {
        return true;
    }

    @Override
    public int getConsumptionPriority() {
        return 1500;
    }

    @Override
    public boolean canConstructBattery() {
        return true;
    }*/

    /*@Override
    public int getMedia(ItemStack stack) {
        return PSEUDOSHARD_UNIT * stack.getCount();
    }

    @Override
    public int getMaxMedia(ItemStack stack) {
        return PSEUDOSHARD_UNIT * stack.getMaxCount();
    }

    @Override
    public void setMedia(ItemStack stack, int media) {
        stack.setCount(media / PSEUDOSHARD_UNIT);
    }

    @Override
    public boolean canProvideMedia(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canRecharge(ItemStack stack) {
        return false;
    }*/



}
