package com.romoalamn.cauldron.blocks.fluid.recipe;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;

public class CauldronUtils {
    public static CauldronBrewingRecipe defaultAmountRecipe(DelegatedOptional<Fluid> in, Item reagent, DelegatedOptional<Fluid> out) {
        return new CauldronBrewingRecipe(getFluid(in), getIngredient(reagent), getFluid(out));
    }
    public static CauldronBrewingRecipe defaultAmountRecipe(DelegatedOptional<Fluid> in, Tag<Item> reagent, DelegatedOptional<Fluid> out){
        return new CauldronBrewingRecipe(getFluid(in), getIngredient(reagent), getFluid(out));
    }

    public static CauldronBrewingRecipe defaultAmountRecipe(DelegatedOptional<Fluid> in, Ingredient reagent, DelegatedOptional<Fluid> out) {
        return new CauldronBrewingRecipe(getFluid(in), reagent, getFluid(out));
    }

    public static DelegatedOptional<Fluid> getFluidDelegate(DelegatedOptional.Delegate<Fluid> supp){
        return DelegatedOptional.of(supp);
    }
    public static FluidComponent getFluid(DelegatedOptional<Fluid> in){
        return new FluidComponent(in, 750);
    }
    public static Ingredient getIngredient(Item reagent){
        return Ingredient.fromStacks(new ItemStack(reagent));
    }

    public static Ingredient getIngredient(Tag<Item> tag){
        return Ingredient.fromTag(tag);
    }

    public static class DelegatedOptional<T>{
        Delegate<T> item;

        private DelegatedOptional(Delegate<T> d){
            item = d;
        }

        public static <T> DelegatedOptional<T> of(Delegate<T> d){
            return new DelegatedOptional<>(d);
        }

        public T get(){
            return item.get();
        }
        @FunctionalInterface
        public interface Delegate<T>{
            T get();
        }
    }

    public static class FluidComponent {
        public DelegatedOptional<Fluid> item;
        public int amount;

        public FluidComponent(DelegatedOptional<Fluid> in, int amt){
            item = in;
            amount = amt;
        }
    }
}
