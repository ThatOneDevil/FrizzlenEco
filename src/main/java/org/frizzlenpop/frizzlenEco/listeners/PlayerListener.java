package org.frizzlenpop.frizzlenEco.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.frizzlenpop.frizzlenEco.FrizzlenEco;
import org.frizzlenpop.frizzlenEco.economy.Currency;
import org.frizzlenpop.frizzlenEco.economy.EconomyManager;

import java.util.logging.Level;

/**
 * Listener for player-related events
 */
public class PlayerListener implements Listener {
    private final FrizzlenEco plugin;
    private final EconomyManager economyManager;
    
    /**
     * Creates a new PlayerListener
     * @param plugin the FrizzlenEco plugin instance
     */
    public PlayerListener(FrizzlenEco plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
    }
    
    /**
     * Handles player join events
     * @param event the join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Run in async task to avoid lag on join
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                // Ensure player has an account for the default currency
                Currency defaultCurrency = economyManager.getDefaultCurrency();
                
                if (!economyManager.hasAccount(player.getUniqueId(), defaultCurrency)) {
                    economyManager.createAccount(player.getUniqueId(), player.getName(), defaultCurrency);
                    
                    plugin.getLogger().info("Created economy account for player: " + player.getName());
                    
                    // Notify player
                    final Currency finalCurrency = defaultCurrency; // Need final for lambda
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        if (player.isOnline()) {
                            player.sendMessage("§a[FrizzlenEco] §eAccount created with §r" + 
                                    finalCurrency.format(finalCurrency.getInitialBalance()) + "§e!");
                        }
                    });
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error creating account for player: " + player.getName(), e);
            }
        });
    }
    
    /**
     * Handles player quit events
     * @param event the quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // No need to save player data here as it's done automatically by the database manager
        // But we could add additional logic if needed
    }
} 