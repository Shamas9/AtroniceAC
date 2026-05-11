package atronicemc.ac;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.*;
import java.net.*;
import java.io.*;

public class AntiCheatPlugin extends JavaPlugin {
    
    public Map<Player, Map<String, Integer>> vls = new HashMap<>();
    public Map<Player, Integer> diamonds = new HashMap<>();
    
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getCommand("ac").setExecutor(new ACCommand(this));
        
        // Reset every 5 min
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            vls.clear();
            diamonds.clear();
        }, 6000, 6000);
        
        getLogger().info("✅ AtroniceAC Ready!");
    }
    
    public void flag(Player p, String check) {
        vls.putIfAbsent(p, new HashMap<>());
        int vl = vls.get(p).getOrDefault(check, 0) + 1;
        vls.get(p).put(check, vl);
        
        // Alert staff in-game
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("atroniceac.alerts")) {
                staff.sendMessage(color("&c[AC] &7" + p.getName() + " &efailed &c" + check + " &ex" + vl));
            }
        }
        
        // Send to Discord
        if (vl >= getConfig().getInt("checks." + check + ".vl-to-report", 20)) {
            sendDiscord(p, check, vl);
        }
    }
    
    public void sendDiscord(Player p, String check, int vl) {
        String webhook = getConfig().getString("discord.webhook-url");
        if (webhook == null || webhook.isEmpty()) return;
        
        new Thread(() -> {
            try {
                URL url = new URL(webhook);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                
                String json = "{\"embeds\":[{" +
                    "\"title\":\"🚨 CHEATER DETECTED\"," +
                    "\"color\":16711680," +
                    "\"fields\":[" +
                    "{\"name\":\"Player\",\"value\":\"" + p.getName() + "\"}," +
                    "{\"name\":\"Hack\",\"value\":\"" + check.toUpperCase() + "\"}," +
                    "{\"name\":\"VL\",\"value\":\"" + vl + "\"}," +
                    "{\"name\":\"World\",\"value\":\"" + p.getWorld().getName() + "\"}" +
                    "]}]}";
                
                con.getOutputStream().write(json.getBytes());
                con.getInputStream();
                con.disconnect();
            } catch (Exception e) {}
        }).start();
    }
    
    public String color(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }
                  }
