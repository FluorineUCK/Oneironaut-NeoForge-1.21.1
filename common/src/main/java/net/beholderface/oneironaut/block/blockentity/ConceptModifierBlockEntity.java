package net.beholderface.oneironaut.block.blockentity;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.block.ConceptModifierBlock;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifier;
import net.beholderface.oneironaut.item.WriteableBlockItem;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ConceptModifierBlockEntity extends HexBlockEntity {

    public static final String TAG_MODIFIER = "modifier";

    private final boolean typeNeedsIota;

    private ConceptModifier conceptModifier = null;
    public ConceptModifierBlockEntity(BlockPos pos, BlockState state) {
        super(OneironautBlockRegistry.CONCEPT_MODIFIER_ENTITY.get(), pos, state);
        if (state.getBlock() instanceof ConceptModifierBlock block){
            typeNeedsIota = ConceptModifier.typeRequiresIota(block.type);
        } else {
            typeNeedsIota = false;
        }
    }

    @Override
    protected void saveModData(NbtCompound tag) {
        if (this.getConceptModifier() != null){
            NBTHelper.putCompound(tag, TAG_MODIFIER, this.getConceptModifier().serialize());
        }
    }

    @Override
    protected void loadModData(NbtCompound tag) {
        if (world != null){
            BlockState state = this.world.getBlockState(this.pos);
            Block block = state.getBlock();
            if (block instanceof ConceptModifierBlock conceptBlock){
                boolean requiresIota = ConceptModifier.typeRequiresIota(conceptBlock.type);
                ConceptModifier modifierToSet = null;
                if ((!requiresIota || tag.contains(WriteableBlockItem.TAG_IOTA)) && this.world instanceof ServerWorld){
                    ConceptCoreBlockEntity core = conceptBlock.getCore(state, this.pos, (ServerWorld) this.world, null);
                    if (core != null){
                        if (requiresIota){
                            Iota iota = IotaType.deserialize(tag.getCompound(WriteableBlockItem.TAG_IOTA), (ServerWorld) world);
                            if (conceptBlock.type == ConceptModifier.ModifierType.ATTRIBUTE){
                                double attributeValue = ((DoubleIota)iota).getDouble();
                                ConceptModifier modifier = new ConceptModifier(core.getPos(), this.pos, null, conceptBlock.type);
                                modifier.setAttributeData(conceptBlock.getAttribute(), new EntityAttributeModifier(modifier.id, modifier.id.toString(),
                                        attributeValue, EntityAttributeModifier.Operation.ADDITION));
                                modifierToSet = modifier;
                            } else if (conceptBlock.type == ConceptModifier.ModifierType.REFERENCE_COMPARISON){
                                boolean overrideValue = ((BooleanIota)iota).getBool();
                                NbtCompound parameter = new NbtCompound();
                                parameter.putBoolean(ConceptModifier.TAG_COMPARISON_OVERRIDE, overrideValue);
                                modifierToSet = new ConceptModifier(core.getPos(), this.pos, parameter, conceptBlock.type);
                            }
                        } else {
                            modifierToSet = new ConceptModifier(core.getPos(), this.pos, null, conceptBlock.type);
                        }
                    }
                }
                this.setConceptModifier(modifierToSet);
                this.markDirty();
                return;
            }
        }
        this.setConceptModifier(ConceptModifier.deserialize(tag.getCompound(TAG_MODIFIER)));
        this.markDirty();
    }

    public void setConceptModifier(ConceptModifier newModifier){
        this.conceptModifier = newModifier;
    }
    public ConceptModifier getConceptModifier(){
        if (this.conceptModifier == null && !this.typeNeedsIota){
            if (this.world instanceof ServerWorld serverWorld){
                BlockState state = serverWorld.getBlockState(this.pos);
                if (state.getBlock() instanceof ConceptModifierBlock block){
                    ConceptCoreBlockEntity core = block.getCore(state, this.pos, serverWorld, null);
                    if (core != null){
                        ConceptModifier newModifier = new ConceptModifier(core.getPos(), this.pos, null, block.type);
                        this.setConceptModifier(newModifier);
                    }
                }
            }
        }
        return this.conceptModifier;
    }
    public boolean hasConceptModifier(){
        return this.getConceptModifier() != null;
    }
}
