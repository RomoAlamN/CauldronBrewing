package com.romoalamn.cauldron.setup;

import com.romoalamn.cauldron.blocks.CauldronBlocks;
import com.romoalamn.cauldron.blocks.CauldronColor;
import com.romoalamn.cauldron.blocks.fluid.CauldronUtils;
import com.romoalamn.cauldron.blocks.fluid.PotionType;
import com.romoalamn.cauldron.enchantments.CauldronEnchantments;
import com.romoalamn.cauldron.item.CauldronItemPotion;
import com.romoalamn.cauldron.item.CauldronItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CauldronClientSetup {

    private static final Logger logger = LogManager.getLogger();

    @SubscribeEvent
    public static void init(final FMLCommonSetupEvent event) {
        logger.info("Cauldron Mod Client initialization ...");
        Minecraft.getInstance().getBlockColors().register(new CauldronColor(), CauldronBlocks.cauldronBlock);
        Minecraft.getInstance().getItemColors().register(new CauldronItemPotion.PotionColor(), CauldronItems.POTION);

    }

    @SubscribeEvent
    public static void tooltipEvent(ItemTooltipEvent tooltipEvent) {
        ItemStack stack = tooltipEvent.getItemStack();
        List<ITextComponent> tooltip = tooltipEvent.getToolTip();
        if (EnchantmentHelper.getEnchantments(stack).containsKey(CauldronEnchantments.POTION_ENCHANTMENT)) {
            try {
                PotionType type = CauldronUtils.getPotionFromStack(stack);
                CompoundNBT nbt = stack.getTag();
                CompoundNBT pot = nbt.getCompound("potion_effect");
                String i18nStr = I18n.format("cauldron.desc." + type.getRegistryName().getPath());
                tooltip.add(new StringTextComponent(TextFormatting.LIGHT_PURPLE + i18nStr));
                tooltip.add(new StringTextComponent(TextFormatting.GRAY + String.format("%s/%s", pot.getInt("uses"), pot.getInt("max_uses"))));
            } catch (Exception e) {
                logger.warn("Failed to get item tooltip:", e);
            }
        }
    }


}
