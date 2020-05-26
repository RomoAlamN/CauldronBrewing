package com.romoalamn.cauldron;

import com.romoalamn.cauldron.blocks.fluid.CauldronFluids;
import com.romoalamn.cauldron.blocks.fluid.PotionType;
import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronBrewingRecipe;
import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronUtils;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class RecipeCreator {
    private Logger logger = LogManager.getLogger();

    int CREATES_LONG = 1;
    int CREATES_STRONG = 2;


    public void registerPotions(IForgeRegistry<CauldronBrewingRecipe> registry) {

        PotionType awkward = CauldronFluids.getPotion(CauldronMod.MODID + ":awkward");
        CauldronUtils.DelegatedOptional<Fluid> water = CauldronUtils.DelegatedOptional.of(() -> Fluids.WATER);

        //awkward potion
        registry.register(
                CauldronUtils.defaultAmountRecipe(
                        PotionType.EMPTY,
                        Tags.Items.CROPS_NETHER_WART,
                        awkward)
                        .setRegistryName(new ResourceLocation(CauldronMod.MODID, "awkward_recipe"))
        );
            /*
            Speed potions
             */
        PotionType swiftness = register(registry, "swiftness", Items.SUGAR, CREATES_LONG | CREATES_STRONG, awkward);
        PotionType leaping = register(registry, "leaping", Items.RABBIT_FOOT, CREATES_LONG | CREATES_STRONG, awkward);
        PotionType healing = register(registry, "healing", Items.GLISTERING_MELON_SLICE, CREATES_STRONG, awkward);
        PotionType poison = register(registry, "poison", Items.SPIDER_EYE, CREATES_LONG | CREATES_STRONG, awkward);
        PotionType waterBreathing = register(registry, "water_breathing", Items.PUFFERFISH, CREATES_LONG, awkward);
        PotionType fireResistance = register(registry, "fire_resistance", Items.MAGMA_CREAM, CREATES_LONG, awkward);
        PotionType nightVision = register(registry, "night_vision", Items.GOLDEN_CARROT, CREATES_LONG, awkward);
        PotionType strength = register(registry, "strength", Items.BLAZE_POWDER, CREATES_LONG | CREATES_STRONG, awkward);
        PotionType regeneration = register(registry, "regeneration", Items.GHAST_TEAR, CREATES_LONG | CREATES_STRONG, awkward);
        PotionType turtleMaster = register(registry, "turtle_master", Items.TURTLE_HELMET, CREATES_LONG | CREATES_STRONG, awkward);
        PotionType slowFalling = register(registry, "slow_falling", Items.PHANTOM_MEMBRANE, CREATES_LONG, awkward);

        // these both create slowness
        PotionType slowness = register(registry, "slowness", Items.FERMENTED_SPIDER_EYE, CREATES_LONG | CREATES_STRONG, swiftness);
        register(registry, "slowness", Items.FERMENTED_SPIDER_EYE, 0, leaping);

        //register long and strong conversions
        registerConversion(registry, "swiftness", "slowness", CREATES_LONG | CREATES_STRONG);
        registerConversion(registry, "leaping", "slowness", CREATES_LONG | CREATES_STRONG);

        PotionType harming = register(registry, "harming", Items.FERMENTED_SPIDER_EYE, CREATES_STRONG, healing);
        register(registry, "harming", Items.FERMENTED_SPIDER_EYE, 0, poison);

        registerConversion(registry, "healing", "harming", CREATES_STRONG);
        registerConversion(registry, "poison", "harming", CREATES_STRONG);

        PotionType invisibility = register(registry, "invisibility", Items.FERMENTED_SPIDER_EYE, CREATES_LONG, nightVision);

        registerConversion(registry, "night_vision", "invisibility", CREATES_LONG);

        PotionType weakness = register(registry, "weakness", Items.FERMENTED_SPIDER_EYE, CREATES_LONG, regeneration);
        register(registry, "weakness", Items.FERMENTED_SPIDER_EYE, 0, strength);

        registerConversion(registry, "strength", "weakness", CREATES_LONG);
        registerConversion(registry, "regeneration", "weakness", CREATES_LONG);


    }

    public PotionType register(IForgeRegistry<CauldronBrewingRecipe> registry, String name, Item reagent, int mask, PotionType input) {
        return register(registry, name, CauldronUtils.getIngredient(reagent), mask, input);
    }

    public PotionType register(IForgeRegistry<CauldronBrewingRecipe> registry, String name, Tag<Item> reagent, int mask, PotionType input) {
        return register(registry, name, CauldronUtils.getIngredient(reagent), mask, input);
    }

    public PotionType register(IForgeRegistry<CauldronBrewingRecipe> registry, String name, Ingredient reagent, int mask, PotionType input) {
        PotionType base = CauldronFluids.getPotion(CauldronMod.MODID, name);
//        logger.info(input.getRegistryName());
        String inputName = Objects.requireNonNull(input.getRegistryName()).getPath();

        registry.register(
                CauldronUtils.defaultAmountRecipe(
                        input,
                        reagent,
                        base
                ).setRegistryName(new ResourceLocation(CauldronMod.MODID, name + "_from_" + inputName))
        );
        if ((mask & CREATES_LONG) > 0) {
            PotionType longBase = CauldronFluids.getPotion(CauldronMod.MODID, "long_" + name);
            registry.register(
                    CauldronUtils.defaultAmountRecipe(
                            base,
                            Tags.Items.DUSTS_REDSTONE,
                            longBase
                    ).setRegistryName(new ResourceLocation(CauldronMod.MODID, "long_" + name + "_from_" + inputName))
            );
        }
        if ((mask & CREATES_STRONG) > 0) {
            PotionType strongBase = CauldronFluids.getPotion(CauldronMod.MODID, "strong_" + name);
            registry.register(
                    CauldronUtils.defaultAmountRecipe(
                            base,
                            Tags.Items.DUSTS_GLOWSTONE,
                            strongBase
                    ).setRegistryName(new ResourceLocation(CauldronMod.MODID, "strong_" + name + "_from_" + inputName))
            );
        }
        return base;
    }

    public void registerConversion(IForgeRegistry<CauldronBrewingRecipe> registry, String first, String last, int mask) {
        if ((mask & CREATES_LONG) > 0) {
            PotionType longFirst = CauldronFluids.getPotion(CauldronMod.MODID, "long_" + first);
            PotionType longSecond = CauldronFluids.getPotion(CauldronMod.MODID, "long_" + last);
            registry.register(
                    CauldronUtils.defaultAmountRecipe(
                            longFirst,
                            Items.FERMENTED_SPIDER_EYE,
                            longSecond
                    ).setRegistryName(new ResourceLocation(CauldronMod.MODID, "long_" + first + "_from_long_" + last))
            );
        }
        if ((mask & CREATES_STRONG) > 0) {
            PotionType strongFirst = CauldronFluids.getPotion(CauldronMod.MODID, "strong_" + first);
            PotionType strongSecond = CauldronFluids.getPotion(CauldronMod.MODID, "strong_" + last);

            registry.register(
                    CauldronUtils.defaultAmountRecipe(
                            strongFirst,
                            Items.FERMENTED_SPIDER_EYE,
                            strongSecond
                    ).setRegistryName(new ResourceLocation(CauldronMod.MODID, "strong_" + first + "_from_strong_" + last))
            );
        }
    }
}
