package org.frizzlenpop.frizzlenEco.economy;

import org.bukkit.Bukkit;
import org.frizzlenpop.frizzlenEco.FrizzlenEco;
import org.frizzlenpop.frizzlenEco.config.ConfigManager;
import org.frizzlenpop.frizzlenEco.database.DatabaseManager;
import org.frizzlenpop.frizzlenEco.economy.events.BalanceChangeEvent;
import org.frizzlenpop.frizzlenEco.economy.events.TransactionEvent;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages all economy-related operations in the plugin
 */
public class EconomyManager {
    private final FrizzlenEco plugin;
    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;
    
    private final Map<String, Currency> currencies = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, AccountHolder>> accounts = new ConcurrentHashMap<>();
    
    private Currency defaultCurrency;
    private boolean initialized = false;
    
    /**
     * Creates a new EconomyManager
     * @param plugin the FrizzlenEco plugin instance
     */
    public EconomyManager(FrizzlenEco plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.configManager = plugin.getConfigManager();
    }
    
    /**
     * Initializes the economy system
     * @return true if initialization was successful
     */
    public boolean initialize() {
        if (initialized) {
            return true;
        }
        
        try {
            // Load currencies from config
            loadCurrencies();
            
            // Load account data from database
            loadAccounts();
            
            initialized = true;
            plugin.getLogger().info("Economy system initialized successfully");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize economy system", e);
            return false;
        }
    }
    
    /**
     * Shuts down the economy system and saves all data
     */
    public void shutdown() {
        try {
            saveAllData();
            plugin.getLogger().info("Economy data saved successfully");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save economy data", e);
        }
    }
    
    /**
     * Loads all currencies from configuration
     */
    private void loadCurrencies() {
        // Clear existing currencies
        currencies.clear();
        defaultCurrency = null;
        
        // Load from config
        List<Map<String, Object>> currencyConfigs = configManager.getCurrencyConfigs();
        
        if (currencyConfigs.isEmpty()) {
            // Create default currency if none exist
            Currency defaultCoin = Currency.builder()
                    .id("coin")
                    .name("Coin")
                    .symbol("$")
                    .format("%s%s")
                    .decimalPlaces(2)
                    .isDefault(true)
                    .initialBalance(BigDecimal.valueOf(100))
                    .build();
            
            currencies.put(defaultCoin.getId(), defaultCoin);
            defaultCurrency = defaultCoin;
            
            // Save default currency to config
            configManager.saveCurrency(defaultCoin);
        } else {
            // Load currencies from config
            for (Map<String, Object> currencyConfig : currencyConfigs) {
                try {
                    String id = (String) currencyConfig.get("id");
                    String name = (String) currencyConfig.get("name");
                    String symbol = (String) currencyConfig.get("symbol");
                    String format = (String) currencyConfig.get("format");
                    int decimalPlaces = (int) currencyConfig.get("decimalPlaces");
                    boolean isDefault = (boolean) currencyConfig.get("isDefault");
                    BigDecimal initialBalance = new BigDecimal(currencyConfig.get("initialBalance").toString());
                    BigDecimal minBalance = new BigDecimal(currencyConfig.get("minBalance").toString());
                    BigDecimal maxBalance = new BigDecimal(currencyConfig.get("maxBalance").toString());
                    BigDecimal interestRate = new BigDecimal(currencyConfig.get("interestRate").toString());
                    boolean allowNegative = (boolean) currencyConfig.get("allowNegative");
                    boolean isEnabled = (boolean) currencyConfig.get("isEnabled");
                    
                    Currency currency = new Currency(id, name, symbol, format, decimalPlaces,
                            isDefault, initialBalance, minBalance, maxBalance, interestRate,
                            allowNegative, isEnabled);
                    
                    currencies.put(currency.getId(), currency);
                    
                    if (isDefault) {
                        defaultCurrency = currency;
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load currency from config", e);
                }
            }
            
            // Ensure there's a default currency
            if (defaultCurrency == null && !currencies.isEmpty()) {
                defaultCurrency = currencies.values().iterator().next();
                defaultCurrency = new Currency(defaultCurrency.getId(), defaultCurrency.getName(),
                        defaultCurrency.getSymbol(), defaultCurrency.getFormat(),
                        defaultCurrency.getDecimalPlaces(), true, defaultCurrency.getInitialBalance(),
                        defaultCurrency.getMinBalance(), defaultCurrency.getMaxBalance(),
                        defaultCurrency.getInterestRate(), defaultCurrency.isAllowNegative(),
                        defaultCurrency.isEnabled());
                currencies.put(defaultCurrency.getId(), defaultCurrency);
                
                // Save updated default currency to config
                configManager.saveCurrency(defaultCurrency);
            }
        }
        
        plugin.getLogger().info("Loaded " + currencies.size() + " currencies");
    }
    
    /**
     * Loads all account data from the database
     */
    private void loadAccounts() {
        // Clear existing accounts
        accounts.clear();
        
        // Load from database
        Map<UUID, Map<String, AccountHolder>> loadedAccounts = databaseManager.loadAllAccounts();
        if (loadedAccounts != null) {
            accounts.putAll(loadedAccounts);
        }
        
        plugin.getLogger().info("Loaded " + accounts.size() + " player accounts");
    }
    
    /**
     * Saves all economy data to the database
     */
    private void saveAllData() {
        databaseManager.saveAllAccounts(accounts);
    }
    
    /**
     * Gets the default currency
     * @return the default currency
     */
    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }
    
    /**
     * Gets all currencies
     * @return collection of all currencies
     */
    public Collection<Currency> getCurrencies() {
        return Collections.unmodifiableCollection(currencies.values());
    }
    
    /**
     * Gets a currency by its ID
     * @param currencyId the currency ID
     * @return the currency, or null if not found
     */
    public Currency getCurrency(String currencyId) {
        return currencies.get(currencyId);
    }
    
    /**
     * Checks if a player has an account
     * @param playerUuid the player's UUID
     * @param currency the currency to check for
     * @return true if the player has an account
     */
    public boolean hasAccount(UUID playerUuid, Currency currency) {
        Map<String, AccountHolder> playerAccounts = accounts.get(playerUuid);
        if (playerAccounts == null) {
            return false;
        }
        
        return playerAccounts.containsKey(currency.getId());
    }
    
    /**
     * Gets a player's balance
     * @param playerUuid the player's UUID
     * @param currency the currency to get balance for
     * @return the player's balance, or 0 if they don't have an account
     */
    public BigDecimal getBalance(UUID playerUuid, Currency currency) {
        Map<String, AccountHolder> playerAccounts = accounts.get(playerUuid);
        if (playerAccounts == null) {
            return BigDecimal.ZERO;
        }
        
        AccountHolder account = playerAccounts.get(currency.getId());
        if (account == null) {
            return BigDecimal.ZERO;
        }
        
        return account.getBalance();
    }
    
    /**
     * Checks if a player has at least the specified amount
     * @param playerUuid the player's UUID
     * @param amount the amount to check for
     * @param currency the currency to check in
     * @return true if the player has at least the amount
     */
    public boolean has(UUID playerUuid, BigDecimal amount, Currency currency) {
        BigDecimal balance = getBalance(playerUuid, currency);
        return balance.compareTo(amount) >= 0;
    }
    
    /**
     * Creates a new account for a player
     * @param playerUuid the player's UUID
     * @param playerName the player's name
     * @param currency the currency to create account for
     * @return true if the account was created successfully
     */
    public boolean createAccount(UUID playerUuid, String playerName, Currency currency) {
        if (playerUuid == null || currency == null) {
            return false;
        }
        
        // Get or create player's account map
        Map<String, AccountHolder> playerAccounts = accounts.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>());
        
        // Check if account already exists
        if (playerAccounts.containsKey(currency.getId())) {
            return true; // Account already exists
        }
        
        // Create new account
        AccountHolder account = new AccountHolder(playerUuid, playerName, currency.getId(), currency.getInitialBalance());
        playerAccounts.put(currency.getId(), account);
        
        // Save to database
        databaseManager.saveAccount(account);
        
        // Fire event
        Bukkit.getPluginManager().callEvent(new BalanceChangeEvent(playerUuid, currency, BigDecimal.ZERO, currency.getInitialBalance()));
        
        return true;
    }
    
    /**
     * Withdraws money from a player's account
     * @param playerUuid the player's UUID
     * @param amount the amount to withdraw
     * @param currency the currency to withdraw from
     * @return true if the withdrawal was successful
     */
    public boolean withdraw(UUID playerUuid, BigDecimal amount, Currency currency) {
        if (playerUuid == null || amount == null || currency == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Get player's account map
        Map<String, AccountHolder> playerAccounts = accounts.get(playerUuid);
        if (playerAccounts == null) {
            return false;
        }
        
        // Get account for this currency
        AccountHolder account = playerAccounts.get(currency.getId());
        if (account == null) {
            return false;
        }
        
        // Check if player has enough money
        BigDecimal currentBalance = account.getBalance();
        BigDecimal newBalance = currentBalance.subtract(amount);
        
        if (!currency.isAllowNegative() && newBalance.compareTo(currency.getMinBalance()) < 0) {
            return false; // Not enough money
        }
        
        // Update balance
        account.setBalance(newBalance);
        
        // Save to database
        databaseManager.saveAccount(account);
        
        // Fire event
        Bukkit.getPluginManager().callEvent(new BalanceChangeEvent(playerUuid, currency, currentBalance, newBalance));
        Bukkit.getPluginManager().callEvent(new TransactionEvent(TransactionEvent.Type.WITHDRAW, playerUuid, null, currency, amount));
        
        return true;
    }
    
    /**
     * Deposits money into a player's account
     * @param playerUuid the player's UUID
     * @param amount the amount to deposit
     * @param currency the currency to deposit to
     * @return true if the deposit was successful
     */
    public boolean deposit(UUID playerUuid, BigDecimal amount, Currency currency) {
        if (playerUuid == null || amount == null || currency == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Get or create player's account map
        Map<String, AccountHolder> playerAccounts = accounts.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>());
        
        // Get or create account for this currency
        AccountHolder account = playerAccounts.get(currency.getId());
        if (account == null) {
            // Player doesn't have an account for this currency, create one
            String playerName = Bukkit.getOfflinePlayer(playerUuid).getName();
            account = new AccountHolder(playerUuid, playerName, currency.getId(), BigDecimal.ZERO);
            playerAccounts.put(currency.getId(), account);
        }
        
        // Check if new balance would exceed maximum
        BigDecimal currentBalance = account.getBalance();
        BigDecimal newBalance = currentBalance.add(amount);
        
        if (newBalance.compareTo(currency.getMaxBalance()) > 0) {
            return false; // Would exceed maximum balance
        }
        
        // Update balance
        account.setBalance(newBalance);
        
        // Save to database
        databaseManager.saveAccount(account);
        
        // Fire event
        Bukkit.getPluginManager().callEvent(new BalanceChangeEvent(playerUuid, currency, currentBalance, newBalance));
        Bukkit.getPluginManager().callEvent(new TransactionEvent(TransactionEvent.Type.DEPOSIT, null, playerUuid, currency, amount));
        
        return true;
    }
    
    /**
     * Transfers money from one player to another
     * @param fromUuid the UUID of the player to take money from
     * @param toUuid the UUID of the player to give money to
     * @param amount the amount to transfer
     * @param currency the currency to transfer
     * @return true if the transfer was successful
     */
    public boolean transfer(UUID fromUuid, UUID toUuid, BigDecimal amount, Currency currency) {
        if (fromUuid == null || toUuid == null || amount == null || currency == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Cannot transfer to self
        if (fromUuid.equals(toUuid)) {
            return false;
        }
        
        // Withdraw from source account
        boolean withdrawSuccess = withdraw(fromUuid, amount, currency);
        if (!withdrawSuccess) {
            return false;
        }
        
        // Deposit to target account
        boolean depositSuccess = deposit(toUuid, amount, currency);
        if (!depositSuccess) {
            // Rollback the withdrawal
            deposit(fromUuid, amount, currency);
            return false;
        }
        
        // Fire transfer event
        Bukkit.getPluginManager().callEvent(new TransactionEvent(TransactionEvent.Type.TRANSFER, fromUuid, toUuid, currency, amount));
        
        return true;
    }
    
    /**
     * Gets all accounts for a player
     * @param playerUuid the player's UUID
     * @return map of currency IDs to account holders
     */
    public Map<String, AccountHolder> getPlayerAccounts(UUID playerUuid) {
        return accounts.getOrDefault(playerUuid, Collections.emptyMap());
    }
} 