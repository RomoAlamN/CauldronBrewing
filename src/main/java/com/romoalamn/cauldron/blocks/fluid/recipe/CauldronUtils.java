package com.romoalamn.cauldron.blocks.fluid.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.romoalamn.cauldron.CauldronMod;
import com.romoalamn.cauldron.blocks.fluid.PotionType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CauldronUtils {
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

    public static class FluidComponent {
        public PotionType potion;
        public int amount;

        public FluidComponent(PotionType in, int amt) {
            potion = in;
            amount = amt;
        }

        public FluidComponent copy() {
            return new FluidComponent(potion, amount);
        }

        public boolean isEmpty() {
            return this == EMPTY || amount <= 0;
        }

        public static FluidComponent EMPTY = new FluidComponent(PotionType.EMPTY, 0);
    }
}
