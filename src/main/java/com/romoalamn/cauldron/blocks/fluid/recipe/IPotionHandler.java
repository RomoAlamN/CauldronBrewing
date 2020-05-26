package com.romoalamn.cauldron.blocks.fluid.recipe;

import com.romoalamn.cauldron.blocks.fluid.PotionType;

public interface IPotionHandler{
    void replaceFluid(PotionType other);
    CauldronUtils.FluidComponent drain(int amount, PotionAction action);
    CauldronUtils.FluidComponent fill(int amount, PotionAction action);
    CauldronUtils.FluidComponent drain(CauldronUtils.FluidComponent comp, PotionAction action);
    CauldronUtils.FluidComponent fill(CauldronUtils.FluidComponent comp, PotionAction action);

    CauldronUtils.FluidComponent empty(PotionAction action);

    CauldronUtils.FluidComponent getPotion();
    int getCapacity();
    enum PotionAction{
        SIMULATE(false), EXECUTE(true);
        boolean isExecute;
        boolean execute(){
            return isExecute;
        }
        boolean simulate(){
            return !isExecute;
        }
        PotionAction(boolean execute){
            isExecute = execute;
        }
    }
}
