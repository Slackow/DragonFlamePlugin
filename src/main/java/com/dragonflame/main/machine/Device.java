package com.dragonflame.main.machine;

import org.bukkit.Location;

public interface Device {


    void tick();

    int getRate();

    int getEnergy();

    void setEnergy(int energy);

    int getMaxCapacity();

    Location getLocation();

    default boolean isGenerator(){
        return this instanceof Generator;
    }

    default boolean isCapacitor(){
        return this instanceof Capacitor;
    }

    default boolean isMachine(){
        return this instanceof Machine;
    }
}
