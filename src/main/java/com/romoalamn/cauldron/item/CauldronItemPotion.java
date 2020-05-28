package com.romoalamn.cauldron.item;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
@ParametersAreNonnullByDefault
public class CauldronItemPotion extends PotionItem {
    public CauldronItemPotion(Properties builder) {
        super(builder);

    }

    /**
     * Returns true if this item has an enchantment glint. By default, this returns <code>stack.isItemEnchanted()</code>,
     * but other items can override it (for instance, written books always return true).
     * <p>
     * Note that if you override this method, you generally want to also call the super version (on {@link net.minecraft.item.Item}) to get
     * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
     *
     * @param stack the itemstack
     */
    @Override
    public boolean hasEffect(ItemStack stack) {
        return !PotionUtils.getEffectsFromStack(stack).isEmpty() | super.hasEffect(stack);
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     *
     * @param pot The stack containing the potion
     */
    @Nonnull
    @Override
    public String getTranslationKey(@Nonnull ItemStack pot) {
        List<EffectInstance> potionEffects = PotionUtils.getEffectsFromStack(pot);
        if(potionEffects.isEmpty()){
            PotionUtils.addPotionToItemStack(pot, Potions.AWKWARD);
            return super.getTranslationKey(pot);
        }else{
            if(potionEffects.size() ==2){
                boolean foundResistance = false;
                boolean foundSlowness = false;
                for(EffectInstance inst : potionEffects){
                    if(inst.getPotion() == Effects.RESISTANCE){
                        foundResistance = true;
                    }else if(inst.getPotion() == Effects.SLOWNESS){
                        foundSlowness=true;
                    }
                }
                if(foundResistance && foundSlowness){
                    return "cauldron.potion.of.effect.turtle";
                }else{
                    return "cauldron.potion.of." + potionEffects.get(0).getEffectName();
                }
            }else{
                return "cauldron.potion.of." + potionEffects.get(0).getEffectName();
            }
        }
    }

    public static class PotionColor implements IItemColor{
    
        @Override
        public int getColor(@Nonnull ItemStack item, int tintIndex) {
            if(tintIndex == 1){
                return 0xFFFFFFFF;
            }
            return PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromStack(item));
        }
    }
}
