package com.romoalamn.cauldron.setup.modcompat;

import com.romoalamn.cauldron.blocks.fluid.PotionType;
import net.minecraftforge.registries.IForgeRegistry;

public class ApotheosisDeferredWorker implements IDeferredWorker{
    @Override
    public Object handle(String event, Object eventObject) {
        if ("register_potions".equals(event)) {
            registerPotions(eventObject);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void registerPotions(Object event0){
        IForgeRegistry<PotionType> event = (IForgeRegistry<PotionType>) event0;


    }
}
