package com.romoalamn.amf.blocks;

import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class AMFBlocks{
    @ObjectHolder("amf:cauldron")
    public static CauldronBlock cauldronBlock;

    @ObjectHolder("amf:cauldron")
    public static TileEntityType<CauldronTile> cauldronBlockTile;

    @ObjectHolder("amf:cauldron")
    public static ContainerType<CauldronContainer> cauldronContainerType;
}
