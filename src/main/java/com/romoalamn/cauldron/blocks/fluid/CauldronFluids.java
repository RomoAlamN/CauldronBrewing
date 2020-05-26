package com.romoalamn.cauldron.blocks.fluid;

import com.romoalamn.cauldron.CauldronMod;
import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronBrewingRecipe;
import com.romoalamn.cauldron.item.CauldronItems;
import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Stores all fluids, and the recipes for the cauldron (A little crowded, I know)
 */
public class CauldronFluids {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * A list of all Fluids registered for the potions
     */
    private static final HashMap<ResourceLocation, PotionFluid> pots = new HashMap<>();
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
     * @param pot  the type of potion to create, stores color information
     * @param data the forward declarations that will be populated when this function is actually called
     * @return A properties object for use in creating the Fluid block
     */
    public static PotionFluid.Properties makeProps(PotionType pot, RegisterForwardPotion data) {
        return new PotionFluid.Properties(data.still, data.flowing,
                FluidAttributes.builder(FluidStill, FluidFlowing)
                        .color(pot.tint)
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
        LOGGER.info("Potion registered for type: {}", pot.parent);
        RegisterForwardPotion forward = new RegisterForwardPotion();
        PotionFluid fluid = new PotionFluid(pot);
        pots.put(pot.getRegistryName(), fluid);
        forward.still = FluidsR.register(Objects.requireNonNull(pot.getRegistryName()).getPath() + "_still", () -> fluid.createSource(makeProps(pot, forward)).get());
        forward.flowing = FluidsR.register(pot.getRegistryName().getPath() + "_flow", () -> fluid.createFlowing(makeProps(pot, forward)).get());
        forward.block = BlockR.register(Objects.requireNonNull(pot.getRegistryName()).getPath() +"_block", () ->
                new FlowingFluidBlock(forward.still, Block.Properties.create(Material.WATER).doesNotBlockMovement().hardnessAndResistance(100.0f).noDrops()));
    }

    /**
     * Does the FluidStack contain a potion fluid
     * <p>
     * <p>
     * Note: Water is a not potion fluid
     *
     * @param stack the fluidstack to check
     * @return see description
     */
    public static boolean isPotion(FluidStack stack) {
        if(stack.getFluid() instanceof PotionFluid.Source){
            return true;
        }
        for (PotionFluid pot : pots.values()) {
            if (stack.getFluid() == pot.getSourcePotion().get()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the PotionFluid from a FluidStack.
     *
     * @param stack the fluidstack to extract from
     * @return the potionfluid
     */
    public static PotionFluid getPotionForLiquid(FluidStack stack) {
        for (PotionFluid pot : pots.values()) {
            LOGGER.info("{} vs {}", stack.getFluid(), pot.getSourcePotion().get());
            if (stack.getFluid() == pot.getSourcePotion().get()) {
                return pot;
            }
        }
        return PotionFluid.EMPTY;
    }

    /**
     * Check sif the ItemStack is a potion ingredient for a recipe involving an input of type stack.
     *
     * @param res   the itemstack to act as reagent
     * @param stack the Fluidstack to act as a base
     * @return whether or not such a potion exists
     */
    public static boolean isPotionIngredient(ItemStack res, FluidStack stack) {
        if((stack.getFluid() != Fluids.WATER) && !CauldronFluids.isPotion(stack)){
            return false;
        }
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
    public static ItemStack liquidToPotion(FluidStack p0) {
        ItemStack potItem = new ItemStack(CauldronItems.POTION, 1);
        LOGGER.info("Retrieving potion for fluid type {}", p0.getFluid());
        if (isPotion(p0)) {
            PotionFluid fluid = getPotionForLiquid(p0);
            List<EffectInstance> pot = fluid.getPotion();
            PotionUtils.addPotionToItemStack(potItem, Potions.AWKWARD);
            PotionUtils.appendEffects(potItem, pot);
        }
        return potItem;
    }

    public static PotionFluid getPotionFluid(String resource) {
        return pots.get(new ResourceLocation(resource));
    }
    public static PotionFluid getPotionFluid(String namespace, String path){
        return getPotionFluid(namespace + ":" + path);
    }

    /**
     * Gets a recipe that uses the ItemStack as a reagent, and the fluidstack as a base
     *
     * @param stack      the fluid that acts as the base for the reaction
     * @param ingredient the ingredient that acts as a catalyst
     * @return An optional recipe, empty if no recipe exists.
     */
    public static Optional<CauldronBrewingRecipe> getRecipe(FluidStack stack, ItemStack ingredient) {
        for (CauldronBrewingRecipe recipe : recipes.values()) {
            if (recipe.isInput(stack) && recipe.isIngredient(ingredient)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    /**
     * Called by my init function to register the DeferredRegistries.
     *
     * @param ev the Mod event bus
     */
    public static void registerFluidCreator(IEventBus ev) {
        BlockR.register(ev);
        ItemsR.register(ev);
        FluidsR.register(ev);
    }
}
