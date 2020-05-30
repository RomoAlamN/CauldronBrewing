package com.romoalamn.cauldron.setup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.romoalamn.cauldron.blocks.fluid.CauldronUtils;
import com.romoalamn.cauldron.blocks.fluid.FluidComponent;
import com.romoalamn.cauldron.blocks.fluid.PotionType;
import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronBrewingRecipe;
import com.romoalamn.cauldron.blocks.fluid.recipe.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.EffectInstance;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
        potTypeHandler.clear();
        potTypeHandler.put("cauldron:potion", (obj) -> {
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
                CauldronUtils.registerPot(new PotionType(inst).setRegistryName(resourceName));
            }
        });
        potTypeHandler.put("cauldron:potion_list", (obj) -> {
            if (obj.has("potions")) {
                JsonArray pots = obj.getAsJsonArray("potions");
                for (JsonElement elem : pots) {
                    potTypeHandler.get("cauldron:potion").addPotion(elem.getAsJsonObject());
                }
            } else {
                logger.warn("PotionType definition of type \"potion_list\" does not define member \"potions\"");
            }
        });

        recipeHandler.clear();
        recipeHandler.put("cauldron:brewing", (obj) -> {
            try {
                CauldronUtils.registerBrewingRecipe(createBrewingFromPotionType(obj));
            } catch (IllegalArgumentException iae) {
                logger.error("Error adding brewing recipe: {}", iae.getMessage());
            }
        });
        recipeHandler.put("cauldron:brew_many", CauldronServerSetup::addRecipeMany);
        recipeHandler.put("cauldron:brew_corruption", (obj) -> {
            if (obj.has("recipe")) {
                JsonObject recipe = obj.getAsJsonObject("recipe");
                JsonObject reagent = new JsonObject();
                reagent.addProperty("item", Objects.requireNonNull(Items.FERMENTED_SPIDER_EYE.getRegistryName()).toString());
                recipe.add("reagent", reagent);
                // create a recipe tree using the default brew_many, using s fermented spider eye for a base.
                recipeHandler.get("cauldron:brew_many").addRecipe(obj);

                // now we create the two corruptions
                if (recipe.has("output")) {
                    JsonObject output = recipe.getAsJsonObject("output");
                    if (recipe.has("long") && output.has("long")) {
                        JsonObject in = recipe.getAsJsonObject("long");
                        JsonObject out = output.getAsJsonObject("long");
                        declareRecipeForType(in, out);
                    }
                    if (recipe.has("strong") && output.has("strong")) {
                        JsonObject in = recipe.getAsJsonObject("strong");
                        JsonObject out = output.getAsJsonObject("strong");
                        declareRecipeForType(in, out);
                    }
                }
            }
        });
        server.getResourceManager().addReloadListener(
                ((stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
                    resourceManager.getAllResourceLocations("potions", s -> s.endsWith(".json")).forEach(CauldronServerSetup::createPotion);
                    resourceManager.getAllResourceLocations("brewing", s -> s.endsWith(".json")).forEach(CauldronServerSetup::createBrewingRecipe);

                    return CompletableFuture.runAsync(() -> {
                    }).thenCompose(stage::markCompleteAwaitingOthers);
                }
                ));
    }

    private static void declareRecipeForType(JsonObject in, JsonObject out) {
        FluidComponent compIn = getFluidComponent(in);
        FluidComponent compOut = getFluidComponent(out);
        CauldronUtils.registerBrewingRecipe(
                new CauldronBrewingRecipe(
                        compIn,
                        CauldronUtils.getIngredient(Items.FERMENTED_SPIDER_EYE),
                        compOut
                ).setRegistryName(compOut.potion.getRegistryName() + "_" + id++)
        );
    }

    @SuppressWarnings("DuplicatedCode")
    private static void addRecipeMany(JsonObject obj) {
        if (obj.has("recipe")) {
            JsonObject recipe = obj.getAsJsonObject("recipe");
            // the same basics as before.
            FluidComponent input = CauldronUtils.getFluid(PotionTypes.awkward);
            Ingredient reagent;
            if (recipe.has("base")) {
                JsonObject base = recipe.getAsJsonObject("base");
                input = getFluidComponent(base);
            } else {
                logger.warn("Potion recipe has no \"base\" element.");
            }
            if (recipe.has("reagent")) {
                JsonObject jIngredient = recipe.getAsJsonObject("reagent");
                reagent = getReagent(jIngredient);
            } else {
                throw new IllegalArgumentException("No ingredient is specified");
            }
            if (recipe.has("output")) {
                JsonObject output = recipe.getAsJsonObject("output");
                FluidComponent baseOut;
                if (output.has("base")) {
                    JsonObject base = output.getAsJsonObject("base");
                    int amount = input.amount;
                    if (base.has("amount")) {
                        amount = base.get("amount").getAsInt();
                    }
                    baseOut = getFluidComponent(base);
                    CauldronUtils.registerBrewingRecipe(
                            new CauldronBrewingRecipe(input, reagent, baseOut).setRegistryName(baseOut.potion.getRegistryName() + "_" + id++)
                    );
                } else {
                    throw new IllegalArgumentException("No base potion defined, this recipe is invalid");
                }

                if (output.has("long")) {
                    JsonObject lon = output.getAsJsonObject("long");
                    FluidComponent longType = getFluidComponent(lon);
                    CauldronUtils.registerBrewingRecipe(
                            new CauldronBrewingRecipe(
                                    baseOut,
                                    CauldronUtils.getIngredient(Tags.Items.DUSTS_REDSTONE),
                                    longType
                            ).setRegistryName(longType.potion.getRegistryName() + "_" + id++)
                    );
                }
                if (output.has("strong")) {
                    JsonObject strong = output.getAsJsonObject("strong");
                    FluidComponent strongType = getFluidComponent(strong);
                    CauldronUtils.registerBrewingRecipe(
                            new CauldronBrewingRecipe(
                                    baseOut,
                                    CauldronUtils.getIngredient(Tags.Items.DUSTS_GLOWSTONE),
                                    strongType
                            ).setRegistryName(strongType.potion.getRegistryName() + "_" + id++)
                    );
                }
            } else {
                throw new IllegalArgumentException("No output means no recipe.");
            }
        }
    }

    interface PotionTypeAddingHandler {
        void addPotion(JsonObject obj);
    }

    interface BrewingRecipeAddingHandler {
        void addRecipe(JsonObject obj);
    }

    private static final HashMap<String, PotionTypeAddingHandler> potTypeHandler = Maps.newHashMap();
    private static final HashMap<String, BrewingRecipeAddingHandler> recipeHandler = Maps.newHashMap();

    private static void createPotion(ResourceLocation location) {
//        logger.info(location.getPath());
        try {
            InputStream in = manager.getResource(location).getInputStream();
            logger.debug("Parsing file: {}", location);
            JsonObject obj = parser.parse(new InputStreamReader(in)).getAsJsonObject();
            if (conformsToExpectations(obj)) {
                String type = obj.get("type").getAsString();
                if (potTypeHandler.containsKey(type)) {
                    potTypeHandler.get(type).addPotion(obj);
                } else {
                    logger.error("Unrecognized PotionType definition type {}", type);
                }
            }
        } catch (IOException ex) {
            logger.info("path did not exist");
        }
    }

    private static boolean conformsToExpectations(@Nonnull JsonObject obj) {
        if (obj.has("requires")) {
            String requires = obj.get("requires").getAsString();
            if (!ModList.get().isLoaded(requires)) {
                logger.debug("PotionType definition requires mod {}, but it is not loaded: skipping.", requires);
                return false;
            }
        }
        return obj.has("type");
    }

    private static void createBrewingRecipe(ResourceLocation location) {
//        logger.info(location.getPath());
        try {
            InputStream in = manager.getResource(location).getInputStream();
            logger.debug("Parsing file: {}", location);
            JsonObject obj = parser.parse(new InputStreamReader(in)).getAsJsonObject();
            if (conformsToExpectations(obj)) {
                String type = obj.get("type").getAsString();
                if (recipeHandler.containsKey(type)) {
                    recipeHandler.get(type).addRecipe(obj);
                } else {
                    logger.error("Recipe type not recognized: {}", type);
                }
            }
        } catch (IOException io) {
            logger.warn("path did not exist");
        }
    }

    private static int id = 0;

    private static FluidComponent getFluidComponent(JsonObject location) {
        PotionType type = PotionTypes.awkward;
        if (location.has("potion_type")) {
            type = CauldronUtils.getPotion(location.get("potion_type").getAsString());
        }
        if (location.has("amount")) {
            return new FluidComponent(type, location.get("amount").getAsInt());
        } else {
            return CauldronUtils.getFluid(type);
        }
    }

    private static Ingredient getReagent(JsonObject location) {
        if (location.has("item")) {
            IForgeRegistry<Item> items = RegistryManager.ACTIVE.getRegistry(Item.class);
            ResourceLocation loc = new ResourceLocation(location.get("item").getAsString());
            if (items.containsKey(loc)) {
                return CauldronUtils.getIngredient(
                        items.getValue(loc)
                );
            } else {
                throw new IllegalArgumentException("Improper item name: " + location.get("item").getAsString());
            }
        } else if (location.has("tag")) {
            Tag<Item> tag = ItemTags.getCollection().get(new ResourceLocation(location.get("tag").getAsString()));
            return CauldronUtils.getIngredient(tag);
        } else {
            throw new IllegalArgumentException("Invalid reagent");
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private static CauldronBrewingRecipe createBrewingFromPotionType(JsonObject obj) {
        FluidComponent input = CauldronUtils.getFluid(PotionTypes.awkward);
        FluidComponent output = FluidComponent.EMPTY;
        Ingredient ingredient = null;
        if (obj.has("base")) {
            JsonObject base = obj.getAsJsonObject("base");
            input = getFluidComponent(base);
        } else {
            logger.warn("Potion recipe has no \"base\" element.");
        }
        if (obj.has("reagent")) {
            JsonObject jIngredient = obj.getAsJsonObject("reagent");
            ingredient = getReagent(jIngredient);
        } else {
            throw new IllegalArgumentException("No ingredient is specified");
        }
        if (obj.has("result")) {
            JsonObject res = obj.getAsJsonObject("result");
            PotionType type = PotionType.EMPTY;
            if (res.has("potion_type")) {
                type = CauldronUtils.getPotion(res.get("potion_type").getAsString());
                if (type == PotionType.EMPTY) {
                    logger.warn("Recipe resulted in an empty potion. This may be on purpose, but is probably not. ");
                }
            }
            if (res.has("amount")) {
                output = new FluidComponent(type, res.get("amount").getAsInt());
            } else {
                output = CauldronUtils.getFluid(type);
            }
        } else {
            logger.warn("Potion recipe has no \"result\" element.");
        }
        return new CauldronBrewingRecipe(input, ingredient, output).setRegistryName(output.potion.getRegistryName() + "_" + id++);
    }
}
