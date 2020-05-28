package com.romoalamn.cauldron.setup.modcompat;

public class DeferredWorkerSpawner {
    public static IDeferredWorker getWorker(String modid){
        if ("apotheosis".equals(modid)) {
            return new ApotheosisDeferredWorker();
        }
        return null;
    }
}
