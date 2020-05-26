package com.romoalamn.cauldron.blocks.fluid.recipe;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fluids.FluidAttributes;

import javax.annotation.Nullable;

public class CauldronCapabilities {
    @CapabilityInject(IPotionHandler.class)
    public static Capability<IPotionHandler> POTION_HANDLER_CAPABILITY = null;
    public static void register(){
        CapabilityManager.INSTANCE.register(IPotionHandler.class, new DefaultPotionHandlerStorage<>(), ()-> new PotionHandler(FluidAttributes.BUCKET_VOLUME));
    }
    private static class DefaultPotionHandlerStorage<T extends IPotionHandler> implements Capability.IStorage<T> {
        /**
         * Serialize the capability instance to a NBTTag.
         * This allows for a central implementation of saving the data.
         * <p>
         * It is important to note that it is up to the API defining
         * the capability what requirements the 'instance' value must have.
         * <p>
         * Due to the possibility of manipulating internal data, some
         * implementations MAY require that the 'instance' be an instance
         * of the 'default' implementation.
         * <p>
         * Review the API docs for more info.
         *
         * @param capability The Capability being stored.
         * @param instance   An instance of that capabilities interface.
         * @param side       The side of the object the instance is associated with.
         * @return a NBT holding the data. Null if no data needs to be stored.
         */
        @Nullable
        @Override
        public INBT writeNBT(Capability<T> capability, T instance, Direction side) {
            if(!(instance instanceof PotionHandler))
                throw new RuntimeException("Derp.");
            CompoundNBT nbt = new CompoundNBT();
            PotionHandler potHandler = (PotionHandler) instance;
            potHandler.writeToNBT(nbt);
            return nbt;
        }

        /**
         * Read the capability instance from a NBT tag.
         * <p>
         * This allows for a central implementation of saving the data.
         * <p>
         * It is important to note that it is up to the API defining
         * the capability what requirements the 'instance' value must have.
         * <p>
         * Due to the possibility of manipulating internal data, some
         * implementations MAY require that the 'instance' be an instance
         * of the 'default' implementation.
         * <p>
         * Review the API docs for more info.         *
         *
         * @param capability The Capability being stored.
         * @param instance   An instance of that capabilities interface.
         * @param side       The side of the object the instance is associated with.
         * @param nbt        A NBT holding the data. Must not be null, as doesn't make sense to call this function with nothing to read...
         */
        @Override
        public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {
            if(!(instance instanceof PotionHandler)){
                throw new RuntimeException("Whaddup, IPotionHandler or otherwise");
            }
            CompoundNBT tags = (CompoundNBT) nbt;
            PotionHandler handler = (PotionHandler) instance;
            handler.readFromNBT(tags);
        }
    }
}
