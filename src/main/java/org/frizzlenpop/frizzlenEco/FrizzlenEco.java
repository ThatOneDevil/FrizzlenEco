package org.frizzlenpop.frizzlenEco;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.frizzlenEco.api.EconomyAPI;
import org.frizzlenpop.frizzlenEco.api.EconomyProvider;
import org.frizzlenpop.frizzlenEco.commands.AdminCommands;
import org.frizzlenpop.frizzlenEco.commands.BankCommands;
import org.frizzlenpop.frizzlenEco.commands.EconomyCommands;
import org.frizzlenpop.frizzlenEco.config.ConfigManager;
import org.frizzlenpop.frizzlenEco.database.DatabaseManager;
import org.frizzlenpop.frizzlenEco.economy.EconomyManager;
import org.frizzlenpop.frizzlenEco.listeners.PlayerListener;
import org.frizzlenpop.frizzlenEco.metrics.MetricsManager;

import java.util.logging.Level;

public final class FrizzlenEco extends JavaPlugin {
    private static FrizzlenEco instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private EconomyManager economyManager;
    private EconomyAPI economyAPI;
    private MetricsManager metricsManager;

    @Override
    public void onEnable() {
        // Store instance for static access
        instance = this;
        
        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        // Initialize database
        databaseManager = new DatabaseManager(this);
        if (!databaseManager.initialize()) {
            getLogger().severe("Failed to initialize database connection. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize economy system
        economyManager = new EconomyManager(this);
        economyManager.initialize();
        
        // Register API
        economyAPI = new EconomyAPI(this);
        getServer().getServicesManager().register(EconomyProvider.class, economyAPI, this, ServicePriority.Normal);
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Initialize metrics
        metricsManager = new MetricsManager(this);
        metricsManager.initialize();
        
        getLogger().info("FrizzlenEco has been enabled!");
    }

    @Override
    public void onDisable() {
        if (economyManager != null) {
            economyManager.shutdown();
        }
        
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        
        getLogger().info("FrizzlenEco has been disabled!");
    }
    
    private void registerCommands() {
        try {
            new EconomyCommands(this);
            new BankCommands(this);
            new AdminCommands(this);
            getLogger().info("Commands registered successfully");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register commands", e);
        }
    }
    
    private void registerListeners() {
        try {
            getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
            getLogger().info("Listeners registered successfully");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register listeners", e);
        }
    }
    
    // Static accessor for the plugin instance
    public static FrizzlenEco getInstance() {
        return instance;
    }
    
    // Getters for managers
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public EconomyAPI getEconomyAPI() {
        return economyAPI;
    }
    
    public MetricsManager getMetricsManager() {
        return metricsManager;
    }
}
