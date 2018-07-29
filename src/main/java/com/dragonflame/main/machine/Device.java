package com.dragonflame.main.machine;

import org.bukkit.Location;

import java.util.UUID;

public interface Device {

    UUID getOwner();


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
