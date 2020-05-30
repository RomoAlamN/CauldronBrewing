package com.romoalamn.cauldron.blocks.fluid;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.state.Property;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
@MethodsReturnNonnullByDefault
public class PotionBlock extends FlowingFluidBlock {

    Property<PotionType> fluidProperty = new Property<PotionType>("fluid_contained", PotionType.class) {
        @Override
        public Collection<PotionType> getAllowedValues() {
            return CauldronUtils.getPotions();
        }

        @Override
        public Optional<PotionType> parseValue(@Nonnull String value) {
            return Optional.of(CauldronUtils.getPotion(value));
        }

        @Override
        public String getName(@Nonnull PotionType value) {
            return Objects.requireNonNull(value.getRegistryName()).toString();
        }
    };
    /**
     * @param supplier    A fluid supplier such as {@link RegistryObject <Fluid>}
     * @param p_i48368_1_
     */
    public PotionBlock(Supplier<? extends FlowingFluid> supplier, Properties p_i48368_1_) {
        super(supplier, p_i48368_1_);
    }
}
