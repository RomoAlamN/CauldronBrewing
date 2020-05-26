package com.romoalamn.cauldron.setup;

import com.romoalamn.cauldron.CauldronMod;
import com.romoalamn.cauldron.blocks.CauldronBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates an ItemGroup, and nothing else
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CauldronCommonSetup {
    private static final Logger logger = LogManager.getLogger();

    public static ItemGroup itemGroup = new ItemGroup(CauldronMod.MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(CauldronBlocks.cauldronBlock);
        }
    };

    @SubscribeEvent
    public static void initCommon(final FMLCommonSetupEvent event) {
    }

}
