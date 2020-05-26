package com.romoalamn.cauldron.blocks;

import com.romoalamn.cauldron.CauldronMod;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class CauldronBlocks {
    @ObjectHolder(CauldronMod.MODID + ":cauldron")
    public static CauldronBlock cauldronBlock;

    @ObjectHolder(CauldronMod.MODID + ":cauldron")
    public static TileEntityType<CauldronTile> cauldronBlockTile;

    @ObjectHolder(CauldronMod.MODID + ":cauldron")
    public static ContainerType<CauldronContainer> cauldronContainerType;
}
