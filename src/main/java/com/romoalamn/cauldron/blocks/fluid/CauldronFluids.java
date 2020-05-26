package com.romoalamn.cauldron.blocks.fluid;

import com.romoalamn.cauldron.CauldronMod;
import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronBrewingRecipe;
import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronUtils;
import com.romoalamn.cauldron.item.CauldronItems;
import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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

    /**
     * A new way of registering items and blocks
     */
    public static final DeferredRegister<Fluid> FluidsR = new DeferredRegister<>(ForgeRegistries.FLUIDS, CauldronMod.MODID);
    public static final DeferredRegister<Item> ItemsR = new DeferredRegister<>(ForgeRegistries.ITEMS, CauldronMod.MODID);
    public static final DeferredRegister<Block> BlockR = new DeferredRegister<>(ForgeRegistries.BLOCKS, CauldronMod.MODID);
    /**
     * The texture to use for still and flowing potions, in the form of a ResourceLocation
     */
    public static final ResourceLocation FluidStill = new ResourceLocation("minecraft:block/water_still");
    public static final ResourceLocation FluidFlowing = new ResourceLocation("minecraft:block/water_flow");

    public static Collection<PotionType> getPotions() {
        return pots.values();
    }

    public static PotionType getPotion(@Nonnull String value) {
        return pots.getOrDefault(new ResourceLocation(value), PotionType.EMPTY);
    }

    /**
     * A class that contains forward declarations, so I don't have to use a global.
     */
    private static class RegisterForwardPotion {
        RegistryObject<FlowingFluidBlock> block;
        RegistryObject<FlowingFluid> flowing;
        RegistryObject<FlowingFluid> still;
    }

    /**
     * Makes the properties that all Potions use, also connects the block and the bucket to the fluid
     *
     * @param data the forward declarations that will be populated when this function is actually called
     * @return A properties object for use in creating the Fluid block
     */
    public static ForgeFlowingFluid.Properties makeProps(RegisterForwardPotion data) {
        return new ForgeFlowingFluid.Properties(data.still, data.flowing,
                FluidAttributes.builder(FluidStill, FluidFlowing)
                        .color(0xFFFFFFFF)
        )
                .bucket(null)
                .block(data.block);
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
}
