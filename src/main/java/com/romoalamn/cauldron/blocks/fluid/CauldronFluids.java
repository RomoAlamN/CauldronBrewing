package com.romoalamn.cauldron.blocks.fluid;

import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronBrewingRecipe;
import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronUtils;
import com.romoalamn.cauldron.item.CauldronItems;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Stores all fluids, and the recipes for the cauldron (A little crowded, I know)
 */
public class CauldronFluids {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * A list of all Fluids registered for the potions
     */
    private static final HashMap<ResourceLocation, PotionType> pots = new HashMap<>();
    /**
     * A list of all cauldron brewing recipes added using the Registry
     */
    private static final HashMap<ResourceLocation, CauldronBrewingRecipe> recipes = new HashMap<>();

    public static Collection<PotionType> getPotions() {
        return pots.values();
    }

    public static PotionType getPotion(@Nonnull String value) {
        return pots.getOrDefault(new ResourceLocation(value), PotionType.EMPTY);
    }

    /**
     * Register a Potion for use as a liquid, along with a properties object
     *
     * @param pot The type of the potion to be converted into a liquid. Tint is color data
     */
    public static void registerPot(PotionType pot) {
        pots.put(pot.getRegistryName(), pot);
    }

    /**
     * Check sif the ItemStack is a potion ingredient for a recipe involving an input of type stack.
     *
     * @param res   the itemstack to act as reagent
     * @param stack the Fluidstack to act as a base
     * @return whether or not such a potion exists
     */
    public static boolean isPotionIngredient(ItemStack res, PotionType stack) {
        for (CauldronBrewingRecipe recipe : recipes.values()) {
            if (recipe.isInput(stack) && recipe.isIngredient(res)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Registers a recipe. Do not call. Use the appropriate registryEvent instead:
     * RegistryEvent.Register&lt;CauldronBrewingRecipe&gt;
     *
     * @param recipe the recipe to register
     */
    public static void registerBrewingRecipe(CauldronBrewingRecipe recipe) {
        recipes.put(recipe.getRegistryName(), recipe);
    }

    /**
     * Creates a liquid from a fluidstack. The stack passed in is not modified, you must do that yourself.
     *
     * @param p0 the FluidStack
     * @return the Potion Item
     */
    public static ItemStack liquidToPotion(CauldronUtils.FluidComponent p0) {
        ItemStack potItem = new ItemStack(CauldronItems.POTION, 1);
        PotionType fluid = p0.potion;
        List<EffectInstance> pot = fluid.getEffects();
        PotionUtils.addPotionToItemStack(potItem, Potions.AWKWARD);
        PotionUtils.appendEffects(potItem, pot);
        return potItem;
    }

    public static PotionType getPotion(String namespace, String path) {
        return getPotion(namespace + ":" + path);
    }

    /**
     * Gets a recipe that uses the ItemStack as a reagent, and the fluidstack as a base
     *
     * @param stack      the fluid that acts as the base for the reaction
     * @param ingredient the ingredient that acts as a catalyst
     * @return An optional recipe, empty if no recipe exists.
     */
    public static Optional<CauldronBrewingRecipe> getRecipe(CauldronUtils.FluidComponent stack, ItemStack ingredient) {
        for (CauldronBrewingRecipe recipe : recipes.values()) {
            if (recipe.isInput(stack.potion) && recipe.isIngredient(ingredient)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }
    public static String getBase(PotionType type){
        String[] potion_name = type.getEffects().toString().split("\\.");
        return potion_name[potion_name.length - 1];
    }
    public static Collection<PotionType> getPotionsSorted() {
        Collection<PotionType> potions = getPotions();
        List<PotionType> pots = new ArrayList<>();
        pots.addAll(potions);
        pots.sort((pot1, pot2)->{
            // convert to strings
            String base1 = getBase(pot1);
            String base2 = getBase(pot2);

            return base1.compareTo(base2);
        });
        return pots;
    }
}
