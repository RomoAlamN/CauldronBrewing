package com.romoalamn.cauldron.blocks.fluid;

public class FluidComponent {
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