package com.romoalamn.cauldron.blocks.fluid;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.romoalamn.cauldron.CauldronMod;
import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronBrewingRecipe;
import com.romoalamn.cauldron.item.CauldronItems;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Stores all fluids, and the recipes for the cauldron (A little crowded, I know)
 */
public class CauldronUtils {
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
    public static ItemStack liquidToPotion(FluidComponent p0) {
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
    public static Optional<CauldronBrewingRecipe> getRecipe(FluidComponent stack, ItemStack ingredient) {
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
    public static Logger logger = LogManager.getLogger(CauldronMod.MODID + "_Utilities");

    public static CauldronBrewingRecipe defaultAmountRecipe(PotionType in, Item reagent, PotionType out) {
        return new CauldronBrewingRecipe(getFluid(in), getIngredient(reagent), getFluid(out));
    }

    public static CauldronBrewingRecipe defaultAmountRecipe(PotionType in, Tag<Item> reagent, PotionType out) {
        return new CauldronBrewingRecipe(getFluid(in), getIngredient(reagent), getFluid(out));
    }

    public static CauldronBrewingRecipe defaultAmountRecipe(PotionType in, Ingredient reagent, PotionType out) {
        return new CauldronBrewingRecipe(getFluid(in), reagent, getFluid(out));
    }

    public static DelegatedOptional<Fluid> getFluidDelegate(DelegatedOptional.Delegate<Fluid> supp) {
        return DelegatedOptional.of(supp);
    }

    public static FluidComponent getFluid(PotionType in) {
        return new FluidComponent(in, FluidAttributes.BUCKET_VOLUME / 2);
    }

    public static Ingredient getIngredient(Item reagent) {
        return Ingredient.fromStacks(new ItemStack(reagent));
    }

    public static Ingredient getIngredient(Tag<Item> tag) {
        return Ingredient.fromTag(tag);
    }

    public static EffectInstance getEffectFromJson(JsonElement effect) throws IllegalArgumentException{
        JsonObject obj = effect.getAsJsonObject();
        int amplifier = 0;
        int duration = 3600;
        Effect eff;
        if (obj.has("amplifier")) {
            amplifier = obj.get("amplifier").getAsInt();
        } else {
            logger.warn("No amplifier present, defaulting to level 1 (0)");
        }
        if (obj.has("duration")) {
            duration = obj.get("duration").getAsInt();
        } else {
            logger.warn("No duration present, using 3600");
        }
        if (obj.has("effect")) {
            eff = getEffectFromString(obj.get("effect").getAsString());
        } else {
            throw new IllegalArgumentException("Potion has no effect registered");
        }

        return new EffectInstance(eff, duration, amplifier);
    }

    public static Effect getEffectFromString(String eff) throws IllegalArgumentException{
        IForgeRegistry<Effect> registry = RegistryManager.ACTIVE.getRegistry(Effect.class);
        ResourceLocation loc = new ResourceLocation(eff);
        if(registry.containsKey(loc)){
            return registry.getValue(loc);
        }else{
            throw new IllegalArgumentException("Effect does not exist: " + eff);
        }

    }

    public static class DelegatedOptional<T> {
        Delegate<T> item;

        private DelegatedOptional(Delegate<T> d) {
            item = d;
        }

        public static <T> DelegatedOptional<T> of(Delegate<T> d) {
            return new DelegatedOptional<>(d);
        }

        public T get() {
            return item.get();
        }

        @FunctionalInterface
        public interface Delegate<T> {
            T get();
        }
    }

}
