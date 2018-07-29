package com.dragonflame.main.machine.machines;

import com.dragonflame.main.machine.Generator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.Objects;
import java.util.UUID;

public class CoalGenerator implements Generator {
    private int energy;
    private ArmorStand armorStand;
    private Location location;

    @Override
    public UUID getOwner() {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void tick() {
        Dropper dropper = (Dropper) location.getBlock().getState();
        if (dropper.getInventory().contains(Material.COAL)) {
            Bukkit.getPlayer("cowslayer7890").sendMessage("Coal");
        }
    }

    public void create(){
        Objects.requireNonNull(location);
        Block block = location.getBlock();
        block.setType(Material.DROPPER, false);
        armorStand = (ArmorStand) location.getWorld().spawnEntity(location.add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
        armorStand.setFireTicks(Integer.MAX_VALUE);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCustomName("coal");
    }

    public void serialize(){
        armorStand = null;
    }

    public CoalGenerator(Location location){
        this.location = location;
    }

    @Override
    public int getRate() {
        return 4;
    }

    @Override
    public int getEnergy() {
        return energy;
    }

    @Override
    public void setEnergy(int energy) {
        this.energy = energy;
    }

    @Override
    public int getMaxCapacity() {
        return 10000;
    }

    @Override
    public Location getLocation() {
        return location;
    }

}
