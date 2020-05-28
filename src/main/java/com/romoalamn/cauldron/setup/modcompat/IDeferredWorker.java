package com.romoalamn.cauldron.setup.modcompat;

@FunctionalInterface
public interface IDeferredWorker {
    Object handle(String event, Object eventObject);
    enum State {
        EMPTY
    }
}
