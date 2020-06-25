package com.romoalamn.cauldron.blocks;

import com.romoalamn.cauldron.blocks.fluid.CauldronUtils;
import com.romoalamn.cauldron.blocks.fluid.FluidComponent;
import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronBrewingRecipe;
import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronCapabilities;
import com.romoalamn.cauldron.blocks.fluid.recipe.PotionTypes;
import com.romoalamn.cauldron.blocks.fluid.recipe.RecipeStates;
import com.romoalamn.cauldron.blocks.fluid.recipe.potionhandler.IPotionHandler;
import com.romoalamn.cauldron.blocks.fluid.recipe.potionhandler.PotionHandler;
import com.romoalamn.cauldron.enchantments.CauldronEnchantments;
import com.romoalamn.cauldron.setup.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@ParametersAreNonnullByDefault
public class CauldronBlock extends net.minecraft.block.CauldronBlock {
//    private static final Logger LOGGER = LogManager.getLogger();

    public CauldronBlock() {
        super(Properties.create(Material.IRON)
                .sound(SoundType.METAL)
                .hardnessAndResistance(2.0f)
                .lightValue(14));
        setDefaultState(this.getStateContainer().getBaseState().with(LEVEL_NEW, 0));
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CauldronTile();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    public static final IntegerProperty LEVEL_NEW = IntegerProperty.create("cauldron_level", 0, 4);
    public static final BooleanProperty UPDATE = BooleanProperty.create("update");
    public static final EnumProperty<RecipeStates> RECIPE_STATE = EnumProperty.create("recipe_state", RecipeStates.class);

    public static final Block[] heatingBlocks = new Block[]{
            Blocks.FIRE, Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE, Blocks.LAVA
    };
    public static final Block[] coolingBlocks = new Block[]{
            Blocks.BLUE_ICE, Blocks.FROSTED_ICE, Blocks.PACKED_ICE, Blocks.SNOW_BLOCK, Blocks.SOUL_SAND
    };

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BlockStateProperties.FACING, LEVEL_NEW, UPDATE, RECIPE_STATE);
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {

    }

    /**
     * On click, you ho
     *
     * @param state   The state of the CauldronBlock being clicked
     * @param worldIn The world object of the interaction
     * @param pos     The coordinates of the block
     * @param player  The player that does the interaction
     * @param handIn  The hand the player used in order to interact with the block
     * @param hit     Where the interaction 'hit'
     * @return The state of the action after it happened
     */
    @Nonnull
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        // get the tile entity at our position
        TileEntity ent = worldIn.getTileEntity(pos);
        ItemStack heldItem = player.getHeldItem(handIn);
        if (ent == null) {
            return ActionResultType.FAIL;
        }

        LazyOptional<IPotionHandler> hOpt = ent.getCapability(CauldronCapabilities.POTION_HANDLER_CAPABILITY);
        if (hOpt.isPresent()) {
            // the or else will never exist
            IPotionHandler h = hOpt.orElse(new PotionHandler(FluidAttributes.BUCKET_VOLUME));
            if (CauldronUtils.isPotionIngredient(heldItem, h.getPotion().potion)) {
                Optional<CauldronBrewingRecipe> recipeOpt = CauldronUtils.getRecipe(h.getPotion(), heldItem);
                if (!recipeOpt.isPresent()) {
                    return ActionResultType.FAIL;
                }

                CauldronBrewingRecipe recipe = recipeOpt.get();

                int outputAmount = h.getPotion().amount / (recipe.getInput().amount);
                if (outputAmount > heldItem.getCount()) {
                    LOGGER.info("But we don't have enough items in our hand. (We have {})", heldItem.getCount());
                    return ActionResultType.FAIL;
                } else {
                    if (Config.COMMON_CONFIG.followStateRecipe.get()) {
                        // check if we have a valid block.
                        RecipeStates recipeState = recipe.getState();
                        switch (recipeState) {
                            case HEATING:
                                if (!checkForHeatingBlock(worldIn, pos.add(0, -1, 0))) {
                                    return ActionResultType.FAIL;
                                }
                                break;
                            case COOLING:
                                if (!checkForCoolingBlock(worldIn, pos.add(0, -1, 0))) {
                                    return ActionResultType.FAIL;
                                }
                                break;
                            default:
                        }
                    }
                    if (!worldIn.isRemote) {
                        //this stuff needs to be done on the server
                        heldItem.shrink(outputAmount);
                        h.replaceFluid(recipe.getOutput().potion);

                        ent.markDirty();
                        createWorldUpdate(state, worldIn, pos, h);
                    } else {
                        // this can only be done on the client
                        double px = pos.getX() + 0.5;
                        double py = pos.getY() + 1.2;
                        double pz = pos.getZ() + 0.5;

                        worldIn.playSound(player, pos, SoundEvents.ENTITY_BOAT_PADDLE_WATER, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        for (int i = 0; i < 10; i++) {
                            Random rand = worldIn.getRandom();
                            double rx = px + rand.nextDouble() - 0.5;
                            double ry = py + rand.nextDouble() * 0.3 - 0.15;
                            double rz = pz + rand.nextDouble() - 0.5;

                            double vx = rand.nextDouble() * 0.15 - .075;
                            double vy = rand.nextDouble() * 0.15 - .075;
                            double vz = rand.nextDouble() * 0.15 - .075;
                            worldIn.addParticle(ParticleTypes.LARGE_SMOKE, rx, ry, rz, vx, 0.01D + vy, vz);
                        }
                        for (int i = 0; i < 10; i++) {
                            Random rand = worldIn.getRandom();
                            double rx = px + rand.nextDouble() - 0.5;
                            double ry = py + rand.nextDouble() * 0.3 - 0.15;
                            double rz = pz + rand.nextDouble() - 0.5;

                            double vx = rand.nextDouble() * 0.3 - .15;
                            double vy = rand.nextDouble() * 0.3 - .15;
                            double vz = rand.nextDouble() * 0.3 - .15;
                            worldIn.addParticle(ParticleTypes.SMOKE, rx, ry, rz, vx, 0.01D + vy, vz);
                        }
                    }
                    return ActionResultType.SUCCESS;
                }

            }

            //endregion
            //region filling and emptying
            if (heldItem.getItem() == Items.GLASS_BOTTLE) {
                if (h.getPotion().amount >= FluidAttributes.BUCKET_VOLUME / 4 && !worldIn.isRemote) {
                    heldItem.setCount(heldItem.getCount() - 1);
                    ItemStack pot = CauldronUtils.liquidToPotion(h.drain(250, IPotionHandler.PotionAction.EXECUTE));

                    if (!player.addItemStackToInventory(pot)) {
                        player.dropItem(pot, false);
                    }
                    createWorldUpdate(state, worldIn, pos, h);
                }
                worldIn.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                return ActionResultType.SUCCESS;
            } else if (heldItem.getItem() == Items.WATER_BUCKET) {
                if (h.getPotion().isEmpty() && !worldIn.isRemote) {
                    h.fill(new FluidComponent(PotionTypes.water, 1000), IPotionHandler.PotionAction.EXECUTE);
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                        if(heldItem.isEmpty()){
                            player.setHeldItem(handIn, new ItemStack(Items.BUCKET));
                        }else{
                            player.dropItem(new ItemStack(Items.BUCKET), false);
                        }
                    }
                    createWorldUpdate(state, worldIn, pos, h);
                    worldIn.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                return ActionResultType.SUCCESS;
            } else if (heldItem.getItem() == Items.BUCKET) {
                if (h.getPotion().potion == PotionTypes.water && h.getPotion().amount == 1000 /*&& !worldIn.isRemote*/) {
                    h.empty(IPotionHandler.PotionAction.EXECUTE);
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                        if (heldItem.isEmpty()) {
                            player.setHeldItem(handIn, new ItemStack(Items.WATER_BUCKET));
                        } else {
                            player.dropItem(new ItemStack(Items.BUCKET), false);
                        }
                    }
                    createWorldUpdate(state, worldIn, pos, h);
                    worldIn.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                return ActionResultType.SUCCESS;
            } else if (heldItem.getItem() instanceof PotionItem && h.getPotion().amount < 1000){
                List<EffectInstance> eff = PotionUtils.getEffectsFromStack(heldItem);
                List<EffectInstance> inEff = h.getPotion().potion.getEffects();
                if(eff.size() == inEff.size()){
                    for(int i = 0; i < eff.size(); i++){
                        if(!eff.get(i).equals(inEff.get(i))){
                            return ActionResultType.PASS;
                        }
                    }
                    h.fill(250, IPotionHandler.PotionAction.EXECUTE);
                }
            }
            //endregion

            // if we have extra content enabled, we can enchant items with the cauldron.
            if (Config.COMMON_CONFIG.extraContent.get() && h.getPotion().amount >= 1000) {
                int maxUses = Config.COMMON_CONFIG.maxUses.get();
                if (!worldIn.isRemote) {
                    if (h.getPotion().potion.getEffects().size() > 0) {
                        if (!EnchantmentHelper.getEnchantments(heldItem).containsKey(CauldronEnchantments.POTION_ENCHANTMENT)) {
                            heldItem.addEnchantment(CauldronEnchantments.POTION_ENCHANTMENT, 1);
                            CompoundNBT nbt = heldItem.getTag();
                            CompoundNBT effectTag = new CompoundNBT();
                            effectTag.putString("id", h.getPotion().potion.getRegistryName().toString());
                            int level = 0;
                            for (EffectInstance eff : h.getPotion().potion.getEffects()) {
                                level += 1 + eff.getAmplifier();
                            }
                            if (level < 1) level = 1;
                            effectTag.putInt("uses", maxUses / level);
                            effectTag.putInt("max_uses", maxUses / level);
                            nbt.put("potion_effect", effectTag);
                        }
                        h.drain(1000, IPotionHandler.PotionAction.EXECUTE);
                    }
                    createWorldUpdate(state, worldIn, pos, h);
                } else {
                    if (EnchantmentHelper.getEnchantments(heldItem).containsKey(CauldronEnchantments.POTION_ENCHANTMENT)) {
                        // uh -oh
                        worldIn.playSound(player, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        worldIn.playSound(player, pos, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1, 1.7f);

                        double px = pos.getX() + 0.5;
                        double py = pos.getY() + 1.2;
                        double pz = pos.getZ() + 0.5;
                        for (int i = 0; i < 20; i++) {
                            Random rand = worldIn.getRandom();
                            double rx = px + rand.nextDouble() - 0.5;
                            double ry = py + rand.nextDouble() * 0.3 - 0.15;
                            double rz = pz + rand.nextDouble() - 0.5;

                            double vx = rand.nextDouble() * 0.3 - .15;
                            double vy = rand.nextDouble() * 0.3 - .15;
                            double vz = rand.nextDouble() * 0.3 - .15;
                            worldIn.addParticle(ParticleTypes.EXPLOSION, rx, ry, rz, vx, 0.01 + vy, vz);
                        }
                    }
                }
                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.PASS;
    }

    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
     * this block
     *
     * @param worldIn
     * @param pos
     * @param state
     * @param player
     */
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        if (Config.COMMON_CONFIG.extraContent.get()) {
            TileEntity ent = worldIn.getTileEntity(pos);
            if(ent == null){
                super.onBlockHarvested(worldIn, pos, state, player); ;
                return;
            }
            ent.getCapability(CauldronCapabilities.POTION_HANDLER_CAPABILITY).ifPresent(h -> {
                if (!h.isEmpty()) {
                    makeAreaOfEffectCloud(h.getPotion(), pos, worldIn);
                }
            });
            // play sound
            worldIn.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    private void makeAreaOfEffectCloud(FluidComponent potion, BlockPos pos, IWorld world) {
        AreaEffectCloudEntity aece = new AreaEffectCloudEntity((World) world, pos.getX(), pos.getY(), pos.getZ());
        aece.setRadius(3.0f);
        aece.setRadiusOnUse(-0.5f);
        aece.setWaitTime(10);
        aece.setRadiusPerTick(-aece.getRadius() / aece.getDuration());
        for (EffectInstance effect : potion.potion.getEffects()) {
            aece.addEffect(effect.getEffectInstance());
        }
        if (!potion.potion.getEffects().isEmpty()) {
            aece.setColor(potion.potion.getEffects().get(0).getPotion().getLiquidColor());
        }
        world.addEntity(aece);
    }

    private <T> boolean contains(T[] toCheck, T search) {
        for (T t : toCheck) {
            if (t.equals(search)) return true;
        }
        return false;
    }

    public boolean checkForCoolingBlock(World worldIn, BlockPos pos) {
        BlockState state = worldIn.getBlockState(pos);
        //        LOGGER.info("Is block cooling? {}", cont ? "yes": "no");
        return contains(coolingBlocks, state.getBlock());
    }

    public boolean checkForHeatingBlock(World worldIn, BlockPos pos) {
        BlockState state = worldIn.getBlockState(pos);
        //        LOGGER.info("Is block heating? {}", cont? "yes" : "no");
//        LOGGER.info("What is it? {}", state.getBlock());
        return contains(heatingBlocks, state.getBlock());
    }

    private void createWorldUpdate(BlockState state, World worldIn, BlockPos pos, IPotionHandler h) {
        BlockState newState = state.with(LEVEL_NEW, MathHelper.clamp((4 * h.getPotion().amount) / (h.getCapacity()), 0, 4));
        worldIn.setBlockState(pos, newState);
        worldIn.notifyBlockUpdate(pos, state, state, 3);
    }

}



