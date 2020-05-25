package com.romoalamn.amf;

import com.romoalamn.amf.blocks.AMFBlocks;
import com.romoalamn.amf.blocks.CauldronBlock;
import com.romoalamn.amf.blocks.CauldronContainer;
import com.romoalamn.amf.blocks.CauldronTile;
import com.romoalamn.amf.blocks.fluid.AMFFluids;
import com.romoalamn.amf.blocks.fluid.PotionFluid;
import com.romoalamn.amf.blocks.fluid.PotionType;
import com.romoalamn.amf.blocks.fluid.recipe.CauldronBrewingRecipe;
import com.romoalamn.amf.blocks.fluid.recipe.CauldronUtils;
import com.romoalamn.amf.setup.AMFCommonSetup;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file

/**
 * The base class for my mod. Hooray!
 */
@Mod("amf")
public class AMFMod {
    // Directly reference a log4j logger.
    /**
     * Logs. duh
     */
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * Store our modid
     */
    public static final String MODID = "amf";

    /**
     * Setup listeners and register the fluid handler.
     */
    public AMFMod() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        AMFFluids.registerFluidCreator(FMLJavaModLoadingContext.get().getModEventBus());

        AMFFluids.registerPot(new PotionType(Potions.REGENERATION, 30, PotionUtils.getPotionColor(Potions.REGENERATION))
                .setRegistryName("amf:regen_pot_normal"));
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code

    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        /**
         * Create a new registry
         *
         * @param registryEvent
         */
        @SubscribeEvent
        public static void onRegistryRegistry(final RegistryEvent.NewRegistry registryEvent) {
            LOGGER.info("Creating Potion Registry");
            new RegistryBuilder<CauldronBrewingRecipe>().add(
                    (IForgeRegistry.AddCallback<CauldronBrewingRecipe>) (owner, stage, id, obj, oldObj) -> {
                        LOGGER.info("Registry Add Called");
                        AMFFluids.registerBrewingRecipe(obj);
                    }
            )
                    .setName(new ResourceLocation("amf", "set_brewing_recipes"))
                    .setType(CauldronBrewingRecipe.class)
                    .create();

            new RegistryBuilder<PotionType>().add(
                    (IForgeRegistry.AddCallback<PotionType>) (owner, stage, id, obj, oldObj) -> {
                        LOGGER.info("Registering pot from type: {}", obj);
//                        AMFFluids.registerPot(obj);
                    }
            ).allowModification()
                    .setName(new ResourceLocation("amf", "fluid_register"))
                    .setType(PotionType.class)
                    .create();
        }

        /**
         * Register blocks
         *
         * @param blockRegistryEvent
         */
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("Registering AMF Blocks");
            blockRegistryEvent.getRegistry().register(new CauldronBlock().setRegistryName("cauldron"));

        }

        /**
         * Register items
         *
         * @param itemRegistryEvent
         */
        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
            // register a new block here
            LOGGER.info("Registering AMF Items");
            Item.Properties properties = new Item.Properties()
                    .group(AMFCommonSetup.itemGroup)
                    .rarity(Rarity.EPIC);
            itemRegistryEvent.getRegistry().register(new BlockItem(AMFBlocks.cauldronBlock, properties)
                    .setRegistryName("cauldron"));

//            itemRegistryEvent.getRegistry().register(new FirstItem().setRegistryName("firstitem"));

        }

        /**
         * Register tile entities
         *
         * @param tileRegistryEvent
         */
        @SubscribeEvent
        public static void onTileRegistry(final RegistryEvent.Register<TileEntityType<?>> tileRegistryEvent) {

            tileRegistryEvent.getRegistry().register(TileEntityType.Builder.create(CauldronTile::new, AMFBlocks.cauldronBlock).build(null).setRegistryName("cauldron"));
        }

        /**
         * REgister container
         *
         * @param containerRegistryEvent
         */
        @SubscribeEvent
        public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> containerRegistryEvent) {
            containerRegistryEvent.getRegistry().register(IForgeContainerType.create((
                    (windowId, inv, data) -> {
                        BlockPos pos = data.readBlockPos();

                        return new CauldronContainer(windowId, Minecraft.getInstance().world, pos, inv);
                    }))
                    .setRegistryName("cauldron"));
        }

        @SubscribeEvent
        public static void onBrewingRecipe(final RegistryEvent.Register<CauldronBrewingRecipe> cauldronRegister) {
            //first we register some potions
            LOGGER.info("Creating Potion Recipes");
            PotionFluid regenPot = AMFFluids.getPotionFluid("amf:regen_pot_normal");
            LOGGER.info("The Source Block is: {}", regenPot.getSourcePotion().get());
            CauldronUtils.DelegatedOptional<Fluid> f = CauldronUtils.DelegatedOptional.of(() -> Fluids.WATER);
            cauldronRegister.getRegistry().register(
                    CauldronUtils.defaultAmountRecipe(f,
                            Tags.Items.STONE,
                            regenPot.getSourcePotion())
                            .setRegistryName(new ResourceLocation("amf", "regen_recipe"))
            );
        }
        @SubscribeEvent
        public static void onPotionRegistry(final RegistryEvent.Register<PotionType> potionFluid){
        }

    }
}
