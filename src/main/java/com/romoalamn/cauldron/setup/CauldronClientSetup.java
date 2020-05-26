package com.romoalamn.cauldron.setup;

import com.romoalamn.cauldron.blocks.CauldronBlocks;
import com.romoalamn.cauldron.blocks.CauldronColor;
import com.romoalamn.cauldron.item.CauldronItemPotion;
import com.romoalamn.cauldron.item.CauldronItems;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(value= Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public class CauldronClientSetup {

    private static final Logger logger = LogManager.getLogger();

    @SubscribeEvent
    public static void init(final FMLCommonSetupEvent event){

        logger.info("Init for client?");
        Minecraft.getInstance().getBlockColors().register(new CauldronColor(), CauldronBlocks.cauldronBlock);
        Minecraft.getInstance().getItemColors().register(new CauldronItemPotion.PotionColor(), CauldronItems.POTION);
    }


}
