package com.romoalamn.cauldron.blocks.fluid.recipe;

import com.romoalamn.cauldron.blocks.fluid.FluidComponent;
import com.romoalamn.cauldron.blocks.fluid.PotionType;

public interface IPotionHandler{
    void replaceFluid(PotionType other);
    FluidComponent drain(int amount, PotionAction action);
    FluidComponent fill(int amount, PotionAction action);
    FluidComponent drain(FluidComponent comp, PotionAction action);
    FluidComponent fill(FluidComponent comp, PotionAction action);

    FluidComponent empty(PotionAction action);

    FluidComponent getPotion();
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
