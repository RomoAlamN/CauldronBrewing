package com.romoalamn.amf.blocks;

import com.romoalamn.amf.blocks.fluid.AMFFluids;
import com.romoalamn.amf.blocks.fluid.recipe.CauldronBrewingRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BlockStateProperties.FACING);
    }

    /**
     * On click, you ho
     *
     * @param state
     * @param worldIn
     * @param pos
     * @param player
     * @param handIn
     * @param hit
     * @return
     */
    @Override
    public ActionResultType func_225533_a_(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        // get the tile entity at our position
        TileEntity ent = worldIn.getTileEntity(pos);
        ItemStack heldItem = player.getHeldItem(handIn);

        //TODO: Finish this, ho. (Cauldron recipe parsing)
        LazyOptional<IFluidHandler> hOpt = ent.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
        if (!hOpt.isPresent()) {

        } else {
            // the or else will never exist
            IFluidHandler h = hOpt.orElse(new IFluidHandler() {
                @Override
                public int getTanks() {
                    return 0;
                }

                @Override
                public FluidStack getFluidInTank(int tank) {
                    return null;
                }

                @Override
                public int getTankCapacity(int tank) {
                    return 0;
                }

                @Override
                public boolean isFluidValid(int tank, FluidStack stack) {
                    return false;
                }

                @Override
                public int fill(FluidStack resource, FluidAction action) {
                    return 0;
                }

                @Override
                public FluidStack drain(FluidStack resource, FluidAction action) {
                    return null;
                }

                @Override
                public FluidStack drain(int maxDrain, FluidAction action) {
                    return null;
                }
            });
            try {
                //region recipes
                LOGGER.info("Are we crafting a potion? {}", AMFFluids.isPotion(h.getFluidInTank(0)) | h.getFluidInTank(0).getFluid() == Fluids.WATER);
                LOGGER.info("What liquid is in the cauldron? {}", h.getFluidInTank(0).getFluid());
                if ((AMFFluids.isPotion(h.getFluidInTank(0)) || h.getFluidInTank(0).getFluid() == Fluids.WATER)) {
                    // check if
                    LOGGER.info("Are we holding a potion ingredient for the fluid?{}", AMFFluids.isPotionIngredient(heldItem, h.getFluidInTank(0)));
                    if (AMFFluids.isPotionIngredient(heldItem, h.getFluidInTank(0))) {
                        Optional<CauldronBrewingRecipe> recipeOpt = AMFFluids.getRecipe(h.getFluidInTank(0), heldItem);
                        if (!recipeOpt.isPresent()) {
                            LOGGER.info("Somehow, I got my wires crossed (The above check turned out to be false)");
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
                                int amount = h.fill(replacement, IFluidHandler.FluidAction.EXECUTE);
                            }
                            return ActionResultType.SUCCESS;
                        }
                    }
                }

                //endregion
                //region filling and emptying
                if (heldItem.getItem() == Items.GLASS_BOTTLE) {
                    if (h.getFluidInTank(0).getAmount() >= 250 && !worldIn.isRemote) {
                        LOGGER.info("Attempting to fill a glass bottle");
                        heldItem.setCount(heldItem.getCount() - 1);
                        ItemStack pot = AMFFluids.liquidToPotion(h.drain(250, IFluidHandler.FluidAction.EXECUTE));
                        if (!player.addItemStackToInventory(pot)) {
                            player.dropItem(pot, false);
                        }
                    }
                    return ActionResultType.SUCCESS;
                } else if (heldItem.getItem() == Items.WATER_BUCKET) {
                    if (h.getFluidInTank(0).isEmpty() && !worldIn.isRemote) {
                        h.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
                        if (!player.isCreative()) {
                            heldItem.shrink(1);
                            player.dropItem(new ItemStack(Items.BUCKET), false);
                        }
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
                        worldIn.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    }
                    return ActionResultType.SUCCESS;
                }
                //endregion
            } finally {
                //update block based on new info.
                worldIn.setBlockState(pos, state.with(LEVEL, MathHelper.clamp((4 * h.getFluidInTank(0).getAmount()) / (h.getTankCapacity(0)), 0, 3)), 2);
            }
        }

        return ActionResultType.PASS;
    }
}



