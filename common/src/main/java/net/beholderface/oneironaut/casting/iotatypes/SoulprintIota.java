package net.beholderface.oneironaut.casting.iotatypes;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.beholderface.oneironaut.casting.idea.IdeaKeyable;
import net.beholderface.oneironaut.registry.OneironautIotaTypeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.beholderface.oneironaut.item.BottomlessMediaItem;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


public class SoulprintIota extends Iota implements IdeaKeyable {
    public SoulprintIota(@NotNull Pair<UUID, String> payload){
        super(OneironautIotaTypeRegistry.UUID, payload);
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    protected boolean toleratesOther(Iota that) {
        if (that.getType().equals(this.type)){
            SoulprintIota other = (SoulprintIota) that;
            Pair<UUID, String> thisPayload = (Pair<UUID, String>) this.payload;
            Pair<UUID, String> thatPayload = (Pair<UUID, String>) other.payload;
            //not checking the entity name because player name changes are a thing
            return thisPayload.getLeft().equals(thatPayload.getLeft());
        }
        return false;
    }

    public @NotNull NbtElement serialize() {
        var data = new NbtCompound();
        var payload = (Pair<UUID, String>) this.payload;
        data.putUuid("iota_uuid", payload.getLeft());
        data.putString("entity_name", payload.getRight());
        return data;
    }
    public @NotNull UUID getEntity(){
        return ((Pair<UUID, String>) this.payload).getLeft();
    }
    public static IotaType<SoulprintIota> TYPE = new IotaType<>() {
        @Override
        public SoulprintIota deserialize(NbtElement tag, ServerWorld world) throws IllegalArgumentException {
            var ctag = HexUtils.downcast(tag, NbtCompound.TYPE);
            return new SoulprintIota(new Pair<UUID, String>(ctag.getUuid("iota_uuid"), ctag.getString("entity_name")));
        }

        @Override
        public Text display(NbtElement tag) {
            var ctag = HexUtils.downcast(tag, NbtCompound.TYPE);
            var name = ctag.getString("entity_name");
            var uuid = ctag.getUuid("iota_uuid");
            Text original = Text.translatable("hexcasting.iota.oneironaut:uuid.label", name);
            ItemStack soulglimmerStack = HexItems.UUID_PIGMENT.getDefaultStack();
            FrozenPigment soulglimmercolor = new FrozenPigment(soulglimmerStack, uuid);
            Style coloredStyle = original.getStyle().withColor(IXplatAbstractions.INSTANCE.getColorProvider(soulglimmercolor).getColor(BottomlessMediaItem.time, Vec3d.ZERO));
            return original.copy().setStyle(coloredStyle);
        }
        @Override
        public int color() {
            return 0xff_7a63bc;
        }
    };

    @Override
    public String getKey() {
        return this.getEntity().toString() + "soul";
    }

    @Override
    public boolean isValidKey(CastingEnvironment env) {
        return true;
    }
}
