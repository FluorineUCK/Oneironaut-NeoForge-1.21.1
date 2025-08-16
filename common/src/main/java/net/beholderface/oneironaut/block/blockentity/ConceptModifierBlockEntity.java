package net.beholderface.oneironaut.block.blockentity;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.beholderface.oneironaut.block.ConceptModifierBlock;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifier;
import net.beholderface.oneironaut.item.WriteableBlockItem;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ConceptModifierBlockEntity extends HexBlockEntity {

    public static final String TAG_MODIFIER = "modifier";

    private ConceptModifier conceptModifier = null;
    public ConceptModifierBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(OneironautBlockRegistry.CONCEPT_MODIFIER_ENTITY.get(), pWorldPosition, pBlockState);
    }

    @Override
    protected void saveModData(NbtCompound tag) {
        if (this.getConceptModifier() != null){
            NBTHelper.putCompound(tag, TAG_MODIFIER, this.getConceptModifier().serialize());
        }
    }

    @Override
    protected void loadModData(NbtCompound tag) {
        if (tag.contains(WriteableBlockItem.TAG_IOTA) && this.world != null && this.world instanceof ServerWorld){
            Iota iota = IotaType.deserialize(tag.getCompound(WriteableBlockItem.TAG_IOTA), (ServerWorld) world);
            BlockState state = this.world.getBlockState(this.pos);
            ConceptModifierBlock block = (ConceptModifierBlock) state.getBlock();
            if (block.type == ConceptModifier.ModifierType.ATTRIBUTE){
                double attributeValue = ((DoubleIota)iota).getDouble();
                ConceptCoreBlockEntity core = block.getCore(state, this.pos, (ServerWorld) this.world, null);
                if (core != null){
                    ConceptModifier modifier = new ConceptModifier(core.getPos(), this.pos, null, block.type);
                    modifier.setAttributeData(block.getAttribute(), new EntityAttributeModifier(modifier.id, modifier.id.toString(),
                            attributeValue, EntityAttributeModifier.Operation.ADDITION));
                    this.setConceptModifier(modifier);
                }
            }
            return;
        }
        this.setConceptModifier(ConceptModifier.deserialize(tag.getCompound(TAG_MODIFIER)));
    }

    public void setConceptModifier(ConceptModifier newModifier){
        this.conceptModifier = newModifier;
    }
    public ConceptModifier getConceptModifier(){
        return this.conceptModifier;
    }
    public boolean hasConceptModifier(){
        return this.getConceptModifier() != null;
    }
}
