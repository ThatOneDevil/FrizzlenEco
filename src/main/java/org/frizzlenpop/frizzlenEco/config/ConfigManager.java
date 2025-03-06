package org.frizzlenpop.frizzlenEco.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frizzlenpop.frizzlenEco.FrizzlenEco;
import org.frizzlenpop.frizzlenEco.economy.Currency;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages plugin configuration files
 */
public class ConfigManager {
    private final FrizzlenEco plugin;
    
    private FileConfiguration config;
    private File configFile;
    
    private FileConfiguration currencyConfig;
    private File currencyFile;
    
    private DatabaseSettings databaseSettings;
    private GeneralSettings generalSettings;
    
    /**
     * Creates a new ConfigManager
     * @param plugin the FrizzlenEco plugin instance
     */
    public ConfigManager(FrizzlenEco plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Loads all configuration files
     */
    public void loadConfigs() {
        // Create and load main config
        loadMainConfig();
        
        // Create and load currency config
        loadCurrencyConfig();
        
        // Load settings
        loadSettings();
    }
    
    /**
     * Loads the main configuration file
     */
    private void loadMainConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        configFile = new File(plugin.getDataFolder(), "config.yml");
        
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        setConfigDefaults();
    }
    
    /**
     * Sets default values for the main config
     */
    private void setConfigDefaults() {
        // Database settings
        if (!config.contains("database.type")) {
            config.set("database.type", "sqlite");
        }
        
        if (!config.contains("database.sqlite.file")) {
            config.set("database.sqlite.file", "database.db");
        }
        
        if (!config.contains("database.mysql.host")) {
            config.set("database.mysql.host", "localhost");
        }
        
        if (!config.contains("database.mysql.port")) {
            config.set("database.mysql.port", 3306);
        }
        
        if (!config.contains("database.mysql.database")) {
            config.set("database.mysql.database", "frizzleneco");
        }
        
        if (!config.contains("database.mysql.username")) {
            config.set("database.mysql.username", "username");
        }
        
        if (!config.contains("database.mysql.password")) {
            config.set("database.mysql.password", "password");
        }
        
        if (!config.contains("database.mysql.useSSL")) {
            config.set("database.mysql.useSSL", false);
        }
        
        // General settings
        if (!config.contains("general.commandPrefix")) {
            config.set("general.commandPrefix", "&a[FrizzlenEco]&r");
        }
        
        if (!config.contains("general.balanceFormat")) {
            config.set("general.balanceFormat", "&e%currency% %amount%");
        }
        
        if (!config.contains("general.enableInterest")) {
            config.set("general.enableInterest", true);
        }
        
        if (!config.contains("general.interestInterval")) {
            config.set("general.interestInterval", 1440); // 24 hours in minutes
        }
        
        if (!config.contains("general.enableMetrics")) {
            config.set("general.enableMetrics", true);
        }
        
        if (!config.contains("general.enableUpdateChecks")) {
            config.set("general.enableUpdateChecks", true);
        }
        
        saveMainConfig();
    }
    
    /**
     * Saves the main configuration file
     */
    public void saveMainConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }
    
    /**
     * Loads the currency configuration file
     */
    private void loadCurrencyConfig() {
        currencyFile = new File(plugin.getDataFolder(), "currencies.yml");
        
        if (!currencyFile.exists()) {
            try {
                currencyFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create currencies.yml", e);
            }
        }
        
        currencyConfig = YamlConfiguration.loadConfiguration(currencyFile);
    }
    
    /**
     * Saves the currency configuration file
     */
    private void saveCurrencyConfig() {
        try {
            currencyConfig.save(currencyFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save currencies to " + currencyFile, e);
        }
    }
    
    /**
     * Loads settings from configuration files
     */
    private void loadSettings() {
        // Load database settings
        String dbType = config.getString("database.type", "sqlite");
        
        if (dbType.equalsIgnoreCase("mysql")) {
            String host = config.getString("database.mysql.host");
            int port = config.getInt("database.mysql.port");
            String database = config.getString("database.mysql.database");
            String username = config.getString("database.mysql.username");
            String password = config.getString("database.mysql.password");
            boolean useSSL = config.getBoolean("database.mysql.useSSL");
            
            databaseSettings = new DatabaseSettings(DatabaseSettings.DatabaseType.MYSQL,
                    host, port, database, username, password, useSSL, null);
        } else {
            String file = config.getString("database.sqlite.file");
            databaseSettings = new DatabaseSettings(DatabaseSettings.DatabaseType.SQLITE,
                    null, 0, null, null, null, false, new File(plugin.getDataFolder(), file));
        }
        
        // Load general settings
        String commandPrefix = config.getString("general.commandPrefix");
        String balanceFormat = config.getString("general.balanceFormat");
        boolean enableInterest = config.getBoolean("general.enableInterest");
        int interestInterval = config.getInt("general.interestInterval");
        boolean enableMetrics = config.getBoolean("general.enableMetrics");
        boolean enableUpdateChecks = config.getBoolean("general.enableUpdateChecks");
        
        generalSettings = new GeneralSettings(commandPrefix, balanceFormat, enableInterest,
                interestInterval, enableMetrics, enableUpdateChecks);
    }
    
    /**
     * Gets all currencies from the currency configuration
     * @return list of currency configurations
     */
    public List<Map<String, Object>> getCurrencyConfigs() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        if (currencyConfig == null) {
            return result;
        }
        
        ConfigurationSection section = currencyConfig.getConfigurationSection("currencies");
        if (section == null) {
            return result;
        }
        
        for (String key : section.getKeys(false)) {
            ConfigurationSection currencySection = section.getConfigurationSection(key);
            if (currencySection != null) {
                Map<String, Object> currencyMap = new HashMap<>();
                
                currencyMap.put("id", key);
                currencyMap.put("name", currencySection.getString("name", "Coin"));
                currencyMap.put("symbol", currencySection.getString("symbol", "$"));
                currencyMap.put("format", currencySection.getString("format", "%s%s"));
                currencyMap.put("decimalPlaces", currencySection.getInt("decimalPlaces", 2));
                currencyMap.put("isDefault", currencySection.getBoolean("isDefault", false));
                currencyMap.put("initialBalance", new BigDecimal(currencySection.getString("initialBalance", "100")));
                currencyMap.put("minBalance", new BigDecimal(currencySection.getString("minBalance", "0")));
                currencyMap.put("maxBalance", new BigDecimal(currencySection.getString("maxBalance", Double.toString(Double.MAX_VALUE))));
                currencyMap.put("interestRate", new BigDecimal(currencySection.getString("interestRate", "0")));
                currencyMap.put("allowNegative", currencySection.getBoolean("allowNegative", false));
                currencyMap.put("isEnabled", currencySection.getBoolean("isEnabled", true));
                
                result.add(currencyMap);
            }
        }
        
        return result;
    }
    
    /**
     * Saves a currency to the configuration
     * @param currency the currency to save
     */
    public void saveCurrency(Currency currency) {
        if (currencyConfig == null || currency == null) {
            return;
        }
        
        String path = "currencies." + currency.getId() + ".";
        currencyConfig.set(path + "name", currency.getName());
        currencyConfig.set(path + "symbol", currency.getSymbol());
        currencyConfig.set(path + "format", currency.getFormat());
        currencyConfig.set(path + "decimalPlaces", currency.getDecimalPlaces());
        currencyConfig.set(path + "isDefault", currency.isDefault());
        currencyConfig.set(path + "initialBalance", currency.getInitialBalance().toString());
        currencyConfig.set(path + "minBalance", currency.getMinBalance().toString());
        currencyConfig.set(path + "maxBalance", currency.getMaxBalance().toString());
        currencyConfig.set(path + "interestRate", currency.getInterestRate().toString());
        currencyConfig.set(path + "allowNegative", currency.isAllowNegative());
        currencyConfig.set(path + "isEnabled", currency.isEnabled());
        
        saveCurrencyConfig();
    }
    
    /**
     * Gets the database settings
     * @return the database settings
     */
    public DatabaseSettings getDatabaseSettings() {
        return databaseSettings;
    }
    
    /**
     * Gets the general settings
     * @return the general settings
     */
    public GeneralSettings getGeneralSettings() {
        return generalSettings;
    }
    
    /**
     * Gets the main configuration
     * @return the main configuration
     */
    public FileConfiguration getConfig() {
        return config;
    }
    
    /**
     * Gets the currency configuration
     * @return the currency configuration
     */
    public FileConfiguration getCurrencyConfig() {
        return currencyConfig;
    }
} 