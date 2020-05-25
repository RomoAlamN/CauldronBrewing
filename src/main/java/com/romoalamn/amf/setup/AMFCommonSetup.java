package com.romoalamn.amf.setup;

import com.romoalamn.amf.blocks.AMFBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates an ItemGroup, and nothing else
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AMFCommonSetup {
    private static final Logger logger = LogManager.getLogger();

    public static ItemGroup itemGroup = new ItemGroup("amf") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(AMFBlocks.cauldronBlock);
        }
    };

    @SubscribeEvent
    public static void initCommon(final FMLCommonSetupEvent event) {
        logger.info("Hello from initCommon");
    }

}