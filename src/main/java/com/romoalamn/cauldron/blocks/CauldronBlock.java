package com.romoalamn.cauldron.blocks;

import com.romoalamn.cauldron.blocks.fluid.CauldronFluids;
import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronBrewingRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.BooleanProperty;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Optional;

public class CauldronBlock extends net.minecraft.block.CauldronBlock {
    private static final Logger LOGGER = LogManager.getLogger();

    public CauldronBlock() {
        super(Properties.create(Material.IRON)
                .sound(SoundType.METAL)
                .hardnessAndResistance(2.0f)
                .lightValue(14));
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CauldronTile();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    public static final BooleanProperty SHOULD_UPDATE = BooleanProperty.create("should_update");

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BlockStateProperties.FACING, SHOULD_UPDATE);
    }

    /**
     * On click, you ho
     *
     * @param state The state of the CauldronBlock being clicked
     * @param worldIn The world object of the interaction
     * @param pos The coordinates of the block
     * @param player The player that does the interaction
     * @param handIn The hand the player used in order to interact with the block
     * @param hit Where the interaction 'hit'
     * @return The state of the action after it happened
     */
    @Nonnull
    @Override
    public ActionResultType func_225533_a_(@Nonnull BlockState state, World worldIn, @Nonnull BlockPos pos, PlayerEntity player, @Nonnull Hand handIn, @Nonnull BlockRayTraceResult hit) {
        // get the tile entity at our position
        TileEntity ent = worldIn.getTileEntity(pos);
        ItemStack heldItem = player.getHeldItem(handIn);
        if(ent == null){
            return ActionResultType.FAIL;
        }

        //TODO: Finish this, ho. (Cauldron recipe parsing)
        LazyOptional<IFluidHandler> hOpt = ent.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
        if (hOpt.isPresent()) {
            // the or else will never exist
            IFluidHandler h = hOpt.orElse(new FluidTank(1000) );
            //region recipes
//            LOGGER.info("Are we crafting a potion? {}", CauldronFluids.isPotion(h.getFluidInTank(0)) | h.getFluidInTank(0).getFluid() == Fluids.WATER);
//            LOGGER.info("What liquid is in the cauldron? {}", h.getFluidInTank(0).getFluid());
            if ((CauldronFluids.isPotion(h.getFluidInTank(0)) || h.getFluidInTank(0).getFluid() == Fluids.WATER)) {
                // check if
//                LOGGER.info("Are we holding a potion ingredient for the fluid?{}", CauldronFluids.isPotionIngredient(heldItem, h.getFluidInTank(0)));
                if (CauldronFluids.isPotionIngredient(heldItem, h.getFluidInTank(0))) {
                    Optional<CauldronBrewingRecipe> recipeOpt = CauldronFluids.getRecipe(h.getFluidInTank(0), heldItem);
                    if (!recipeOpt.isPresent()) {
//                        LOGGER.info("Somehow, I got my wires crossed (The above check turned out to be false)");
                        return ActionResultType.FAIL;
                    }
                    CauldronBrewingRecipe recipe = recipeOpt.get();

                    int outputAmount = h.getFluidInTank(0).getAmount() / (recipe.getInput().amount);
                    LOGGER.info("Turns out we can make about {} iterations of the recipe", outputAmount);
                    if (outputAmount >= heldItem.getCount()) {
                        LOGGER.info("But we don't have enough items in our hand. (We have {})", heldItem.getCount());
                        return ActionResultType.FAIL;
                    } else {
                        if (!worldIn.isRemote) {
                            FluidStack replacement = new FluidStack(recipe.getOutput().item.get(), 0);
                            int drain = 0;
                            while (drain < h.getFluidInTank(0).getAmount()) {
                                heldItem.shrink(1);
                                drain += recipe.getInput().amount;
                                FluidStack out = recipe.getOutput(h.getFluidInTank(0), heldItem);
                                replacement.setAmount(replacement.getAmount() + out.getAmount());
                            }
                            h.drain(h.getFluidInTank(0).getAmount() + 1000, IFluidHandler.FluidAction.EXECUTE);
                            h.fill(replacement, IFluidHandler.FluidAction.EXECUTE);
                            //TODO: Fix this pile of hot garbage
                            createWorldUpdate(state, worldIn, pos, h);
                            createWorldUpdate(state, worldIn, pos, h);
                            createWorldUpdate(state, worldIn, pos, h);
                        }
                        return ActionResultType.SUCCESS;
                    }
                }
            }

            //endregion
            //region filling and emptying
            if (heldItem.getItem() == Items.GLASS_BOTTLE) {
                if (h.getFluidInTank(0).getAmount() >= 250 && !worldIn.isRemote) {
                    heldItem.setCount(heldItem.getCount() - 1);
                    ItemStack pot = CauldronFluids.liquidToPotion(h.drain(250, IFluidHandler.FluidAction.EXECUTE));

                    if (!player.addItemStackToInventory(pot)) {
                        player.dropItem(pot, false);
                    }
                    createWorldUpdate(state, worldIn, pos, h);
                }
                return ActionResultType.SUCCESS;
            } else if (heldItem.getItem() == Items.WATER_BUCKET) {
                if (h.getFluidInTank(0).isEmpty() && !worldIn.isRemote) {
                    h.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                        player.dropItem(new ItemStack(Items.BUCKET), false);
                    }
                    createWorldUpdate(state, worldIn, pos, h);
                    worldIn.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                return ActionResultType.SUCCESS;
            } else if (heldItem.getItem() == Items.BUCKET) {
                if (h.getFluidInTank(0).getFluid() == Fluids.WATER && h.getFluidInTank(0).getAmount() == 1000 && !worldIn.isRemote) {
                    h.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                        if (heldItem.isEmpty()) {
                            player.setHeldItem(handIn, new ItemStack(Items.WATER_BUCKET));
                        } else {
                            player.dropItem(new ItemStack(Items.WATER_BUCKET), false);
                        }
                    }
                    createWorldUpdate(state, worldIn,pos,h);
                    worldIn.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                return ActionResultType.SUCCESS;
            }
            //endregion

        }

        return ActionResultType.PASS;
    }

    private void createWorldUpdate(BlockState state, World worldIn, BlockPos pos, IFluidHandler h) {

        BlockState newState =state.with(LEVEL, MathHelper.clamp((4 * h.getFluidInTank(0).getAmount()) / (h.getTankCapacity(0)), 0, 3))
                .cycle(SHOULD_UPDATE);
        worldIn.setBlockState(pos, newState);
        worldIn.notifyBlockUpdate(pos, state, newState, 3);
    }
}



