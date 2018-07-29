package com.dragonflame.main.machine;

public interface Machine extends Device {

    int getRequiredEnergy();

    default boolean canUseEnergy(){
        return getEnergy() >= getRequiredEnergy();
    }

}
