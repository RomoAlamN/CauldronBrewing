package com.romoalamn.amf.setup;

import com.romoalamn.amf.blocks.AMFBlocks;
import com.romoalamn.amf.blocks.CauldronColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(value= Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public class AMFClientSetup {

    private static final Logger logger = LogManager.getLogger();

    @SubscribeEvent
    public static void init(final FMLCommonSetupEvent event){

        logger.info("Init for client?");
        Minecraft.getInstance().getBlockColors().register(new CauldronColor(), AMFBlocks.cauldronBlock);
    }


}
