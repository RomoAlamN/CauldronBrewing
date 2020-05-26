package com.romoalamn.cauldron;

import com.romoalamn.cauldron.blocks.CauldronBlock;
import com.romoalamn.cauldron.blocks.CauldronBlocks;
import com.romoalamn.cauldron.blocks.CauldronContainer;
import com.romoalamn.cauldron.blocks.CauldronTile;
import com.romoalamn.cauldron.blocks.fluid.CauldronFluids;
import com.romoalamn.cauldron.blocks.fluid.PotionType;
import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronBrewingRecipe;
import com.romoalamn.cauldron.item.CauldronItemPotion;
import com.romoalamn.cauldron.setup.CauldronCommonSetup;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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

import java.util.Objects;

// The value here should match an entry in the META-INF/mods.toml file

/**
 * The base class for my mod. Hooray!
 */
@Mod(CauldronMod.MODID)
public class CauldronMod {
    // Directly reference a log4j logger.
    /**
     * Logs. duh
     */
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * Store our modid
     */
    public static final String MODID = "cauldron";

    /**
     * Setup listeners and register the fluid handler.
     */
    public CauldronMod() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        CauldronFluids.registerFluidCreator(FMLJavaModLoadingContext.get().getModEventBus());
        createFluids();
    }

    private void createFluids() {

        CauldronFluids.registerPot(new PotionType(Potions.SWIFTNESS)
                .setRegistryName(CauldronMod.MODID, "swiftness"));
        CauldronFluids.registerPot(new PotionType(Potions.LONG_SWIFTNESS)
                .setRegistryName(CauldronMod.MODID , "long_swiftness"));
        CauldronFluids.registerPot(new PotionType(Potions.STRONG_SWIFTNESS)
                .setRegistryName(CauldronMod.MODID, "strong_swiftness"));
        CauldronFluids.registerPot(new PotionType(Potions.LEAPING)
                .setRegistryName(CauldronMod.MODID , "leaping"));
        CauldronFluids.registerPot(new PotionType(Potions.LONG_LEAPING)
                .setRegistryName(CauldronMod.MODID, "long_leaping"));
        CauldronFluids.registerPot(new PotionType(Potions.STRONG_LEAPING)
                .setRegistryName(CauldronMod.MODID , "strong_leaping"));
        CauldronFluids.registerPot(new PotionType(Potions.HEALING)
                .setRegistryName(CauldronMod.MODID, "healing"));
        CauldronFluids.registerPot(new PotionType(Potions.STRONG_HEALING)
                .setRegistryName(CauldronMod.MODID , "strong_healing"));
        CauldronFluids.registerPot(new PotionType(Potions.POISON)
                .setRegistryName(CauldronMod.MODID, "poison"));
        CauldronFluids.registerPot(new PotionType(Potions.LONG_POISON)
                .setRegistryName(CauldronMod.MODID , "long_poison"));
        CauldronFluids.registerPot(new PotionType(Potions.STRONG_POISON)
                .setRegistryName(CauldronMod.MODID, "strong_poison"));
        CauldronFluids.registerPot(new PotionType(Potions.WATER_BREATHING)
                .setRegistryName(CauldronMod.MODID , "water_breathing"));
        CauldronFluids.registerPot(new PotionType(Potions.LONG_WATER_BREATHING)
                .setRegistryName(CauldronMod.MODID, "long_water_breathing"));
        CauldronFluids.registerPot(new PotionType(Potions.FIRE_RESISTANCE)
                .setRegistryName(CauldronMod.MODID , "fire_resistance"));
        CauldronFluids.registerPot(new PotionType(Potions.LONG_FIRE_RESISTANCE)
                .setRegistryName(CauldronMod.MODID, "long_fire_resistance"));
        CauldronFluids.registerPot(new PotionType(Potions.NIGHT_VISION)
                .setRegistryName(CauldronMod.MODID , "night_vision"));
        CauldronFluids.registerPot(new PotionType(Potions.LONG_NIGHT_VISION)
                .setRegistryName(CauldronMod.MODID, "long_night_vision"));
        CauldronFluids.registerPot(new PotionType(Potions.STRENGTH)
                .setRegistryName(CauldronMod.MODID , "strength"));
        CauldronFluids.registerPot(new PotionType(Potions.LONG_STRENGTH)
                .setRegistryName(CauldronMod.MODID, "long_strength"));
        CauldronFluids.registerPot(new PotionType(Potions.STRONG_STRENGTH)
                .setRegistryName(CauldronMod.MODID , "strong_strength"));
        CauldronFluids.registerPot(new PotionType(Potions.REGENERATION)
                .setRegistryName(CauldronMod.MODID , "regeneration"));
        CauldronFluids.registerPot(new PotionType(Potions.LONG_REGENERATION)
                .setRegistryName(CauldronMod.MODID, "long_regeneration"));
        CauldronFluids.registerPot(new PotionType(Potions.STRONG_REGENERATION)
                .setRegistryName(CauldronMod.MODID , "strong_regeneration"));
        CauldronFluids.registerPot(new PotionType(Potions.TURTLE_MASTER)
                .setRegistryName(CauldronMod.MODID, "turtle_master"));
        CauldronFluids.registerPot(new PotionType(Potions.LONG_TURTLE_MASTER)
                .setRegistryName(CauldronMod.MODID , "long_turtle_master"));
        CauldronFluids.registerPot(new PotionType(Potions.STRONG_TURTLE_MASTER)
                .setRegistryName(CauldronMod.MODID, "strong_turtle_master"));
        CauldronFluids.registerPot(new PotionType(Potions.SLOW_FALLING)
                .setRegistryName(CauldronMod.MODID , "slow_falling"));
        CauldronFluids.registerPot(new PotionType(Potions.LONG_SLOW_FALLING)
                .setRegistryName(CauldronMod.MODID, "long_slow_falling"));
        CauldronFluids.registerPot(new PotionType(Potions.SLOWNESS)
                .setRegistryName(CauldronMod.MODID , "slowness"));
        CauldronFluids.registerPot(new PotionType(Potions.LONG_SLOWNESS)
                .setRegistryName(CauldronMod.MODID, "long_slowness"));
        CauldronFluids.registerPot(new PotionType(Potions.STRONG_SLOWNESS)
                .setRegistryName(CauldronMod.MODID, "strong_slowness"));
        CauldronFluids.registerPot(new PotionType(Potions.HARMING)
                .setRegistryName(CauldronMod.MODID, "harming"));
        CauldronFluids.registerPot(new PotionType(Potions.STRONG_HARMING)
                .setRegistryName(CauldronMod.MODID, "strong_harming"));
        CauldronFluids.registerPot(new PotionType(Potions.INVISIBILITY)
                .setRegistryName(CauldronMod.MODID, "invisibility"));
        CauldronFluids.registerPot(new PotionType(Potions.LONG_INVISIBILITY)
                .setRegistryName(CauldronMod.MODID, "long_invisibility"));
        CauldronFluids.registerPot(new PotionType(Potions.WEAKNESS)
                .setRegistryName(CauldronMod.MODID, "weakness"));
        CauldronFluids.registerPot(new PotionType(Potions.LONG_WEAKNESS)
                .setRegistryName(CauldronMod.MODID, "long_weakness"));
        CauldronFluids.registerPot(new PotionType(Potions.AWKWARD)
        .setRegistryName(CauldronMod.MODID, "awkward"));


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
         * @param registryEvent the event we use to register our new registry
         */
        @SubscribeEvent
        public static void onRegistryRegistry(final RegistryEvent.NewRegistry registryEvent) {
            LOGGER.info("Creating Potion Registry");
            new RegistryBuilder<CauldronBrewingRecipe>().add(
                    (IForgeRegistry.AddCallback<CauldronBrewingRecipe>) (owner, stage, id, obj, oldObj) -> {
                        LOGGER.info("Registry Add Called");
                        CauldronFluids.registerBrewingRecipe(obj);
                    }
            )
                    .setName(new ResourceLocation("cau", "set_brewing_recipes"))
                    .setType(CauldronBrewingRecipe.class)
                    .create();

        }

        /**
         * Register blocks
         *
         * @param blockRegistryEvent The event we use to register blocks
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
         * @param itemRegistryEvent The event we use to register items
         */
        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
            // register a new block here
            LOGGER.info("Registering AMF Items");
            Item.Properties properties = new Item.Properties()
                    .group(CauldronCommonSetup.itemGroup)
                    .rarity(Rarity.COMMON);
            itemRegistryEvent.getRegistry().register(new BlockItem(CauldronBlocks.cauldronBlock, properties)
                    .setRegistryName("cauldron"));
            itemRegistryEvent.getRegistry().register(new CauldronItemPotion(
                    new Item.Properties()
                    .maxStackSize(1)
                    .group(CauldronCommonSetup.itemGroup)
            ).setRegistryName(CauldronMod.MODID, "potion"));

//            itemRegistryEvent.getRegistry().register(new FirstItem().setRegistryName("firstitem"));

        }

        /**
         * Register tile entities
         *
         * @param tileRegistryEvent The event we use to register Tile Entities
         */
        @SubscribeEvent
        public static void onTileRegistry(final RegistryEvent.Register<TileEntityType<?>> tileRegistryEvent) {

            //noinspection ConstantConditions
            tileRegistryEvent.getRegistry().register(TileEntityType.Builder.create(CauldronTile::new, CauldronBlocks.cauldronBlock).build(null).setRegistryName("cauldron"));
        }

        /**
         * Register container
         *
         * @param containerRegistryEvent The event we use to register Containers
         */
        @SubscribeEvent
        public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> containerRegistryEvent) {
            containerRegistryEvent.getRegistry().register(IForgeContainerType.create((
                    (windowId, inv, data) -> {
                        BlockPos pos = data.readBlockPos();

                        return new CauldronContainer(windowId, Objects.requireNonNull(Minecraft.getInstance().world), pos, inv);
                    }))
                    .setRegistryName("cauldron"));
        }

        @SubscribeEvent
        public static void onBrewingRecipe(final RegistryEvent.Register<CauldronBrewingRecipe> cauldronRegister) {
            //first we register some potions
            LOGGER.info("Creating Potion Recipes");
            IForgeRegistry<CauldronBrewingRecipe> registry = cauldronRegister.getRegistry();

            RecipeCreator creator = new RecipeCreator();
            creator.registerPotions(registry);
        }

    }
}