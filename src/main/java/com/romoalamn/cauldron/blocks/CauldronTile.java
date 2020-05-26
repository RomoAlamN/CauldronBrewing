package com.romoalamn.cauldron.blocks;

import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronCapabilities;
import com.romoalamn.cauldron.blocks.fluid.recipe.IPotionHandler;
import com.romoalamn.cauldron.blocks.fluid.recipe.PotionHandler;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class CauldronTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {

    private static final Logger logger = LogManager.getLogger();

    private final LazyOptional<IItemHandler> handler = LazyOptional.of(this::createHandler);
    private final LazyOptional<IPotionHandler> potions = LazyOptional.of(this::createPotionHandler);


    public CauldronTile() {
        super(CauldronBlocks.cauldronBlockTile);
//        EntityDataManager.createKey(getClass())

    }

    @Override
    public void tick() {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(CompoundNBT tag) {
        CompoundNBT invTag = tag.getCompound("inv");
        handler.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(invTag));
        CompoundNBT fluidTag = tag.getCompound("potion");
        potions.ifPresent(h -> ((PotionHandler)h).readFromNBT(fluidTag));
        super.read(tag);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    @Override
    @MethodsReturnNonnullByDefault
    public CompoundNBT write(@Nonnull CompoundNBT tag) {
        handler.ifPresent(h -> {
            CompoundNBT compound = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            tag.put("inv", compound);
        });
        potions.ifPresent(h-> {
            CompoundNBT t = new CompoundNBT();
            ((PotionHandler)h).writeToNBT(t);
            tag.put("potion", t);
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
    private IPotionHandler createPotionHandler(){
        return new PotionHandler(FluidAttributes.BUCKET_VOLUME);
    }


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }else if(cap == CauldronCapabilities.POTION_HANDLER_CAPABILITY) {
            return potions.cast();
        }
        return super.getCapability(cap, side);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(Objects.requireNonNull(getType().getRegistryName()).getPath());
    }

    @Nullable
    @Override
    public Container createMenu(int id, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity playerEntity) {
        return new CauldronContainer(id, Objects.requireNonNull(world), pos, playerInventory);
    }

    /**
     * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
     * many blocks change at once. This compound comes back to you clientside in
     */
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT base = new CompoundNBT();
        base.put("basic", super.getUpdateTag());
        base.put("cauldron", write(new CompoundNBT()));
        return base;
    }

    /**
     * Called when the chunk's TE update tag, gotten from {@link #getUpdateTag()}, is received on the client.
     * <p>
     * Used to handle this tag in a special way. By default this simply calls
     *
     * @param tag The
     */
    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        super.handleUpdateTag(tag);
        this.read((CompoundNBT) tag.get("caul"));
        logger.info("Received Update Packet from server! {!}");
    }

    /**
     * Retrieves packet to send to the client whenever this Tile Entity is re-synced via World.notifyBlockUpdate. For
     * modded TE's, this packet comes back to you clientside in {@link #onDataPacket}
     */
    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), 0, getUpdateTag());
    }

    /**
     * Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible for
     * sending the packet.
     *
     * @param net The NetworkManager the packet originated from
     * @param pkt The data packet
     */
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        super.onDataPacket(net, new SUpdateTileEntityPacket(pkt.getPos(), 0,(CompoundNBT)pkt.getNbtCompound().get("base")));
        CompoundNBT caul =  (CompoundNBT) pkt.getNbtCompound().get("cauldron");
        this.read(caul);
        logger.info("Received Update Packet from server! {!}");
    }
}
