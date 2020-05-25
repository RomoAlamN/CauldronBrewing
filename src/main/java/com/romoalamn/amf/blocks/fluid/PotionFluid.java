package com.romoalamn.amf.blocks.fluid;

import com.romoalamn.amf.blocks.fluid.recipe.CauldronUtils;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.Item;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

/**
 * Represents a Flowing and Still Liquid. Flowing is not really used
 */
public class PotionFluid{
    private static final Logger logger  = LogManager.getLogger();
    // unused
    /**
     * Unused, but must be defined
     */
    private FlowingFluid flowingPotion;

    // only need this one, really
    /**
     * The source block that we use to represent our potion liquid
     */
    private FlowingFluid sourcePotion;
    /**
     * The type of the potion, has some useful information, such as the color information, and the potion effect itself.
     */
    private final PotionType pType;

    /**
     * Creates a new PotionFluid Instance.
     * Warning: This does not create the representations for flowing and Still Water.
     * You must call createSource and createFlowing for their respective blocks to be loaded
     * @param pot the potiontype object
     */
    public PotionFluid(PotionType pot) {
        pType = pot;
    }

    /**
     * Gets the potion effect registered
     * @return the potion effect registered
     */
    List<EffectInstance> getPotion() {
        return pType.parent;
    }

    public PotionType getType(){
        return pType;
    }

    /**
     * Creates the flowing version of this PotionFluid. Populates the local variable, and returns it.
     * @param props the properties to use
     * @return the new flowing instance.
     */
    public CauldronUtils.DelegatedOptional<FlowingFluid> createFlowing(PotionFluid.Properties props) {
        logger.info("Creating the flowing water");
        flowingPotion = new Flowing(props);
        return CauldronUtils.DelegatedOptional.of(this::getFlowingLazy);
    }

    /**
     * @see PotionFluid#createFlowing(Properties)
     * @param props the properties to use
     * @return the new still instance
     */
    public CauldronUtils.DelegatedOptional<FlowingFluid> createSource(PotionFluid.Properties props) {
        logger.info("Creating source");
        sourcePotion = new Source(props);
        return CauldronUtils.DelegatedOptional.of(this::getSourceLazy);
    }

    public CauldronUtils.DelegatedOptional<FlowingFluid> getFlowingPotion(){
        logger.info("Retrieving the flowing version");
        return CauldronUtils.DelegatedOptional.of(this::getFlowingLazy);
    }

    public CauldronUtils.DelegatedOptional<Fluid> getSourcePotion(){
        logger.info("Retrieving Source potion");
        return CauldronUtils.DelegatedOptional.of(this::getSourceLazy);
    }

    /**
     * Retrieves the source block variation for this Potion
     * @return the source block
     */
    @Nonnull
    private FlowingFluid getSourceLazy() {
        logger.info("Retrieve delegate called");
        return sourcePotion;
    }

    /**
     * Retrieves the fluid's flowing variation
     * @return the flowing block
     */
    @Nonnull
    private FlowingFluid getFlowingLazy() {
        logger.info("Retrieve delegate called for flowing");
        return flowingPotion;
    }


    /**
     * Private, not meant to be used. Used only to create EMPTY fluid
     */
    private void setSource(FlowingFluid fluid){
        sourcePotion = fluid;
    }
    /**
     * Private, do not use. Only for EMPTy
     */
    private void setFlowing(FlowingFluid fluid){
        flowingPotion = fluid;
    }
    /**
     * A Source block
     */
    public class Source extends ForgeFlowingFluid.Source {
        /**
         * Creates the source fluid
         * @param properties the properties to use
         */
        public Source(PotionFluid.Properties properties) {
            super(properties);
        }

        /**
         * Properly creates fluid levels. Even though we don't need them . *sigh*
         * @param builder used to build the state container
         * @see ForgeFlowingFluid.Source#fillStateContainer(StateContainer.Builder)
         */
        @Override
        protected void fillStateContainer(StateContainer.Builder<Fluid, IFluidState> builder) {
            builder.add(IntegerProperty.create("level", 1, 8));
            super.fillStateContainer(builder);
        }
    }

    /**
     * Flowing Potion block
     */
    public class Flowing extends ForgeFlowingFluid.Flowing {
        /**
         * Creates the flowing block with the appropriate properties
         * @param properties the appropriate properties
         */
        public Flowing(PotionFluid.Properties properties) {
            super(properties);
        }
    }

    /**
     * Used mostly in case I need it in the future. All the methods are the same, with the exception of the (redundant)
     * parent attribute.
     */
    public static class Properties extends ForgeFlowingFluid.Properties {
        /**
         * @deprecated
         */
        PotionFluid parent;

        /**
         * Creates a default properties using the default constructor of The parent class (see see also section)
         * @param still The still texture to use
         * @param flowing the flowing texture to use
         * @param attributes some fluid attributes
         * @see ForgeFlowingFluid.Properties#Properties(Supplier, Supplier, FluidAttributes.Builder)
         */
        public Properties(Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing, FluidAttributes.Builder attributes) {
            super(still, flowing, attributes);
        }

        @Override
        public PotionFluid.Properties canMultiply() {
            return (PotionFluid.Properties) super.canMultiply();
        }

        @Override
        public PotionFluid.Properties bucket(Supplier<? extends Item> bucket) {
            return (PotionFluid.Properties) super.bucket(bucket);
        }

        @Override
        public PotionFluid.Properties block(Supplier<? extends FlowingFluidBlock> block) {
            return (PotionFluid.Properties) super.block(block);
        }

        @Override
        public PotionFluid.Properties slopeFindDistance(int slopeFindDistance) {
            return (PotionFluid.Properties) super.slopeFindDistance(slopeFindDistance);
        }

        @Override
        public PotionFluid.Properties levelDecreasePerBlock(int levelDecreasePerBlock) {
            return (PotionFluid.Properties) super.levelDecreasePerBlock(levelDecreasePerBlock);
        }

        @Override
        public PotionFluid.Properties explosionResistance(float explosionResistance) {
            return (PotionFluid.Properties) super.explosionResistance(explosionResistance);
        }

        /**
         * @deprecated
         * @param par gets the parent PotionFluid
         * @return this instance
         */
        private PotionFluid.Properties parent(PotionFluid par) {
            parent = par;
            return this;
        }

    }

    public static PotionFluid EMPTY = new PotionFluid(new PotionType(Potions.EMPTY, 0, PotionUtils.getPotionColor(Potions.EMPTY)));
    static {
        EMPTY.setSource(Fluids.WATER);
        EMPTY.setFlowing(Fluids.FLOWING_WATER);
    }
}
