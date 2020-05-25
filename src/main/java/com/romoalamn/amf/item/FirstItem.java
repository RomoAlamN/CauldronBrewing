package com.romoalamn.amf.item;


import com.romoalamn.amf.setup.AMFCommonSetup;
import net.minecraft.item.Item;

/**
 * @deprecated
 */
public class FirstItem extends Item {
    public FirstItem(){
        super(new Properties()
        .maxStackSize(1)
        .group(AMFCommonSetup.itemGroup));
    }
}
