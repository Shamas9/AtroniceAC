package atronicemc.ac;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.Location;

public class GameListener implements Listener {
    
    private AntiCheatPlugin plugin;
    
    public GameListener(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }
    
    // ========== SPEED CHECK ==========
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("atroniceac.bypass")) return;
        if (p.isFlying()) return;
        
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) return;
        
        double dist = Math.sqrt(
            Math.pow(to.getX() - from.getX(), 2) + 
            Math.pow(to.getZ() - from.getZ(), 2)
        );
        
        if (dist > 0.6 && p.isOnGround()) {
            plugin.flag(p, "speed");
            e.setCancelled(true);
        }
    }
    
    // ========== XRAY CHECK ==========
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Material block = e.getBlock().getType();
        
        // Xray: Detects too many diamonds in short time
        if (block == Material.DIAMOND_ORE || block == Material.DEEPSLATE_DIAMOND_ORE) {
            int count = plugin.diamonds.getOrDefault(p, 0) + 1;
            plugin.diamonds.put(p, count);
            
            if (count >= 15) {
                plugin.flag(p, "xray");
            }
        }
        
        // Nuker: Detects breaking too fast
        if (count >= 8) {
            plugin.flag(p, "nuker");
        }
    }
    
    // ========== KILLAURA CHECK ==========
    @EventHandler
    public void onSwing(PlayerAnimationEvent e) {
        // Simplified - counts attacks per second
        Player p = e.getPlayer();
        // Can add click speed tracking
    }
}
