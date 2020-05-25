package com.romoalamn.amf.setup;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * IDK what I'm doing here.
 */
@Mod.EventBusSubscriber(value= Dist.DEDICATED_SERVER, bus=Mod.EventBusSubscriber.Bus.MOD)
public class AMFServerSetup {
    private static final Logger logger = LogManager.getLogger();
    @SubscribeEvent
    public static void initServer(final FMLCommonSetupEvent event){
        logger.info("Hello from initServer");
    }

}
