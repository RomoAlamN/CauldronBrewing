package com.romoalamn.cauldron.setup.modcompat;

import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;

public class DeferredActions {
    private HashMap<String, IDeferredWorker> workers = new HashMap<>();

    public Object postEventTo(String modid, String event, Object eventObject){
        if(workers.containsKey(modid)){
            return workers.get(modid).handle(event, eventObject);
        }
        return IDeferredWorker.State.EMPTY;
    }
    public List<Object> postEventToAll(String event, Object eventObject){
        List<Object> rets = Lists.newArrayList();
        workers.keySet().forEach((it)-> rets.add(postEventTo(it, event, eventObject)));
        return rets;
    }
    public void addWorker(String modid, IDeferredWorker worker){
        workers.put(modid, worker);
    }
    public static final DeferredActions INSTANCE = new DeferredActions();
}
