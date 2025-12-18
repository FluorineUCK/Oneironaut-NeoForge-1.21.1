package net.beholderface.oneironaut.casting.conceptmodification;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.block.ConceptModifierBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ConceptModifier {

    public static final String TAG_COREPOS = "corePos";
    public static final String TAG_HOSTPOS = "hostPos";
    public static final String TAG_PARAMETERS = "parameters";
    public static final String TAG_MODIFIER_TYPE = "type";
    public static final String TAG_ATTRIBUTE_DATA = "attribute";
    public static final String TAG_ATTRIBUTE_MODIFIER = "modifier";
    public static final String TAG_COMPARISON_OVERRIDE = "comparison";
    public static final String TAG_POTENCY = "potency";

    public final BlockPos corePos;
    public final BlockPos hostPos;
    public final UUID id;
    public final NbtCompound parameters;
    public final ModifierType type;

    public ConceptModifier(@Nullable BlockPos corePos, @NotNull BlockPos hostPos, @Nullable NbtCompound parameters, ModifierType type){
        this.corePos = corePos;
        this.hostPos = hostPos;
        this.id = MiscAPIKt.toUUID(hostPos);
        this.parameters = parameters != null ? parameters : new NbtCompound();
        this.type = type;
    }

    public void onApply(ServerPlayerEntity player){
        if (this.type == ModifierType.ATTRIBUTE){
            EntityAttributeModifier modifier = this.getAttributeModifier();
            EntityAttribute attribute = this.getAttributeType();
            EntityAttributeInstance instance = player.getAttributeInstance(attribute);
            assert instance != null;
            //Oneironaut.LOGGER.info("Attempting to apply {} modifer to player", attribute);
            instance.addTemporaryModifier(modifier);
        }
    }

    public void onRemove(ServerPlayerEntity player){
        if (this.type == ModifierType.ATTRIBUTE){
            EntityAttribute attribute = this.getAttributeType();
            EntityAttributeInstance instance = player.getAttributeInstance(attribute);
            if (instance != null){
                instance.removeModifier(this.id);
            }
        }
    }

    public NbtCompound serialize(){
        NbtCompound nbt = new NbtCompound();
        if (this.corePos != null){
            NBTHelper.putCompound(nbt, TAG_COREPOS, NbtHelper.fromBlockPos(this.corePos));
        }
        NBTHelper.putCompound(nbt, TAG_HOSTPOS, NbtHelper.fromBlockPos(this.hostPos));
        NBTHelper.putCompound(nbt, TAG_PARAMETERS, parameters != null ? parameters : new NbtCompound());
        nbt.putString(TAG_MODIFIER_TYPE, this.type.toString());
        return nbt;
    }

    @Nullable
    public static ConceptModifier deserialize(NbtCompound nbt){
        try {
            BlockPos corePos = null;
            if (nbt.contains(TAG_COREPOS)){
                corePos = NbtHelper.toBlockPos(nbt.getCompound(TAG_COREPOS));
            }
            BlockPos hostPos = NbtHelper.toBlockPos(nbt.getCompound(TAG_HOSTPOS));
            NbtCompound parameters = nbt.getCompound(TAG_PARAMETERS);
            ModifierType type = ModifierType.valueOf(nbt.getString(TAG_MODIFIER_TYPE));
            return new ConceptModifier(corePos, hostPos, parameters, type);
        } catch (Exception e){
            return null;
        }
    }

    @Nullable
    public EntityAttributeModifier getAttributeModifier(){
        NbtCompound attributeNBT = this.parameters.getCompound(TAG_ATTRIBUTE_DATA);
        if (attributeNBT != null){
            NbtCompound modifierNBT = attributeNBT.getCompound(TAG_ATTRIBUTE_MODIFIER);
            if (modifierNBT != null){
                return EntityAttributeModifier.fromNbt(modifierNBT);
            }
        }
        return null;
    }

    @Nullable
    public EntityAttribute getAttributeType(){
        NbtCompound attributeNBT = this.parameters.getCompound(TAG_ATTRIBUTE_DATA);
        if (attributeNBT != null){
            String modifierID = attributeNBT.getString(TAG_MODIFIER_TYPE);
            if (modifierID != null){
                return Registries.ATTRIBUTE.get(new Identifier(modifierID));
            }
        }
        return null;
    }

    public void setAttributeData(EntityAttribute attribute, EntityAttributeModifier modifier){
        Identifier attributeID = Registries.ATTRIBUTE.getId(attribute);
        if (attributeID == null){
            throw new IllegalStateException("Attribute "+ attribute.toString() +" is not registered.");
        }
        NbtCompound attributeNBT = new NbtCompound();
        NBTHelper.putCompound(attributeNBT, TAG_ATTRIBUTE_MODIFIER, modifier.toNbt());
        NBTHelper.putString(attributeNBT, TAG_MODIFIER_TYPE, attributeID.toString());
        NBTHelper.putCompound(this.parameters, TAG_ATTRIBUTE_DATA, attributeNBT);
    }

    public long getMediaCost(Block block){
        if (block instanceof ConceptModifierBlock conceptModifierBlock && conceptModifierBlock.costCalulator != null){
            return (long) (conceptModifierBlock.costCalulator.apply(this.parameters) * MediaConstants.DUST_UNIT);
        }
        return 0;
    }

    public enum ModifierType {
        ANTIEROSION(false, "antierosion"), //implemented
        ATTRIBUTE(true, "attribute"), //implemented
        FALSY_REFERENCE(false, "falsy"), //implemented
        GTP_DROPREDUCTION(true, "gtp_splat"), //implemented
        KEEPINVENTORY(false, "keepinv"),
        LITTERBUG_REFERENCE(false, "litterbug"),
        NO_OVERCAST(false, "nobloodcast"),
        NONE(false, "none"),
        REFERENCE_COMPARISON(true, "ref_comparison"), //implemented
        TOTEM(false, "totem"),
        XL_REFERENCE(true, "ref_size"),
        STACK_LIMIT(false, "stack_limit");

        public final boolean requiresIota;
        public final String translationKey;
        ModifierType(boolean requiresIota, String translation){
            this.requiresIota = requiresIota;
            this.translationKey = "oneironaut.conceptmodifier." + translation;
        }
    }
}
