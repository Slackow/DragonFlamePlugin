package com.dragonflame.main;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

public class Main extends JavaPlugin implements Listener {

    static Map<UUID, MyPlayerData> dataMap = new LinkedHashMap<>();

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        MyPlayerData.logback(p);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        putData(uuid);
    }

    private void putData(UUID uuid) {
        dataMap.put(uuid, fetchData(uuid));
    }


    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }
        Player p = (Player) sender;
        MyPlayerData data;

        String namespace = "mechtech:";

        if (label.startsWith(namespace)) {
            label = label.substring(namespace.length());
        }
        Player target;
        switch (label) {
            case "tpa":
                if (args.length != 1)
                    return false;
                target = getPlayer(args[0]);

                if (target == null)
                    break;
                target.sendMessage(GOLD + "TP request from " + DARK_RED + p.getName() + GOLD + ", say " + GREEN + "/tpaccept" + GOLD + " to accept and " + RED + "/tpdeny" + GOLD + " to deny");
                getData(target).setTprequest(p.getUniqueId());
                p.sendMessage(GOLD + "Request sent to " + DARK_RED + target.getName());
                return true;
            case "tpaccept":
                if (args.length != 0)
                    return false;
                data = getData(p);
                if (data.getTprequest() == null)
                    p.sendMessage(RED + "No Requests");
                Player playerWhoRequested = Bukkit.getPlayer(data.getTprequest());
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
                if (args.length != 0)
                    return false;
                data = getData(p);
                if (data.getTprequest() == null)
                    p.sendMessage("No Requests");
                Bukkit.getPlayer(data.getTprequest()).sendMessage(RED + "Request Denied");
                p.sendMessage(RED + "Request Denied");
                data.setTprequest(null);
                return true;
            case "f":
                Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                String[] strings = new String[onlinePlayers.size()];
                int x = 0;
                for (Player player : onlinePlayers) {
                    strings[x++] = "<" + player.getName() + "> f";
                }
                onlinePlayers.forEach(player -> player.sendMessage(strings));
                return true;
            case "back":
                getData(p).back(p);
                return true;
            case "home":
                data = getData(p);
                switch (args.length) {
                    case 0:
                        data.home(p, null);
                        break;
                    case 1:
                        data.home(p, args[0]);
                        break;
                    case 2:
                        if (p.hasPermission("dragonflame.home.op")) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                            UUID uuid = offlinePlayer.getUniqueId();
                            putData(uuid);
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
                switch (args.length) {
                    case 0:
                        data.setHome(p, null);
                        break;
                    case 1:
                        data.setHome(p, args[0]);
                        break;
                    case 6:
                        if (p.hasPermission("dragonflame.home.op")) {
                            OfflinePlayer playerTargeted = Bukkit.getOfflinePlayer(args[0]);
                            data = fetchData(playerTargeted.getUniqueId());


                            dataMap.put(playerTargeted.getUniqueId(), data);
                            Location loca = new Location(
                                    Bukkit.getWorld(args[5]),
                                    Integer.parseInt(args[2]),
                                    Integer.parseInt(args[3]),
                                    Integer.parseInt(args[4])
                            );
                            data.setHome(p, args[1], loca);
                            break;
                        }
                    default:
                        p.sendMessage(RED + "Too Many Arguments");
                        break;
                }
                return true;
            case "delhome":
                data = getData(p);
                switch (args.length) {
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
                try {
                    JsonObject jsonObject = new JsonParser().parse(new FileReader("player.json")).getAsJsonObject();
                    for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        FileWriter fileWriter = new FileWriter("plugins/data/" + entry.getKey() + ".json");
                        fileWriter.write(entry.getValue().toString());
                        fileWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


                return true;
            case "tempa":
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getTemp(p.getName(), args));
                ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
                Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                putData(offlinePlayer.getUniqueId());
                for (int i = 1; i <= 3; i++) {
                    Objective ox = scoreboard.getObjective("home" + i + "x");
                    Objective oy = scoreboard.getObjective("home" + i + "y");
                    Objective oz = scoreboard.getObjective("home" + i + "z");
                    Objective od = scoreboard.getObjective("home" + i + "d");
                    int ix = ox.getScore(offlinePlayer).getScore();
                    int iy = oy.getScore(offlinePlayer).getScore();
                    int iz = oz.getScore(offlinePlayer).getScore();
                    int id = od.getScore(offlinePlayer).getScore();
                    MyPlayerData myPlayerData = dataMap.get(offlinePlayer.getUniqueId());
                    if (ix == 0 && iy == 0 && iz == 0)
                        continue;
                    myPlayerData.setHome(p, "" + i, new Location(worlds[id + 1], ix, iy, iz));
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "scoreboard players reset " + args[0] + " home1d");
                return true;
            case "ttp":
                if (p.hasPermission("dragonflame.tp")) {
                    switch (args.length) {
                        case 1:
                            target = getPlayer(args[0]);
                            if (target == null) {
                                p.sendMessage("Invalid player");
                                break;
                            }

                            MyPlayerData.logbackAndTP(p, target.getLocation());
                            break;
                        case 2:
                            target = getPlayer(args[0]);

                            if(target != null)
                            if("void".equals(args[1])){
                                getData(target).setBack(null);
                                target.teleport(target.getLocation().add(0, -5000, 0));
                            }


                            Player target2 = getPlayer(args[1]);

                            if (target == null || target2 == null) {
                                p.sendMessage("Invalid player");
                                break;
                            }

                            MyPlayerData.logbackAndTP(target, target2.getLocation());
                            break;

                        default:
                            p.sendMessage(RED + "Too Many or Few Args");
                            break;
                    }
                }
                return true;
            case "fly":
                if (p.hasPermission("dragonflame.fly")) {
                    if (args.length == 1) {
                        if (p.hasPermission("dragonflame.flyothers")) {
                            target = getPlayer(args[0]);
                            if (target != null) {
                                target.setAllowFlight(!target.getAllowFlight());
                                if (target.getAllowFlight())
                                    target.sendMessage(GOLD + "Flight is Enabled!");
                                else
                                    target.sendMessage(GOLD + "Flight is Disabled!");
                            } else
                                p.sendMessage(RED + "Invalid Player!");
                        }
                    } else {
                        p.setAllowFlight(!p.getAllowFlight());
                        if (p.getAllowFlight())
                            p.sendMessage(GOLD + "Flight is Enabled!");
                        else
                            p.sendMessage(GOLD + "Flight is Disabled!");
                    }
                }
                return true;
            case "echest":
                if (p.hasPermission("dragonflame.echest"))
                    if (args.length != 1)
                        p.openInventory(p.getEnderChest());
                    else if (p.hasPermission("dragonflame.echestothers")) {
                        target = getPlayer(args[0]);
                        if (target != null)
                            p.openInventory(target.getEnderChest());
                        else
                            p.sendMessage(RED + "Invalid Player!");
                    }
                return true;
            case "repair":
                if (p.hasPermission("dragonflame.repair"))
                    p.getInventory().getItemInMainHand().setDurability((short) 0);
                return true;
            case "feed":
                if (p.hasPermission("dragonflame.feed")) {
                    p.setFoodLevel(20);
                    p.setSaturation(20);
                }
                return true;
            case "invsee":
                if (p.hasPermission("dragonflame.invsee")) {
                    if (args.length != 1)
                        return false;
                    target = getPlayer(args[0]);
                    if (target != null) {
                        p.openInventory(target.getInventory());
                    } else
                        p.sendMessage(RED + "Invalid Player!");
                    return true;
                }
            case "hat":
                if (p.hasPermission("dragonflame.hat")) {
                    ItemStack item = p.getInventory().getItemInMainHand();
                    ItemStack swap = p.getInventory().getHelmet();
                    p.getInventory().setHelmet(item);
                    p.getInventory().setItemInMainHand(swap);
                    p.sendMessage(GREEN + "Poof!");
                    return true;
                }
        }
        return false;
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        for (Player p : Bukkit.getOnlinePlayers()) {
            UUID uuid = p.getUniqueId();
            putData(uuid);
        }
    }

    private static MyPlayerData fetchData(UUID uuid) {
        if (dataMap.containsKey(uuid)) {
            return dataMap.get(uuid);
        }
        File file = getFile(uuid);
        if (file.exists()) {
            try {
                FileReader reader = new FileReader(file);
                JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
                reader.close();
                return MyPlayerData.deserData(jsonObject);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new MyPlayerData();
    }

    private static File getFile(UUID uuid) {
        return new File("plugins/data/" + uuid + ".json");
    }

    private static void saveData(UUID uuid) {
        File file = getFile(uuid);
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(MyPlayerData.serData(dataMap.get(uuid)).toString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Player getPlayer(String s) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equals(s)) {
                return player;
            }
        }
        return null;
    }


    private static final World[] worlds = {Bukkit.getWorld("world_nether"), Bukkit.getWorld("world"), Bukkit.getWorld("world_the_end")};

    private static String getTemp(String player, String[] args) {
        StringBuilder sb = new StringBuilder()
                .append("tellraw ")
                .append(player)
                .append(" [\"")
                .append(args[0])
                .append("'s homes:\"");
        for (int i = 1; i <= 3; i++) {
            sb.append(",\"\\nHome ");
            sb.append(i);
            sb.append(": \"");
            for (char chr : new char[]{'x', 'y', 'z', 'd'}) {
                sb.append(",{\"score\":{\"objective\":\"home");
                sb.append(i);
                sb.append(chr);
                sb.append("\",\"name\":\"");
                sb.append(args[0]);
                sb.append("\"}}");
                if (chr != 'd')
                    sb.append(",\", \"");
            }
        }
        sb.append("]");
        return sb.toString();
    }


    private MyPlayerData getData(Player p) {
        return dataMap.get(p.getUniqueId());
    }


    @Override
    public void onDisable() {
        dataMap.keySet().forEach(Main::saveData);
    }
}
