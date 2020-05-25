package com.romoalamn.amf.blocks;

import com.romoalamn.amf.AMFMod;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class AMFBlocks{
    @ObjectHolder(AMFMod.MODID + ":cauldron")
    public static CauldronBlock cauldronBlock;

    @ObjectHolder(AMFMod.MODID + ":cauldron")
    public static TileEntityType<CauldronTile> cauldronBlockTile;

    @ObjectHolder(AMFMod.MODID + ":cauldron")
    public static ContainerType<CauldronContainer> cauldronContainerType;
}
