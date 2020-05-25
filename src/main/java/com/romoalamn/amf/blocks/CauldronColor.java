package com.romoalamn.amf.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

import javax.annotation.Nullable;

public class CauldronColor implements IBlockColor {
    @Override
    public int getColor(BlockState state, @Nullable ILightReader blockReader, @Nullable BlockPos pos, int tintIndex) {

        return 0xFFFF0000;
    }
}
