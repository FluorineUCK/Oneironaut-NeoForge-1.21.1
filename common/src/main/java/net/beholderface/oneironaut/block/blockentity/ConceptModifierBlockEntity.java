package net.beholderface.oneironaut.block.blockentity;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.datafixers.util.Pair;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.block.ConceptModifierBlock;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifier;
import net.beholderface.oneironaut.item.WriteableBlockItem;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.beholderface.oneironaut.registry.OneironautItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class ConceptModifierBlockEntity extends HexBlockEntity {

    public static final String TAG_MODIFIER = "modifier";

    private final boolean typeNeedsIota;

    public final ConceptModifier.ModifierType modifierType;

    private ConceptModifier conceptModifier = null;
    public ConceptModifierBlockEntity(BlockPos pos, BlockState state) {
        super(OneironautBlockRegistry.CONCEPT_MODIFIER_ENTITY.get(), pos, state);
        if (state.getBlock() instanceof ConceptModifierBlock block){
            typeNeedsIota = block.type.requiresIota;
            this.modifierType = block.type;
        } else {
            typeNeedsIota = false;
            this.modifierType = null;
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
                boolean requiresIota = conceptBlock.type.requiresIota;
                ConceptModifier modifierToSet = null;
                if ((!requiresIota || tag.contains(WriteableBlockItem.TAG_IOTA))){
                    ConceptCoreBlockEntity core = conceptBlock.getCore(state, this.pos, this.world, null);
                    BlockPos corePos = core != null ? core.getPos() : null;
                    if (requiresIota){
                        Iota iota = IotaType.deserialize(tag.getCompound(WriteableBlockItem.TAG_IOTA), null);
                        if (conceptBlock.type == ConceptModifier.ModifierType.ATTRIBUTE){
                            double attributeValue = ((DoubleIota)iota).getDouble();
                            ConceptModifier modifier = new ConceptModifier(corePos, this.pos, null, conceptBlock.type);
                            modifier.setAttributeData(conceptBlock.getAttribute(), new EntityAttributeModifier(modifier.id, modifier.id.toString(),
                                    attributeValue, EntityAttributeModifier.Operation.MULTIPLY_BASE));
                            modifierToSet = modifier;
                        } else if (conceptBlock.type == ConceptModifier.ModifierType.REFERENCE_COMPARISON){
                            boolean overrideValue = ((BooleanIota)iota).getBool();
                            NbtCompound parameter = new NbtCompound();
                            parameter.putBoolean(ConceptModifier.TAG_COMPARISON_OVERRIDE, overrideValue);
                            modifierToSet = new ConceptModifier(corePos, this.pos, parameter, conceptBlock.type);
                        }
                    } else {
                        modifierToSet = new ConceptModifier(corePos, this.pos, null, conceptBlock.type);
                    }
                }
                this.setConceptModifier(modifierToSet);
            }
        }
        if (this.conceptModifier == null){
            this.setConceptModifier(ConceptModifier.deserialize(tag.getCompound(TAG_MODIFIER)));
        }
    }

    public void tick(World world, BlockPos pos, BlockState state){
        if (this.conceptModifier == null){
            this.getConceptModifier();
            if (this.conceptModifier != null){
                this.markDirty();
            }
        }
    }

    public void setConceptModifier(ConceptModifier newModifier){
        this.conceptModifier = newModifier;
        this.markDirty();
    }
    public ConceptModifier getConceptModifier(){
        if (this.conceptModifier == null && !this.typeNeedsIota && this.modifierType != ConceptModifier.ModifierType.NONE && this.world != null){
            BlockState state = world.getBlockState(this.pos);
            if (state.getBlock() instanceof ConceptModifierBlock block){
                ConceptCoreBlockEntity core = block.getCore(state, this.pos, world, null);
                BlockPos corePos = core != null ? core.getPos() : null;
                ConceptModifier newModifier = new ConceptModifier(corePos, this.pos, null, block.type);
                this.setConceptModifier(newModifier);
            }
        }
        return this.conceptModifier;
    }
    public boolean hasConceptModifier(){
        return this.getConceptModifier() != null;
    }

    public static void applyScryingLensOverlay(List<Pair<ItemStack, Text>> lines,
                                               BlockState state, BlockPos pos, PlayerEntity observer, World world, Direction hitFace){
        ConceptModifierBlockEntity be = (ConceptModifierBlockEntity) world.getBlockEntity(pos);
        if (be != null){
            ConceptModifier modifier = be.getConceptModifier();
            if (modifier != null){
                String translation = modifier.type.translationKey;
                lines.add(Pair.of(state.getBlock().asItem().getDefaultStack(), Text.translatable(translation)));
                ConceptModifier.ModifierType type = modifier.type;
                if (type == ConceptModifier.ModifierType.ATTRIBUTE){
                    assert modifier.getAttributeType() != null;
                    double modifierValue = modifier.getAttributeModifier().getValue();
                    lines.add(Pair.of(HexItems.ABACUS.getDefaultStack(), Text.translatable("oneironaut.conceptmodifier.attribute.overlay1." + (modifierValue > 0 ? "positive" : "negative"))
                            .append(Text.translatable(modifier.getAttributeType().getTranslationKey()))
                            .append(Text.translatable("oneironaut.conceptmodifier.attribute.overlay2", Math.abs(modifierValue) * 100))));
                } else if (type.requiresIota){
                    lines.add(Pair.of(HexItems.ABACUS.getDefaultStack(), Text.literal(modifier.parameters.asString())));
                }
                if (modifier.corePos == null){
                    lines.add(Pair.of(OneironautItemRegistry.CONCEPT_CORE.get().getDefaultStack(), Text.translatable("oneironaut.conceptmodifier.nocore")));
                }
            }
        }
    }
}
