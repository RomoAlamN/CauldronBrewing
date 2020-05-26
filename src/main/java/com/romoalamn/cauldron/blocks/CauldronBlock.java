package com.romoalamn.cauldron.blocks;

import com.romoalamn.cauldron.blocks.fluid.CauldronFluids;
import com.romoalamn.cauldron.blocks.fluid.recipe.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;

import javax.annotation.Nonnull;
import java.util.Optional;

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

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BlockStateProperties.FACING, LEVEL_NEW);
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
    public ActionResultType func_225533_a_(@Nonnull BlockState state, World worldIn, @Nonnull BlockPos pos, PlayerEntity player, @Nonnull Hand handIn, @Nonnull BlockRayTraceResult hit) {
        // get the tile entity at our position
        TileEntity ent = worldIn.getTileEntity(pos);
        ItemStack heldItem = player.getHeldItem(handIn);
        if (ent == null) {
            return ActionResultType.FAIL;
        }

        //TODO: Finish this, ho. (Cauldron recipe parsing)
        LazyOptional<IPotionHandler> hOpt = ent.getCapability(CauldronCapabilities.POTION_HANDLER_CAPABILITY);
        LOGGER.info("Capability {} present", hOpt.isPresent() ? "is": "is not");
        if (hOpt.isPresent()) {
            // the or else will never exist
            IPotionHandler h = hOpt.orElse(new PotionHandler(FluidAttributes.BUCKET_VOLUME));
            //region recipes
            LOGGER.info("What liquid is in the cauldron? {}", h.getPotion().potion);
            // check if
            LOGGER.info("Are we holding a potion ingredient for the fluid?{}", CauldronFluids.isPotionIngredient(heldItem,h.getPotion().potion));
            if (CauldronFluids.isPotionIngredient(heldItem, h.getPotion().potion)) {
                Optional<CauldronBrewingRecipe> recipeOpt = CauldronFluids.getRecipe(h.getPotion(), heldItem);
                if (!recipeOpt.isPresent()) {
//                        LOGGER.info("Somehow, I got my wires crossed (The above check turned out to be false)");
                    return ActionResultType.FAIL;
                }
                CauldronBrewingRecipe recipe = recipeOpt.get();

                int outputAmount = h.getPotion().amount / (recipe.getInput().amount);
                LOGGER.info("Turns out we can make about {} iterations of the recipe", outputAmount);
                if (outputAmount >= heldItem.getCount()) {
                    LOGGER.info("But we don't have enough items in our hand. (We have {})", heldItem.getCount());
                    return ActionResultType.FAIL;
                } else {
                    if (!worldIn.isRemote) {
                        heldItem.shrink(outputAmount);
                        h.replaceFluid(recipe.getOutput().potion);

                        createWorldUpdate(state, worldIn, pos, h);
                        ent.markDirty();
                    }
                    return ActionResultType.SUCCESS;
                }

            }

            //endregion
            //region filling and emptying
            if (heldItem.getItem() == Items.GLASS_BOTTLE) {
                if (h.getPotion().amount >= FluidAttributes.BUCKET_VOLUME / 4 && !worldIn.isRemote) {
                    heldItem.setCount(heldItem.getCount() - 1);
                    ItemStack pot = CauldronFluids.liquidToPotion(h.drain(250, IPotionHandler.PotionAction.EXECUTE));

                    if (!player.addItemStackToInventory(pot)) {
                        player.dropItem(pot, false);
                    }
                    createWorldUpdate(state, worldIn, pos, h);
                }
                return ActionResultType.SUCCESS;
            } else if (heldItem.getItem() == Items.WATER_BUCKET) {
                if (h.getPotion().isEmpty() && !worldIn.isRemote) {
                    h.fill(new CauldronUtils.FluidComponent(CauldronPotionTypes.WATER, 1000), IPotionHandler.PotionAction.EXECUTE);
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                        player.dropItem(new ItemStack(Items.BUCKET), false);
                    }
                    createWorldUpdate(state, worldIn, pos, h);
                    worldIn.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                return ActionResultType.SUCCESS;
            } else if (heldItem.getItem() == Items.BUCKET) {
                if (h.getPotion().potion == CauldronPotionTypes.WATER && h.getPotion().amount == 1000 && !worldIn.isRemote) {
                    h.empty(IPotionHandler.PotionAction.EXECUTE);
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                        if (heldItem.isEmpty()) {
                            player.setHeldItem(handIn, new ItemStack(Items.WATER_BUCKET));
                        } else {
                            player.dropItem(new ItemStack(Items.WATER_BUCKET), false);
                        }
                    }
                    createWorldUpdate(state, worldIn, pos, h);
                    worldIn.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                return ActionResultType.SUCCESS;
            }
            //endregion

        }

        return ActionResultType.PASS;
    }

    private void createWorldUpdate(BlockState state, World worldIn, BlockPos pos, IPotionHandler h) {

        BlockState newState = state.with(LEVEL_NEW, MathHelper.clamp((4 * h.getPotion().amount) / (h.getCapacity()), 0, 4));
        worldIn.setBlockState(pos, newState);
        worldIn.notifyBlockUpdate(pos, newState, newState, 3);
    }
}



