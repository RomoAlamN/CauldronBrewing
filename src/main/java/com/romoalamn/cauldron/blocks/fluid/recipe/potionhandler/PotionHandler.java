package com.romoalamn.cauldron.blocks.fluid.recipe.potionhandler;

import com.romoalamn.cauldron.blocks.fluid.CauldronUtils;
import com.romoalamn.cauldron.blocks.fluid.FluidComponent;
import com.romoalamn.cauldron.blocks.fluid.PotionType;
import net.minecraft.nbt.CompoundNBT;

public class PotionHandler implements IPotionHandler {
    FluidComponent currentContents = FluidComponent.EMPTY;
    int capacity;
    private void modifySafe(int newAmount){
        if(currentContents.isEmpty()){
            currentContents = new FluidComponent(currentContents.potion, newAmount);
        }else{
            currentContents.amount = newAmount;
        }
    }

    @Override
    public boolean isEmpty() {
        return currentContents == FluidComponent.EMPTY || currentContents.potion == PotionType.EMPTY || currentContents.amount == 0;
    }

    @Override
    public boolean isFull() {
        return currentContents.amount == capacity;
    }

    private void modifySafe(PotionType newType){
        if(currentContents.isEmpty()){
            currentContents = new FluidComponent(newType, currentContents.amount);
        }else{
            currentContents.potion = newType;
        }
    }
    @Override
    public FluidComponent fill(int amount, PotionAction action) {
        int actualFill = Math.max(amount, capacity - currentContents.amount);
        if(action.execute()){
            modifySafe(currentContents.amount + actualFill);
        }
        return new FluidComponent(currentContents.potion, actualFill);
    }

    @Override
    public FluidComponent drain(int amount, PotionAction action) {
        int actualDrain = Math.min(amount, currentContents.amount);
        if(action.execute()) {
            modifySafe(currentContents.amount - actualDrain);
        }
        PotionType potion = currentContents.potion;
        checkEmpty();
        return new FluidComponent(potion, actualDrain);
    }

    @Override
    public FluidComponent drain(FluidComponent comp, PotionAction action) {
        return drain(comp.amount, action);
    }

    @Override
    public FluidComponent fill(FluidComponent comp, PotionAction action) {
        return fill(comp.amount, action);
    }

    private void checkEmpty(){
        if(getPotion().isEmpty()){
            currentContents = FluidComponent.EMPTY;
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
    public FluidComponent empty(PotionAction action) {
        return drain(capacity, action);
    }

    @Override
    public FluidComponent getPotion() {
        return currentContents;
    }

    public void readFromNBT(CompoundNBT tag) {
        readContentsFrom((CompoundNBT) tag.get("contents"));
        capacity = tag.getInt("capacity");
    }

    public void readContentsFrom(CompoundNBT nbt) {
        currentContents = new FluidComponent(
                CauldronUtils.getPotion(nbt.getString("fluid")),
                nbt.getInt("amount"));
    }
}
