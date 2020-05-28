package com.romoalamn.cauldron.setup;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.romoalamn.cauldron.blocks.fluid.CauldronFluids;
import com.romoalamn.cauldron.blocks.fluid.PotionType;
import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronBrewingRecipe;
import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronUtils;
import com.romoalamn.cauldron.blocks.fluid.recipe.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.EffectInstance;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This sets up the datapack listeners on the integrated and the dedicated servers
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CauldronServerSetup {
    private static final Logger logger = LogManager.getLogger();

    @SubscribeEvent
    public static void initServerClient(final FMLServerAboutToStartEvent event) {
        commonSetup(event.getServer());
    }

    private static IReloadableResourceManager manager = null;

    private static final JsonParser parser = new JsonParser();

    private static void commonSetup(MinecraftServer server) {
        manager = server.getResourceManager();
        server.getResourceManager().addReloadListener(
                ((stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
                    resourceManager.getAllResourceLocations("potions", s -> s.endsWith(".json")).forEach(CauldronServerSetup::createPotion);
                    resourceManager.getAllResourceLocations("brewing", s -> s.endsWith(".json")).forEach(CauldronServerSetup::createBrewingRecipe);

                    return CompletableFuture.runAsync(() -> {
                    }).thenCompose(stage::markCompleteAwaitingOthers);
                }
                ));
    }

    private static void createPotion(ResourceLocation location) {
//        logger.info(location.getPath());
        try {
            InputStream in = manager.getResource(location).getInputStream();

            JsonObject obj = parser.parse(new InputStreamReader(in)).getAsJsonObject();
            if (obj.has("name") && obj.has("effects")) {
                String resourceName = obj.get("name").getAsString();
                JsonArray effects = obj.get("effects").getAsJsonArray();
                List<EffectInstance> inst = Lists.newArrayList();
                for (JsonElement effect : effects) {
                    try {
                        inst.add(CauldronUtils.getEffectFromJson(effect));
                    } catch (IllegalArgumentException except) {
                        logger.warn(except);
                    }
                }
                CauldronFluids.registerPot(new PotionType(inst).setRegistryName(resourceName));
            }
        } catch (IOException ex) {
            logger.info("path did not exist");
        }
    }

    private static void createBrewingRecipe(ResourceLocation location) {
//        logger.info(location.getPath());
        try {
            InputStream in = manager.getResource(location).getInputStream();
            JsonObject obj = parser.parse(new InputStreamReader(in)).getAsJsonObject();
            if (obj.has("type")) {
                String type = obj.get("type").getAsString();
                if (type.equals("cauldron:brewing")) {
                    try {
                        CauldronFluids.registerBrewingRecipe(createBrewingFromPotionType(obj));
                    } catch (IllegalArgumentException iae) {
                        logger.warn(iae);
                    }
                }
            }
        } catch (IOException io) {
            logger.warn("path did not exist");
        }
    }
    private static int id = 0;

    private static CauldronBrewingRecipe createBrewingFromPotionType(JsonObject obj) {
        CauldronUtils.FluidComponent input = CauldronUtils.getFluid(PotionTypes.awkward);
        CauldronUtils.FluidComponent output = CauldronUtils.FluidComponent.EMPTY;
        Ingredient ingredient = null;
        if (obj.has("base")) {
            JsonObject base = obj.getAsJsonObject("base");
            PotionType type = PotionTypes.awkward;
            int amount = 0;
            if (base.has("potion_type")) {
                type = CauldronFluids.getPotion(base.get("potion_type").getAsString());
            }
            if (base.has("amount")) {
                input = new CauldronUtils.FluidComponent(type, base.get("amount").getAsInt());
            } else {
                input = CauldronUtils.getFluid(type);
            }
        } else {
            logger.warn("Potion recipe has no \"base\" element.");
        }
        if (obj.has("reagent")) {
            JsonObject jIngredient = obj.getAsJsonObject("reagent");
            int amount = 1;
            if (jIngredient.has("item")) {
                IForgeRegistry<Item> items = RegistryManager.ACTIVE.getRegistry(Item.class);
                ResourceLocation loc = new ResourceLocation(jIngredient.get("item").getAsString());
                if (items.containsKey(loc)) {
                    ingredient = CauldronUtils.getIngredient(
                            items.getValue(loc)
                    );
                }else{
                    throw new IllegalArgumentException("Improper item name: " + jIngredient.get("item").getAsString());
                }
            } else if (jIngredient.has("tag")) {
                Tag<Item> tag = ItemTags.getCollection().get(new ResourceLocation(jIngredient.get("tag").getAsString()));
                ingredient = CauldronUtils.getIngredient(tag);
            }
        }else {
            throw new IllegalArgumentException("No ingredient is specified");
        }
        if(obj.has("result")){
            JsonObject res = obj.getAsJsonObject("result");
            PotionType type = PotionType.EMPTY;
            if(res.has("potion_type")){
                type = CauldronFluids.getPotion(res.get("potion_type").getAsString());
                if(type == PotionType.EMPTY){
                    logger.warn("Recipe resulted in an empty potion. This may be on purpose, but is probably not. ");
                }
            }
            if(res.has("amount")){
                output = new CauldronUtils.FluidComponent(type, res.get("amount").getAsInt());
            }else{
                output = CauldronUtils.getFluid(type);
            }
        } else {
            logger.warn("Potion recipe has no \"result\" element.");
        }
        return new CauldronBrewingRecipe(input, ingredient, output).setRegistryName(output.potion.getRegistryName() + "_" + id++);
    }
}
