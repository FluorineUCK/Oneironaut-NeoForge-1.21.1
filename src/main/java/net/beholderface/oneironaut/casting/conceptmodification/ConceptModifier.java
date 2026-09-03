package net.beholderface.oneironaut.casting.conceptmodification;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.block.ConceptModifierBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
    public final CompoundTag parameters;
    public final ModifierType type;

    public ConceptModifier(@Nullable BlockPos corePos, @NotNull BlockPos hostPos, @Nullable CompoundTag parameters, ModifierType type){
        this.corePos = corePos;
        this.hostPos = hostPos;
        this.id = MiscAPIKt.toUUID(hostPos);
        this.parameters = parameters != null ? parameters : new CompoundTag();
        this.type = type;
    }

    public void onApply(ServerPlayer player){
        if (this.type == ModifierType.ATTRIBUTE){
            AttributeModifier modifier = this.getAttributeModifier();
            Holder<Attribute> attribute = this.getAttributeType();
            AttributeInstance instance = player.getAttribute(attribute);
            assert instance != null;
            //Oneironaut.LOGGER.info("Attempting to apply {} modifer to player", attribute);
            instance.addTransientModifier(modifier);
        }
    }

    public void onRemove(ServerPlayer player){
        if (this.type == ModifierType.ATTRIBUTE){
            Holder<Attribute> attribute = this.getAttributeType();
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null){
                instance.removeModifier(attributeModifierId());
            }
        }
    }

    public CompoundTag serialize(){
        CompoundTag nbt = new CompoundTag();
        if (this.corePos != null){
            nbt.put(TAG_COREPOS, NbtUtils.writeBlockPos(this.corePos));
        }
        nbt.put(TAG_HOSTPOS, NbtUtils.writeBlockPos(this.hostPos));
        NBTHelper.putCompound(nbt, TAG_PARAMETERS, parameters != null ? parameters : new CompoundTag());
        nbt.putString(TAG_MODIFIER_TYPE, this.type.toString());
        return nbt;
    }

    @Nullable
    public static ConceptModifier deserialize(CompoundTag nbt){
        try {
            BlockPos corePos = null;
            if (nbt.contains(TAG_COREPOS)){
                corePos = NbtUtils.readBlockPos(nbt, TAG_COREPOS).orElse(null);
            }
            BlockPos hostPos = NbtUtils.readBlockPos(nbt, TAG_HOSTPOS).orElseThrow();
            CompoundTag parameters = nbt.getCompound(TAG_PARAMETERS);
            ModifierType type = ModifierType.valueOf(nbt.getString(TAG_MODIFIER_TYPE));
            return new ConceptModifier(corePos, hostPos, parameters, type);
        } catch (Exception e){
            return null;
        }
    }

    @Nullable
    public AttributeModifier getAttributeModifier(){
        CompoundTag attributeNBT = this.parameters.getCompound(TAG_ATTRIBUTE_DATA);
        if (attributeNBT != null){
            CompoundTag modifierNBT = attributeNBT.getCompound(TAG_ATTRIBUTE_MODIFIER);
            if (modifierNBT != null){
                return AttributeModifier.load(modifierNBT);
            }
        }
        return null;
    }

    @Nullable
    public Holder<Attribute> getAttributeType(){
        CompoundTag attributeNBT = this.parameters.getCompound(TAG_ATTRIBUTE_DATA);
        if (attributeNBT != null){
            String modifierID = attributeNBT.getString(TAG_MODIFIER_TYPE);
            if (modifierID != null){
                ResourceLocation id = ResourceLocation.tryParse(modifierID);
                return id == null ? null : BuiltInRegistries.ATTRIBUTE.getHolder(id).orElse(null);
            }
        }
        return null;
    }

    public void setAttributeData(Holder<Attribute> attribute, AttributeModifier modifier){
        ResourceLocation attributeID = BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
        if (attributeID == null){
            throw new IllegalStateException("Attribute "+ attribute +" is not registered.");
        }
        CompoundTag attributeNBT = new CompoundTag();
        NBTHelper.putCompound(attributeNBT, TAG_ATTRIBUTE_MODIFIER, modifier.save());
        NBTHelper.putString(attributeNBT, TAG_MODIFIER_TYPE, attributeID.toString());
        NBTHelper.putCompound(this.parameters, TAG_ATTRIBUTE_DATA, attributeNBT);
    }

    public ResourceLocation attributeModifierId() {
        return ResourceLocation.fromNamespaceAndPath("oneironaut", "concept_modifier/" + this.id);
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
        STACK_LIMIT(false, "stack_limit"); //implemented

        public final boolean requiresIota;
        public final String translationKey;
        ModifierType(boolean requiresIota, String translation){
            this.requiresIota = requiresIota;
            this.translationKey = "oneironaut.conceptmodifier." + translation;
        }
    }
}
