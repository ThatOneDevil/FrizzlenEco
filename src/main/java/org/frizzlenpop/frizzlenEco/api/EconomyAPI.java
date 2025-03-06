package org.frizzlenpop.frizzlenEco.api;

import org.bukkit.OfflinePlayer;
import org.frizzlenpop.frizzlenEco.FrizzlenEco;
import org.frizzlenpop.frizzlenEco.economy.Currency;
import org.frizzlenpop.frizzlenEco.economy.EconomyManager;

import java.math.BigDecimal;
import java.util.logging.Level;

/**
 * Implementation of the EconomyProvider interface
 * Serves as the bridge between other plugins and the FrizzlenEco economy system
 */
public class EconomyAPI implements EconomyProvider {
    private final FrizzlenEco plugin;
    private final EconomyManager economyManager;
    
    /**
     * Creates a new EconomyAPI instance
     * @param plugin the FrizzlenEco plugin instance
     */
    public EconomyAPI(FrizzlenEco plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
    }
    
    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }
    
    @Override
    public String getName() {
        return "FrizzlenEco";
    }
    
    @Override
    public Currency getDefaultCurrency() {
        return economyManager.getDefaultCurrency();
    }
    
    @Override
    public Currency[] getCurrencies() {
        return economyManager.getCurrencies().toArray(new Currency[0]);
    }
    
    @Override
    public String format(BigDecimal amount) {
        return format(amount, getDefaultCurrency());
    }
    
    @Override
    public String format(BigDecimal amount, Currency currency) {
        return currency.format(amount);
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return hasAccount(player, getDefaultCurrency());
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer player, Currency currency) {
        try {
            return economyManager.hasAccount(player.getUniqueId(), currency);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking if player has account", e);
            return false;
        }
    }
    
    @Override
    public BigDecimal getBalance(OfflinePlayer player) {
        return getBalance(player, getDefaultCurrency());
    }
    
    @Override
    public BigDecimal getBalance(OfflinePlayer player, Currency currency) {
        try {
            return economyManager.getBalance(player.getUniqueId(), currency);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting player balance", e);
            return BigDecimal.ZERO;
        }
    }
    
    @Override
    public boolean has(OfflinePlayer player, BigDecimal amount) {
        return has(player, amount, getDefaultCurrency());
    }
    
    @Override
    public boolean has(OfflinePlayer player, BigDecimal amount, Currency currency) {
        try {
            BigDecimal balance = getBalance(player, currency);
            return balance.compareTo(amount) >= 0;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking if player has enough money", e);
            return false;
        }
    }
    
    @Override
    public boolean withdraw(OfflinePlayer player, BigDecimal amount) {
        return withdraw(player, amount, getDefaultCurrency());
    }
    
    @Override
    public boolean withdraw(OfflinePlayer player, BigDecimal amount, Currency currency) {
        try {
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                plugin.getLogger().warning("Attempted to withdraw negative amount: " + amount);
                return false;
            }
            
            return economyManager.withdraw(player.getUniqueId(), amount, currency);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error withdrawing money from player", e);
            return false;
        }
    }
    
    @Override
    public boolean deposit(OfflinePlayer player, BigDecimal amount) {
        return deposit(player, amount, getDefaultCurrency());
    }
    
    @Override
    public boolean deposit(OfflinePlayer player, BigDecimal amount, Currency currency) {
        try {
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                plugin.getLogger().warning("Attempted to deposit negative amount: " + amount);
                return false;
            }
            
            return economyManager.deposit(player.getUniqueId(), amount, currency);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error depositing money to player", e);
            return false;
        }
    }
    
    @Override
    public boolean createAccount(OfflinePlayer player) {
        return createAccount(player, getDefaultCurrency());
    }
    
    @Override
    public boolean createAccount(OfflinePlayer player, Currency currency) {
        try {
            return economyManager.createAccount(player.getUniqueId(), player.getName(), currency);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error creating account for player", e);
            return false;
        }
    }
    
    @Override
    public boolean transfer(OfflinePlayer from, OfflinePlayer to, BigDecimal amount) {
        return transfer(from, to, amount, getDefaultCurrency());
    }
    
    @Override
    public boolean transfer(OfflinePlayer from, OfflinePlayer to, BigDecimal amount, Currency currency) {
        try {
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                plugin.getLogger().warning("Attempted to transfer negative amount: " + amount);
                return false;
            }
            
            return economyManager.transfer(from.getUniqueId(), to.getUniqueId(), amount, currency);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error transferring money between players", e);
            return false;
        }
    }
} 