package com.romoalamn.cauldron.enchantments;

import com.romoalamn.cauldron.blocks.fluid.CauldronUtils;
import com.romoalamn.cauldron.blocks.fluid.PotionType;
import com.romoalamn.cauldron.setup.Config;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class PotionEnchantment extends Enchantment {
    private static final Logger logger = LogManager.getLogger("PotionEnchantment");

    public PotionEnchantment(EquipmentSlotType... slots) {
        super(Rarity.COMMON, EnchantmentType.ALL, slots);
    }

    /**
     * Determines if this enchantment can be applied to a specific ItemStack.
     *
     * @param stack
     */
    @Override
    public boolean canApply(ItemStack stack) {
        return true;
    }

    /**
     * This applies specifically to applying at the enchanting table. The other method {@link #canApply(ItemStack)}
     * applies for <i>all possible</i> enchantments.
     *
     * @param stack
     * @return
     */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    /**
     * Determines if the enchantment passed can be applyied together with this enchantment.
     *
     * @param ench
     */
    @Override
    protected boolean canApplyTogether(Enchantment ench) {
        return false;
    }

    private void removeEnchantmentFrom(ItemStack item) {
        //remove the enchantment
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(item);
        map.remove(this);
        EnchantmentHelper.setEnchantments(map, item);
    }

    /**
     * Called whenever a mob is damaged with an item that has this enchantment on it.
     *
     * @param user
     * @param target
     * @param level
     */
    @Override
    public void onEntityDamaged(LivingEntity user, Entity target, int level) {
        if (!(target instanceof LivingEntity)) {
            return;
        }
        LivingEntity victim = (LivingEntity) target;
        ItemStack item = user.getHeldItem(user.getActiveHand());
        CompoundNBT nbt = item.getTag();
        if (nbt == null) {
            return;
        }
        if (nbt.contains("potion_effect")) {
            CompoundNBT pot_eff = nbt.getCompound("potion_effect");
            if (pot_eff.contains("id") && pot_eff.contains("uses") && pot_eff.contains("max_uses")) {
                String id = pot_eff.getString("id");
                int uses = pot_eff.getInt("uses");
                int max_uses = pot_eff.getInt("max_uses");

                PotionType toApply = CauldronUtils.getPotion(id);

                for (EffectInstance eff : toApply.getEffects()) {
                    if (eff.getPotion().isInstant()) {
                        if (!Config.COMMON_CONFIG.ignoreInstants.get()) {
                            eff.getPotion().affectEntity(user, user, victim, eff.getAmplifier(), 1.0 / max_uses);
                        }
                    } else {
                        eff = new EffectInstance(eff.getPotion(), (int)(eff.getDuration() * 5) / max_uses, eff.getDuration());
                        victim.addPotionEffect(eff);
                    }
                }

                // tick down the uses left.
                if (uses <= 0) {
                    removeEnchantmentFrom(item);
                }
                if(!user.getEntityWorld().isRemote) {
                    pot_eff.putInt("uses", uses - 1);
                }

            } else {
                logger.warn("No potion id found on weapon.");
                removeEnchantmentFrom(item);
            }
        } else {
            logger.warn("No potion effect exists for weapon.");
            removeEnchantmentFrom(item);
        }
        super.onEntityDamaged(user, target, level);
    }

    /**
     * Is this enchantment allowed to be enchanted on books via Enchantment Table
     *
     * @return false to disable the vanilla feature
     */
    @Override
    public boolean isAllowedOnBooks() {
        return false;
    }
}
