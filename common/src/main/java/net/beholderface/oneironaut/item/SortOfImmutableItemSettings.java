package net.beholderface.oneironaut.item;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.util.Rarity;

public class SortOfImmutableItemSettings extends Item.Settings {

    protected RegistrySupplier<ItemGroup> tab = null;

    public SortOfImmutableItemSettings(){
        super();
    }

    @Override
    public SortOfImmutableItemSettings food(FoodComponent foodComponent) {
        SortOfImmutableItemSettings output = this.copy();
        output.foodComponent = foodComponent;
        return output;
    }

    @Override
    public SortOfImmutableItemSettings maxCount(int maxCount) {
        SortOfImmutableItemSettings output = this.copy();
        if (this.maxDamage > 0) {
            throw new RuntimeException("Unable to have damage AND stack.");
        } else {
            output.maxCount = maxCount;
            return output;
        }
    }

    @Override
    public SortOfImmutableItemSettings maxDamageIfAbsent(int maxDamage) {
        return this.maxDamage == 0 ? this.maxDamage(maxDamage) : this;
    }

    @Override
    public SortOfImmutableItemSettings maxDamage(int maxDamage) {
        SortOfImmutableItemSettings output = this.copy();
        output.maxDamage = maxDamage;
        output.maxCount = 1;
        return output;
    }

    @Override
    public SortOfImmutableItemSettings recipeRemainder(Item recipeRemainder) {
        SortOfImmutableItemSettings output = this.copy();
        output.recipeRemainder = recipeRemainder;
        return output;
    }

    @Override
    public SortOfImmutableItemSettings rarity(Rarity rarity) {
        SortOfImmutableItemSettings output = this.copy();
        output.rarity = rarity;
        return output;
    }

    @Override
    public SortOfImmutableItemSettings fireproof() {
        SortOfImmutableItemSettings output = this.copy();
        output.fireproof = fireproof;
        return output;
    }

    @Override
    public SortOfImmutableItemSettings requires(FeatureFlag... features) {
        SortOfImmutableItemSettings output = this.copy();
        output.requiredFeatures = FeatureFlags.FEATURE_MANAGER.featureSetOf(features);
        return output;
    }

    public SortOfImmutableItemSettings tab(RegistrySupplier<ItemGroup> group){
        SortOfImmutableItemSettings output = this.copy();
        output.tab = group;
        output.arch$tab(group);
        return output;
    }

    public SortOfImmutableItemSettings copy(){
        SortOfImmutableItemSettings output = new SortOfImmutableItemSettings();
        output.fireproof = this.fireproof;
        output.foodComponent = this.foodComponent;
        output.maxCount = this.maxCount;
        output.maxDamage = this.maxDamage;
        output.rarity = this.rarity;
        output.recipeRemainder = this.recipeRemainder;
        output.requiredFeatures = this.requiredFeatures;
        output.tab = this.tab;
        if (this.tab != null){ //not quite sure how arch tab stuff works so I'm doing this just in case
            output.arch$tab(this.tab);
        }
        return output;
    }
}
