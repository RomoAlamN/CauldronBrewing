package com.romoalamn.cauldron.blocks.fluid.recipe;

import net.minecraft.util.IStringSerializable;

public enum RecipeStates implements IStringSerializable {
    HEATING, COOLING, NORMAL;

    @Override
    public String getName() {
        switch (this){
            case NORMAL:
                return "normal";
            case COOLING:
                return "cooling";
            case HEATING:
                return "heating";
        }
        return "INVALID";
    }
}
