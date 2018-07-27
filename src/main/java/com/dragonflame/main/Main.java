package com.dragonflame.main;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.Bukkit.getPlayer;
import static org.bukkit.ChatColor.*;

public class Main extends JavaPlugin implements Listener {

    private static JsonObject dataJson;
    static Map<UUID, MyPlayerData> dataMap = new LinkedHashMap<>();

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        Player p = e.getEntity();
        MyPlayerData.logback(p);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        File file = new File("player.json");
        dataJson = new JsonObject();
        if(file.exists()) {
            try {
                dataJson = new JsonParser().parse(new FileReader(file)).getAsJsonObject();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


        for (Player p : Bukkit.getOnlinePlayers()){
            storeData(p.getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        storeData(p.getUniqueId());
    }

    private void storeData(UUID uuid){
        if(dataJson.has(uuid.toString())){
            dataMap.computeIfAbsent(uuid, k -> MyPlayerData.deserData(dataJson.getAsJsonObject(uuid.toString())));
        } else {
            dataMap.computeIfAbsent(uuid, k -> new MyPlayerData());
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player))
        {
            return false;
        }
        Player p = (Player) sender;
        MyPlayerData data;

        String namespace = "MechTech:";

        if(label.startsWith(namespace)){
            label = label.substring(namespace.length());
        }

        switch (label){
            case "tpa":
                if (args.length != 1)
                    return false;
                Player playerRequested = getPlayer(args[0]);
                playerRequested.sendMessage(GOLD + "TP request from " + DARK_RED + p.getName() + GOLD + ", say " + GREEN + "/tpaccept" + GOLD + " to accept and " + RED + "/tpdeny" + GOLD + " to deny");
                getData(playerRequested).setTprequest(p.getUniqueId());
                p.sendMessage(GOLD + "Request sent to " + DARK_RED + playerRequested.getName());
                return true;
            case "tpaccept":
                if (args.length != 0)
                    return false;
                data = getData(p);
                if (data.getTprequest() == null)
                    p.sendMessage(RED + "No Requests");
                Player playerWhoRequested = getPlayer(data.getTprequest());
                MyPlayerData.logbackAndTP(playerWhoRequested, p.getLocation());
                playerWhoRequested.sendMessage(GREEN + "Request Accepted");
                p.sendMessage(GREEN + "Request Accepted");
                data.setTprequest(null);
                return true;
            case "spawn":
                Location loc = Bukkit.getWorld("world").getSpawnLocation();
                loc.setYaw(-180);
                loc.setPitch(0);
                MyPlayerData.logbackAndTP(p, loc);
                return true;
            case "tpdeny":
                if(args.length != 0)
                    return false;
                data = getData(p);
                if (data.getTprequest() == null)
                    p.sendMessage("No Requests");
                getPlayer(data.getTprequest()).sendMessage(RED + "Request Denied");
                p.sendMessage(RED + "Request Denied");
                data.setTprequest(null);
                return true;
            case "f":
                Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                String[] strings = new String[onlinePlayers.size()];
                int x = 0;
                for(Player player : onlinePlayers){
                    strings[x++] = "<" + player.getName() + "> f";
                }
                onlinePlayers.forEach(player -> player.sendMessage(strings));
                return true;
            case "back":
                getData(p).back(p);
                return true;
            case "home":
                data = getData(p);
                switch (args.length){
                    case 0:
                        data.home(p, null);
                        break;
                    case 1:
                        data.home(p, args[0]);
                        break;
                    case 2:
                        if(p.hasPermission("dragonflame.home.op")){
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                            UUID uuid = offlinePlayer.getUniqueId();
                            if(dataJson.has(uuid.toString()) && !dataMap.containsKey(uuid))
                                storeData(uuid);
                            dataMap.get(uuid).home(p, args[1]);
                            break;
                        }
                    default:
                        p.sendMessage(RED + "Too Many Arguments");
                        break;
                }
                return true;
            case "sethome":
                data = getData(p);
                switch(args.length){
                    case 0:
                        data.setHome(p, null);
                        break;
                    case 1:
                        data.setHome(p, args[0]);
                        break;
                    case 6:
                        if(p.hasPermission("dragonflame.home.op")){
                            OfflinePlayer playerTargeted = Bukkit.getOfflinePlayer(args[0]);
                            if(!dataJson.has(playerTargeted.getUniqueId().toString())){
                                storeData(playerTargeted.getUniqueId());
                            }
                            Location loca = new Location(
                                    Bukkit.getWorld(args[5]),
                                    Integer.parseInt(args[2]),
                                    Integer.parseInt(args[3]),
                                    Integer.parseInt(args[4])
                            );
                            dataMap.get(playerTargeted.getUniqueId()).setHome(p, args[1], loca);
                            break;
                        }
                    default:
                        p.sendMessage(RED + "Too Many Arguments");
                        break;
                }
                return true;
            case "delhome":
                data = getData(p);
                switch (args.length){
                    case 0:
                        data.delHome(p, null);
                        break;
                    case 1:
                        data.delHome(p, args[0]);
                        break;
                }
                return true;
            case "book":
                Bukkit.dispatchCommand(getServer().getConsoleSender(), "execute as " + p.getName() + " run function server:triggers/book");
                return true;
            case "recipebook":
                Bukkit.dispatchCommand(getServer().getConsoleSender(), "execute as " + p.getName() + " run function server:triggers/recipe");
                return true;
            case "oldhome":
                Bukkit.dispatchCommand(getServer().getConsoleSender(), "scoreboard players set " + p.getName() + " home " + args[0]);
                return true;
            case "temp":
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getTemp(p.getName(), args));
                ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
                Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
                Objective objective = scoreboard.getObjective("home1x");
                for(OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()){
                    p.sendMessage("" + objective.getScore(offlinePlayer).getScore());
                }
                return true;
        }

        return false;
    }

    private static String getTemp(String player, String[] args){
        StringBuilder sb = new StringBuilder()
                .append("tellraw ")
                .append(player)
                .append(" [\"")
                .append(args[0])
                .append("'s homes:\"");
        for (int i = 1;i <= 3;i++){
            sb.append(",\"\\nHome ");
            sb.append(i);
            sb.append(": \"");
            for (char chr : new char[]{'x', 'y', 'z', 'd'}){
                sb.append(",{\"score\":{\"objective\":\"home");
                sb.append(i);
                sb.append(chr);
                sb.append("\",\"name\":\"");
                sb.append(args[0]);
                sb.append("\"}}");
                if(chr != 'd')
                    sb.append(",\", \"");
            }
        }
        sb.append("]");
        return sb.toString();
    }



    private MyPlayerData getData(Player p){
        return dataMap.get(p.getUniqueId());
    }



    @Override
    public void onDisable() {
        try {
            FileWriter fileWriter = new FileWriter("player.json");
            JsonObject json = new JsonObject();
            dataMap.forEach((uuid, myPlayerData) -> json.add(uuid.toString(), MyPlayerData.serData(myPlayerData)));
            fileWriter.write(json.toString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
