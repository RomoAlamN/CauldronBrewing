package com.romoalamn.cauldron.blocks.fluid;

import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class PotionType extends ForgeRegistryEntry<PotionType> implements Comparable<PotionType>{
    public static PotionType EMPTY = new PotionType(Potions.EMPTY).setRegistryName("empty_potion");
    /**
     * The potion effect represented
     */

    List<EffectInstance> parent;
    /**
     * The color of the fluid to use
     */
    int tint;

    /**
     * Creates the parent and tint fields
     * @param pot mapped to parent field
     * @see  #parent
     */
    public PotionType(Potion pot){
        parent = pot.getEffects();
        tint = PotionUtils.getPotionColor(pot);
    }
    public PotionType(List<EffectInstance> pot){
        parent = pot;
        tint = PotionUtils.getPotionColorFromEffectList(pot);
    }
    public List<EffectInstance> getEffects(){
        return parent;
    }
    public boolean resourceMatches(String modid, String path){
        return modid.equals(Objects.requireNonNull(getRegistryName()).getNamespace()) && path.equals(getRegistryName().getPath());
    }

    @Override
    public int compareTo(@Nonnull PotionType potionType) {
        try {
            return Objects.requireNonNull(potionType.getRegistryName()).getPath().compareTo(Objects.requireNonNull(getRegistryName()).getPath());
        }catch (NullPointerException npe){
            return 0;
        }
    }
}
