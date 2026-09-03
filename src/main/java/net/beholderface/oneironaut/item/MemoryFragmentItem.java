package net.beholderface.oneironaut.item;

import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.beholderface.oneironaut.Oneironaut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryFragmentItem extends Item {

    public final List<ResourceLocation> names;

    public MemoryFragmentItem(net.minecraft.world.item.Item.Properties settings, List<ResourceLocation> advancementNames) {
        super(settings);
        this.names = advancementNames;
    }

    public static final List<ResourceLocation> NAMES_TOWER = List.of(new ResourceLocation[]{
            Oneironaut.id("lore/treatise1"),
            Oneironaut.id("lore/treatise2"),
            Oneironaut.id("lore/treatise3"),
            Oneironaut.id("lore/treatise4"),
            Oneironaut.id("lore/science1"),
            Oneironaut.id("lore/science2"),
            Oneironaut.id("lore/science3")
    });

    public static final String CRITEREON_KEY = "grant";

    //mostly stolen from base hex lore fragment code
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        player.playSound(HexSounds.READ_LORE_FRAGMENT.value(), 1f, 1f);
        var handStack = player.getItemInHand(usedHand);
        if (!(player instanceof ServerPlayer splayer)) {
            handStack.shrink(1);
            return InteractionResultHolder.success(handStack);
        }
        PlayerAdvancements tracker = splayer.getAdvancements();
        AdvancementHolder rootAdvancement = splayer.level().getServer().getAdvancements().get(Oneironaut.id("lore/root"));
        if (rootAdvancement != null && !tracker.getOrStartProgress(rootAdvancement).isDone()){
            tracker.award(rootAdvancement, CRITEREON_KEY);
        }
        AdvancementHolder unfoundLore = null;
        var shuffled = new ArrayList<>(this.names);
        Collections.shuffle(shuffled);
        for (var advID : shuffled) {
            var adv = splayer.level().getServer().getAdvancements().get(advID);
            if (adv == null) {
                continue; // uh oh
            }

            if (!tracker.getOrStartProgress(adv).isDone()) {
                unfoundLore = adv;
                break;
            }
        }

        if (unfoundLore == null) {
            splayer.displayClientMessage(Component.translatable("item.oneironaut.memory_fragment.all"), true);
            splayer.giveExperiencePoints(20);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    HexSounds.READ_LORE_FRAGMENT, SoundSource.PLAYERS, 1f, 1f);
        } else {
            tracker.award(unfoundLore, CRITEREON_KEY);
        }

        CriteriaTriggers.CONSUME_ITEM.trigger(splayer, handStack);
        splayer.awardStat(Stats.ITEM_USED.get(this));
        handStack.shrink(1);

        return InteractionResultHolder.success(handStack);
    }

}
