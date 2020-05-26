package com.romoalamn.cauldron.blocks.fluid.recipe;

import com.romoalamn.cauldron.blocks.fluid.CauldronFluids;
import com.romoalamn.cauldron.blocks.fluid.PotionType;
import net.minecraft.nbt.CompoundNBT;

public class PotionHandler implements IPotionHandler {
    CauldronUtils.FluidComponent currentContents = CauldronUtils.FluidComponent.EMPTY;
    int capacity;
    private void modifySafe(int newAmount){
        if(currentContents.isEmpty()){
            currentContents = new CauldronUtils.FluidComponent(currentContents.potion, newAmount);
        }else{
            currentContents.amount = newAmount;
        }
    }
    private void modifySafe(PotionType newType){
        if(currentContents.isEmpty()){
            currentContents = new CauldronUtils.FluidComponent(newType, currentContents.amount);
        }else{
            currentContents.potion = newType;
        }
    }
    @Override
    public CauldronUtils.FluidComponent fill(int amount, PotionAction action) {
        int actualFill = Math.max(amount, capacity - currentContents.amount);
        if(action.execute()){
            modifySafe(currentContents.amount + actualFill);
        }
        return new CauldronUtils.FluidComponent(currentContents.potion, actualFill);
    }

    @Override
    public CauldronUtils.FluidComponent drain(int amount, PotionAction action) {
        int actualDrain = Math.min(amount, currentContents.amount);
        if(action.execute()) {
            modifySafe(currentContents.amount - actualDrain);
        }
        PotionType potion = currentContents.potion;
        checkEmpty();
        return new CauldronUtils.FluidComponent(potion, actualDrain);
    }

    @Override
    public CauldronUtils.FluidComponent drain(CauldronUtils.FluidComponent comp, PotionAction action) {
        return drain(comp.amount, action);
    }

    @Override
    public CauldronUtils.FluidComponent fill(CauldronUtils.FluidComponent comp, PotionAction action) {
        return fill(comp.amount, action);
    }

    private void checkEmpty(){
        if(getPotion().isEmpty()){
            currentContents = CauldronUtils.FluidComponent.EMPTY;
        }
    }

    public PotionHandler(int cap) {
        capacity = cap;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public void replaceFluid(PotionType other) {
        modifySafe(other);
    }

    public void writeToNBT(CompoundNBT tag) {
        tag.put("contents", getContentsNBT());
        tag.putInt("capacity", capacity);
    }

    private CompoundNBT getContentsNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("fluid", currentContents.potion.getRegistryName().toString());
        tag.putInt("amount", currentContents.amount);
        return tag;
    }

    @Override
    public CauldronUtils.FluidComponent empty(PotionAction action) {
        return drain(capacity, action);
    }

    @Override
    public CauldronUtils.FluidComponent getPotion() {
        return currentContents;
    }

    public void readFromNBT(CompoundNBT tag) {
        readContentsFrom((CompoundNBT) tag.get("contents"));
        capacity = tag.getInt("capacity");
    }

    public void readContentsFrom(CompoundNBT nbt) {
        currentContents = new CauldronUtils.FluidComponent(
                CauldronFluids.getPotion(nbt.getString("fluid")),
                nbt.getInt("amount"));
    }
}
