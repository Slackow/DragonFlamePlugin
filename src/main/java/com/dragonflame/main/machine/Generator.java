package com.dragonflame.main.machine;

public interface Generator extends Device {
    default void generate(){
        int oof = getEnergy() + getRate();
        int capacity = getMaxCapacity();
        setEnergy(oof >= capacity ? capacity : oof);
    }
}
