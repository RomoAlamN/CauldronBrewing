package com.romoalamn.cauldron.blocks;

import com.romoalamn.cauldron.blocks.fluid.recipe.CauldronCapabilities;
import com.romoalamn.cauldron.blocks.fluid.recipe.potionhandler.IPotionHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class CauldronColor implements IBlockColor {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public int getColor(@Nonnull BlockState state, @Nullable ILightReader blockReader, @Nullable BlockPos pos, int tintIndex) {
        if(blockReader == null) return 0xFFFFFFFF;
        TileEntity cauldron;
        try {
            cauldron = blockReader.getTileEntity(Objects.requireNonNull(pos));
            if(cauldron == null){
                throw new NullPointerException("Cauldron has no Tile Entity");
            }
        }catch (NullPointerException npe){
            logger.warn( npe);
            return 0xFFFFFFFF;
        }
//        logger.info("Got Tile Entity");
        LazyOptional<IPotionHandler> handler = cauldron.getCapability(CauldronCapabilities.POTION_HANDLER_CAPABILITY);
//        logger.info("Retrieved Fluid Handler");
        class Here {
            int color = 0xFFFFFFFF;
        }
        Here here = new Here();
//        logger.info("Getting Color");
        handler.ifPresent(h -> {
//            logger.info("Getting effects");
            List<EffectInstance> effects = h.getPotion().potion.getEffects();
//            logger.info(h.getFluidInTank(0).getFluid());
//            logger.info("Getting color from effects. ");
            here.color = PotionUtils.getPotionColorFromEffectList(effects);
//            logger.info("Color retrieved was: {}", Integer.toString(here.color, 16));
        }
        );
        return here.color;
    }
}
