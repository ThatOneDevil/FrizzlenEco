package org.frizzlenpop.frizzlenEco.vault;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.frizzlenpop.frizzlenEco.FrizzlenEco;
import org.frizzlenpop.frizzlenEco.economy.Currency;
import org.frizzlenpop.frizzlenEco.economy.EconomyManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Vault Economy interface.
 * Acts as a bridge between Vault and the FrizzlenEco economy system.
 */
public class VaultEconomyProvider implements Economy {
    private final FrizzlenEco plugin;
    private final EconomyManager economyManager;
    private final VaultCurrencyAdapter currencyAdapter;

    public VaultEconomyProvider(FrizzlenEco plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        this.currencyAdapter = new VaultCurrencyAdapter(plugin);
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
    public boolean hasBankSupport() {
        // FrizzlenEco no longer supports bank accounts
        return false;
    }

    @Override
    public int fractionalDigits() {
        return economyManager.getDefaultCurrency().getDecimalPlaces();
    }

    @Override
    public String format(double amount) {
        return economyManager.getDefaultCurrency().format(BigDecimal.valueOf(amount));
    }

    @Override
    public String currencyNamePlural() {
        return economyManager.getDefaultCurrency().getName() + "s";
    }

    @Override
    public String currencyNameSingular() {
        return economyManager.getDefaultCurrency().getName();
    }

    @Override
    public boolean hasAccount(String playerName) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
        return hasAccount(player);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        try {
            return economyManager.hasAccount(player.getUniqueId(), economyManager.getDefaultCurrency());
        } catch (Exception e) {
            plugin.getLogger().severe("Error checking if player has account: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        // World-specific economy is not supported, so we delegate to the standard method
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        // World-specific economy is not supported, so we delegate to the standard method
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
        return getBalance(player);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        try {
            Currency currency = currencyAdapter.getPlayerCurrency(player.getUniqueId());
            BigDecimal balance = economyManager.getBalance(player.getUniqueId(), currency);
            return balance.doubleValue();
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting player balance: " + e.getMessage());
            return 0.0;
        }
    }

    @Override
    public double getBalance(String playerName, String worldName) {
        // World-specific economy is not supported, so we delegate to the standard method
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String worldName) {
        // World-specific economy is not supported, so we delegate to the standard method
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
        return has(player, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        try {
            Currency currency = currencyAdapter.getPlayerCurrency(player.getUniqueId());
            BigDecimal balance = economyManager.getBalance(player.getUniqueId(), currency);
            return balance.compareTo(BigDecimal.valueOf(amount)) >= 0;
        } catch (Exception e) {
            plugin.getLogger().severe("Error checking if player has enough money: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        // World-specific economy is not supported, so we delegate to the standard method
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        // World-specific economy is not supported, so we delegate to the standard method
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amount");
        }
        
        try {
            BigDecimal decimalAmount = BigDecimal.valueOf(amount);
            Currency currency = currencyAdapter.getPlayerCurrency(player.getUniqueId());
            
            if (economyManager.withdraw(player.getUniqueId(), decimalAmount, currency)) {
                double newBalance = economyManager.getBalance(player.getUniqueId(), currency).doubleValue();
                return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
            } else {
                return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error withdrawing from player account: " + e.getMessage());
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Internal error: " + e.getMessage());
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        // World-specific economy is not supported, so we delegate to the standard method
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        // World-specific economy is not supported, so we delegate to the standard method
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amount");
        }
        
        try {
            BigDecimal decimalAmount = BigDecimal.valueOf(amount);
            Currency currency = currencyAdapter.getPlayerCurrency(player.getUniqueId());
            
            if (economyManager.deposit(player.getUniqueId(), decimalAmount, currency)) {
                double newBalance = economyManager.getBalance(player.getUniqueId(), currency).doubleValue();
                return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
            } else {
                return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Failed to deposit");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error depositing to player account: " + e.getMessage());
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Internal error: " + e.getMessage());
        }
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        // World-specific economy is not supported, so we delegate to the standard method
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        // World-specific economy is not supported, so we delegate to the standard method
        return depositPlayer(player, amount);
    }
    
    @Override
    public EconomyResponse createBank(String name, String playerName) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
        return createBank(name, player);
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        // Implementation depends on whether bank accounts are supported
        // This is a placeholder implementation
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank creation not implemented via Vault");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        // Implementation depends on whether bank accounts are supported
        // This is a placeholder implementation
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank deletion not implemented via Vault");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        // Implementation depends on whether bank accounts are supported
        // This is a placeholder implementation
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank balance not implemented via Vault");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        // Implementation depends on whether bank accounts are supported
        // This is a placeholder implementation
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank has amount not implemented via Vault");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        // Implementation depends on whether bank accounts are supported
        // This is a placeholder implementation
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank withdrawal not implemented via Vault");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        // Implementation depends on whether bank accounts are supported
        // This is a placeholder implementation
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank deposit not implemented via Vault");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        // Implementation depends on whether bank accounts are supported
        // This is a placeholder implementation
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank ownership not implemented via Vault");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        // Implementation depends on whether bank accounts are supported
        // This is a placeholder implementation
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank ownership not implemented via Vault");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        // Implementation depends on whether bank accounts are supported
        // This is a placeholder implementation
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank membership not implemented via Vault");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        // Implementation depends on whether bank accounts are supported
        // This is a placeholder implementation
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank membership not implemented via Vault");
    }

    @Override
    public List<String> getBanks() {
        // Implementation depends on whether bank accounts are supported
        // This is a placeholder implementation
        return new ArrayList<>();
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
        return createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        try {
            Currency currency = currencyAdapter.getPlayerCurrency(player.getUniqueId());
            return economyManager.createAccount(player.getUniqueId(), player.getName(), currency);
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating player account: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        // World-specific economy is not supported, so we delegate to the standard method
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        // World-specific economy is not supported, so we delegate to the standard method
        return createPlayerAccount(player);
    }
    
    /**
     * Gets the currency adapter used by this provider.
     * @return the currency adapter
     */
    public VaultCurrencyAdapter getCurrencyAdapter() {
        return currencyAdapter;
    }
} 