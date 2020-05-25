package com.romoalamn.amf.blocks;

import com.romoalamn.amf.blocks.fluid.AMFFluids;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CauldronTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {

    private static final Logger logger = LogManager.getLogger();

    private LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler);
    private LazyOptional<IFluidHandler> fluids = LazyOptional.of(this::createFluidHandler);

    public CauldronTile() {
        super(AMFBlocks.cauldronBlockTile);
    }

    @Override
    public void tick() {
    }

    @Override
    public void read(CompoundNBT tag) {
        CompoundNBT invTag = tag.getCompound("inv");
        handler.ifPresent(h -> {
            ((INBTSerializable<CompoundNBT>) h).deserializeNBT(invTag);
        });
        CompoundNBT fluidTag = tag.getCompound("fluid");
        fluids.ifPresent(h -> {
            ((FluidTank)h).readFromNBT(fluidTag);
        });
        super.read(tag);
    }

    @Override
    @MethodsReturnNonnullByDefault
    public CompoundNBT write(CompoundNBT tag) {
        handler.ifPresent(h -> {
            CompoundNBT compound = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            tag.put("inv", compound);
        });
        fluids.ifPresent(h-> {

            ((FluidTank)h).writeToNBT(tag);
        });
        return super.write(tag);
    }

    private IItemHandler createHandler() {
        return new ItemStackHandler() {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() == Items.DIAMOND;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (!isItemValid(slot, stack)) {
                    return stack;
                }
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    private IFluidHandler createFluidHandler() {
        return new FluidTank(1000){
            public boolean isFluidStateValid(@Nonnull FluidStack stack) {
                if (isFluidValid(0, stack)) {
                    // is the tank empty?
                    if (fluid.isEmpty()) {
                        return true;

                    }
                    // is it a valid fluid
                    if (AMFFluids.isPotion(stack) | stack.getFluid() == Fluids.WATER) {
                        return fluid.isFluidEqual(stack);
                    }
                }
                return false;

            }

            @Override
            public boolean isFluidValid(@Nonnull FluidStack stack) {
                return (AMFFluids.isPotion(stack) | stack.getFluid() == Fluids.WATER);
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }else if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluids.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(getType().getRegistryName().getPath());
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new CauldronContainer(id, world, pos, playerInventory);
    }


}
