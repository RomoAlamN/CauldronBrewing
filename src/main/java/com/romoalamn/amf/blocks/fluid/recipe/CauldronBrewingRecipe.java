package com.romoalamn.amf.blocks.fluid.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CauldronBrewingRecipe implements IForgeRegistryEntry<CauldronBrewingRecipe> {
    /**
     * This is the Fluidstack held in the cauldron. (Not actually.)
     * If this fluid is in the cauldron, we can check if the ingredient is proper
     */
    CauldronUtils.FluidComponent input;
    /**
     * This is the ingredient the player is holding in order to create the potion
     */
    Ingredient ingredient;
    /**
     * this is the Fluid that is in the Cauldron after a successful recipe
     */
    CauldronUtils.FluidComponent output;
    /**
     * This is the registry name of the recipe. Used in the to register the recipe in the registry
     */
    ResourceLocation registryName = null;

    /**
     * Default constructor
     * @param input the input
     * @param ingredient the ingredient for the recipe
     * @param output the liquid in the cauldron after a successful brewing
     */
    public CauldronBrewingRecipe(CauldronUtils.FluidComponent input, Ingredient ingredient, CauldronUtils.FluidComponent output) {
        this.input = input;
        this.ingredient = ingredient;
        this.output  = output;
    }

    /**
     * Is the FluidStack stack the same fluid as our input
     * @param stack the stack to check equality on
     * @return whether or not the two fluids are the same type
     */
    public boolean isInput(@Nonnull FluidStack stack) {
        return stack.getFluid() == input.item.get();
    }

    /**
     * Returns the result of a successful brewing.
     * @param input The input fluid in the cauldron
     * @param ingredient The item in the player's hand
     * @return the fluid that corresponds to the recipe above
     */
    public FluidStack getOutput(FluidStack input, ItemStack ingredient) {
        if(isInput(input) && isIngredient(ingredient) && input.getAmount() > this.input.amount){
            return new FluidStack(getOutput().item.get(), getOutput().amount);
        }else{
            return FluidStack.EMPTY;
        }
    }

    /**
     * Gets the fluidstack that is the input
     * @return see description
     */
    public CauldronUtils.FluidComponent getInput() {
        return input;
    }

    /**
     * Returns an Ingredient that corresponds to the item that must be used on the cauldron for this recipe to succeed
     * @return see description
     */
    public Ingredient getIngredient() {
        return ingredient;
    }

    /**
     * Returns the hypothetical output of the recipe
     * @return see description
     */
    public CauldronUtils.FluidComponent getOutput() {
        return output;
    }

    /**
     * Returns whether the ItemStack fits the criteria for the Ingredient
     * @param ingredient the itemstack to check
     * @return see description
     */
    public boolean isIngredient(ItemStack ingredient) {
        return this.ingredient.test(ingredient);
    }

    /**
     * Sets a unique name for this Item. This should be used for uniquely identify the instance of the Item.
     * This is the valid replacement for the atrocious 'getUnlocalizedName().substring(6)' stuff that everyone does.
     * Unlocalized names have NOTHING to do with unique identifiers. As demonstrated by vanilla blocks and items.
     * <p>
     * The supplied name will be prefixed with the currently active mod's modId.
     * If the supplied name already has a prefix that is different, it will be used and a warning will be logged.
     * <p>
     * If a name already exists, or this Item is already registered in a registry, then an IllegalStateException is thrown.
     * <p>
     * Returns 'this' to allow for chaining.
     *
     * @param name Unique registry name
     * @return This instance
     */
    @Override
    public CauldronBrewingRecipe setRegistryName(ResourceLocation name) {
        if(name != null){
            registryName = name;
        }else{
            throw new IllegalStateException("Recipe already has name");
        }
        return this;
    }

    /**
     * A unique identifier for this entry, if this entry is registered already it will return it's official registry name.
     * Otherwise it will return the name set in setRegistryName().
     * If neither are valid null is returned.
     *
     * @return Unique identifier or null.
     */
    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }

    /**
     * Determines the type for this entry, used to look up the correct registry in the global registries list as there can only be one
     * registry per concrete class.
     *
     * @return Root registry type.
     */
    @Override
    public Class<CauldronBrewingRecipe> getRegistryType() {
        return (Class<CauldronBrewingRecipe>) getClass();
    }
}
