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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

public class Main extends JavaPlugin implements Listener {




 //   private List<Device> devices = new ArrayList<>();

    static Map<UUID, MyPlayerData> dataMap = new LinkedHashMap<>();

   /* @EventHandler
    public void onBreak(BlockBreakEvent e){
        e.getPlayer().sendMessage("lol");
    }




    @EventHandler
    public void onIgnite(BlockIgniteEvent e){
        Player p = e.getPlayer();
        if(p == null) {
            return;
        }
        e.setCancelled(true);
        p.sendMessage("ignited at " + e.getBlock().getLocation());
        CoalGenerator generator = new CoalGenerator(e.getBlock().getLocation());
        generator.create();
    }
*/

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        MyPlayerData.logback(p);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        fetchData(uuid);
    }



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
                loc.add(0.5, 0, 0.5);
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
                            OfflinePlayer offlinePlayer = getOfflinePlayer(args[0]);
                            UUID uuid = offlinePlayer.getUniqueId();
                            fetchData(uuid).home(p, args[1]);
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
                            OfflinePlayer playerTargeted = getOfflinePlayer(args[0]);
                            data = fetchData(playerTargeted.getUniqueId());
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
                    case 2:
                        if(p.hasPermission("dragonflame.home.op")) {
                            OfflinePlayer offlinePlayer = getOfflinePlayer(args[0]);
                            UUID uuid = offlinePlayer.getUniqueId();
                            data = fetchData(uuid);
                            data.delHome(p, args[1]);
                            break;
                        }
                    default:
                        p.sendMessage(RED + "Too Many Arguments");
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
                    if (args.length == 0)
                        return false;
                    target = getPlayer(args[0]);
                    if (target != null)
                        if (args.length == 1)
                            p.openInventory(target.getInventory());
                        else
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
            case "nick":
                if (p.hasPermission("dragonflame.nick")) {
                    if (args.length == 0) {
                        p.setDisplayName(p.getName());
                        p.sendMessage(RED + "Nickname reset!");
                    }
                    if (args.length == 1) {
                        p.setDisplayName(args[0]);
                        p.setPlayerListName(args[0]);
                        p.sendMessage(GREEN + "Your nickname is now " + args[0] + "!");
                    }
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
            fetchData(uuid);
        }
    }

    private static MyPlayerData fetchData(UUID uuid) {
        if (dataMap.containsKey(uuid)) {
            return dataMap.get(uuid);
        }
        File file = getFile(uuid);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)){
                JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
                return dataMap.put(uuid, MyPlayerData.deserData(jsonObject));
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

    @SuppressWarnings("deprecation")
    private static OfflinePlayer getOfflinePlayer(String s){
        return Bukkit.getOfflinePlayer(s);
    }

    private static Player getPlayer(String s) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equals(s)) {
                return player;
            }
        }
        return null;
    }





    private MyPlayerData getData(Player p) {
        return dataMap.get(p.getUniqueId());
    }


    @Override
    public void onDisable() {
        File f = new File("plugins/data");
        if(f.isDirectory() || f.mkdir())
            dataMap.keySet().forEach(Main::saveData);
        else
            Bukkit.broadcastMessage("something went wrong lmao");
    }
}
