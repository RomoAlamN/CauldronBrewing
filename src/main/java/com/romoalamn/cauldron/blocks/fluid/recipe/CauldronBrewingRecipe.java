package com.romoalamn.cauldron.blocks.fluid.recipe;

import com.romoalamn.cauldron.blocks.fluid.PotionType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;

public class CauldronBrewingRecipe extends ForgeRegistryEntry<CauldronBrewingRecipe> {
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

    RecipeStates state;

    /**
     * Default constructor
     * @param input the input
     * @param ingredient the ingredient for the recipe
     * @param output the liquid in the cauldron after a successful brewing
     */
    public CauldronBrewingRecipe(CauldronUtils.FluidComponent input, Ingredient ingredient, CauldronUtils.FluidComponent output) {
        this(input, ingredient, output, RecipeStates.HEATING);
    }
    public CauldronBrewingRecipe(CauldronUtils.FluidComponent input, Ingredient ingredient, CauldronUtils.FluidComponent output, RecipeStates state){
        this.input = input;
        this.ingredient = ingredient;
        this.output  = output;
        this.state = state;
    }

    /**
     * Is the FluidStack stack the same fluid as our input
     * @param stack the stack to check equality on
     * @return whether or not the two fluids are the same type
     */
    public boolean isInput(@Nonnull PotionType stack) {
        return stack == input.potion;
    }

    /**
     * Returns the result of a successful brewing.
     * @param input The input fluid in the cauldron
     * @param ingredient The item in the player's hand
     * @return the fluid that corresponds to the recipe above
     */
    public CauldronUtils.FluidComponent getOutput(CauldronUtils.FluidComponent input, ItemStack ingredient) {
        if(isInput(input.potion) && isIngredient(ingredient) && input.amount > this.input.amount){
            return new CauldronUtils.FluidComponent(getOutput().potion, getOutput().amount);
        }else{
            return CauldronUtils.FluidComponent.EMPTY;
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

    public RecipeStates getState() {
        return state;
    }

    /**
     * Returns whether the ItemStack fits the criteria for the Ingredient
     * @param ingredient the itemstack to check
     * @return see description
     */
    public boolean isIngredient(ItemStack ingredient) {
        return this.ingredient.test(ingredient);
    }
}
