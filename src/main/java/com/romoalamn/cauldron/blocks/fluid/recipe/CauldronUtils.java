package com.romoalamn.cauldron.blocks.fluid.recipe;

import com.romoalamn.cauldron.blocks.fluid.PotionType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraftforge.fluids.FluidAttributes;

public class CauldronUtils {
    public static CauldronBrewingRecipe defaultAmountRecipe(PotionType in, Item reagent, PotionType out) {
        return new CauldronBrewingRecipe(getFluid(in), getIngredient(reagent), getFluid(out));
    }
    public static CauldronBrewingRecipe defaultAmountRecipe(PotionType in, Tag<Item> reagent, PotionType out){
        return new CauldronBrewingRecipe(getFluid(in), getIngredient(reagent), getFluid(out));
    }

    public static CauldronBrewingRecipe defaultAmountRecipe(PotionType in, Ingredient reagent, PotionType out) {
        return new CauldronBrewingRecipe(getFluid(in), reagent, getFluid(out));
    }

    public static DelegatedOptional<Fluid> getFluidDelegate(DelegatedOptional.Delegate<Fluid> supp){
        return DelegatedOptional.of(supp);
    }
    public static FluidComponent getFluid(PotionType in){
        return new FluidComponent(in, FluidAttributes.BUCKET_VOLUME / 2);
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
        public PotionType potion;
        public int amount;

        public FluidComponent(PotionType in, int amt){
            potion = in;
            amount = amt;
        }

        public FluidComponent copy(){
            return new FluidComponent(potion, amount);
        }
        public boolean isEmpty(){
            return this == EMPTY || amount <=0;
        }
        public static FluidComponent EMPTY = new FluidComponent(PotionType.EMPTY, 0);
    }
}
