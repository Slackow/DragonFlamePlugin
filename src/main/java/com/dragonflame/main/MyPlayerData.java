package com.dragonflame.main;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.RED;

class MyPlayerData {
    private Map<String, Location> homes = new HashMap<>();
    private Location back;
    private UUID tprequest;

    static MyPlayerData deserData(JsonElement json){
        MyPlayerData result = new MyPlayerData();
        JsonObject jsonO = json.getAsJsonObject();
        for (Entry<String, JsonElement> homeEntry : jsonO.getAsJsonObject("homes").entrySet()) {
            result.homes.put(homeEntry.getKey(), deserLocation(homeEntry.getValue()));
        }
        return result;
    }

    void setHome(Player p, String s){
        setHome(p, s, p.getLocation());
    }


    void setHome(Player p, String s, Location loc){
        if (s == null){
            s = "home";
        }
        if (!homes.containsKey(s)) {
            if(homes.size() < 3){
                homes.put(s, loc);
            } else {
                p.sendMessage("You have reached your limit of 3 homes");
                return;
            }
        } else {
            homes.put(s, loc);
        }
        p.sendMessage(GOLD + "Home Set \"" + s + "\"");
    }

    static void logback(Player p){
        Main.dataMap.get(p.getUniqueId()).setBack(p.getLocation());
    }

    static void logbackAndTP(Player p, Location loc){
        logback(p);
        p.teleport(loc);
        p.sendMessage(
                GOLD +
                "Teleported Successfully to ("
                + Math.round(loc.getX()) + ", " +
                Math.round(loc.getY()) + ", " +
                Math.round(loc.getZ()) + ")"
        );
    }

    void home(Player p, String s){
        if(s == null){
            if(homes.size() == 1){
                logbackAndTP(p, homes.values().iterator().next());
            } else {
                listHomes(p);
            }
        } else {
            if(homes.containsKey(s)){
                logbackAndTP(p, homes.get(s));
            } else {
                listHomes(p);
            }
        }
    }

    private void listHomes(Player p){
        if(homes.isEmpty()) {
            p.sendMessage(RED + "No Homes set");
            return;
        }
        p.sendMessage("Homes:");
        homes.forEach((str, location) -> p.sendMessage("-\"" + str + "\" (" + round(location.getX(), 2) + ", " + round(location.getY(), 2) + ", " + round(location.getZ(), 2)  + ") Dimension: " + location.getWorld().getName()));
    }

    private static double round(double i, int places){
        double result = i;
        for (int j = 0; j < places; j++) {
            result*=10;
        }
        result = Math.round(result);
        for (int j = 0; j < places; j++) {
            result/=10;
        }
        return result;
    }

    void delHome(Player p, String s){
        if (!homes.containsKey(s)){
            if(homes.size() == 1)
                homes.clear();
            else {
                p.sendMessage(RED + "Invalid Home Name");
                return;
            }
        } else {
            homes.remove(s);
        }
        p.sendMessage(GOLD + "Home Deleted" + (s == null ? "":": \"" + s + "\""));
    }

    static JsonElement serData(MyPlayerData data){
        JsonObject result = new JsonObject();
        JsonObject homesJson = new JsonObject();
        data.homes.forEach((s, location) -> homesJson.add(s, serLocation(location)));
        result.add("homes", homesJson);
        return result;
    }

    void back(Player p){
        if(back != null)
            logbackAndTP(p, getBack());
        else
            p.sendMessage(RED + "You have no place to go back to");

    }

    private static JsonObject serLocation(Location loc){
        JsonObject result = new JsonObject();
        result.addProperty("x", loc.getX());
        result.addProperty("y", loc.getY());
        result.addProperty("z", loc.getZ());
        result.addProperty("dim", loc.getWorld().getName());
        result.addProperty("pitch", loc.getPitch());
        result.addProperty("yaw", loc.getYaw());
        return result;
    }

    private static Location deserLocation(JsonElement jsonElement){
        JsonObject json  = jsonElement.getAsJsonObject();
        Location location = new Location(
                Bukkit.getWorld(json.get("dim").getAsString()),
                json.get("x").getAsDouble(),
                json.get("y").getAsDouble(),
                json.get("z").getAsDouble()
        );
        if(json.has("pitch"))
            location.setPitch(json.get("pitch").getAsFloat());
        if(json.has("yaw"))
            location.setYaw(json.get("yaw").getAsFloat());
        return location;
    }


    private Location getBack() {
        return back;
    }

    private void setBack(Location back) {
        this.back = back;
    }

    UUID getTprequest() {
        return tprequest;
    }

    void setTprequest(UUID tprequest) {
        this.tprequest = tprequest;
    }

    public Map<String, Location> getHomes() {
        return homes;
    }
}
