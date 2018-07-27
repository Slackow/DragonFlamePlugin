package com.dragonflame.main;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Home {
    private Location location;
    private String name;

    public Home(Location location, String name) {
        this.location = location;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public void tp(Player player){
        player.teleport(location);
    }

    @Override
    public String toString() {
        return "Home[location=" + location + ", name=" + name + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(this == obj) return true;
        if(obj instanceof Home){
            Home home = (Home) obj;
            return this.name.equals(home.name);
        }
        return false;
    }
}
