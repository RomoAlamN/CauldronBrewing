package com.romoalamn.amf.blocks.fluid;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.List;

public class PotionType extends ForgeRegistryEntry<PotionType> {
    /**
     * The potion effect represented
     */

    List<EffectInstance> parent;
    /**
     * The color of the fluid to use
     */
    int tint;

    int duration;

    /**
     * Creates the parent and tint fields
     * @param pot mapped to parrent field
     * @param color mapped to tint field
     * @see #tint
     * @see  #parent
     */
    public PotionType(Potion pot, int duration, int color){
        parent = pot.getEffects();
        tint = color;
        this.duration = duration;
    }

    public boolean resourceMatches(String modid, String path){
        return modid.equals(getRegistryName().getNamespace()) && path.equals(getRegistryName().getPath());
    }
}
