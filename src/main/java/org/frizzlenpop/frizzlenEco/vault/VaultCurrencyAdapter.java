package org.frizzlenpop.frizzlenEco.vault;

import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenEco.FrizzlenEco;
import org.frizzlenpop.frizzlenEco.economy.Currency;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class to help maintain state about which currency a player is using
 * when interacting through Vault.
 * This allows plugins using Vault to indirectly work with a multi-currency system.
 */
public class VaultCurrencyAdapter {
    private final FrizzlenEco plugin;
    private final Map<UUID, Currency> playerCurrencyPreferences;
    
    /**
     * Creates a new VaultCurrencyAdapter instance.
     * @param plugin The FrizzlenEco plugin instance
     */
    public VaultCurrencyAdapter(FrizzlenEco plugin) {
        this.plugin = plugin;
        this.playerCurrencyPreferences = new HashMap<>();
    }
    
    /**
     * Gets the currency a player is currently using for Vault operations.
     * If no preference is set, returns the default currency.
     * 
     * @param playerUuid The UUID of the player
     * @return The player's preferred currency
     */
    public Currency getPlayerCurrency(UUID playerUuid) {
        return playerCurrencyPreferences.getOrDefault(playerUuid, 
            plugin.getEconomyManager().getDefaultCurrency());
    }
    
    /**
     * Sets a player's preferred currency for Vault operations.
     * 
     * @param playerUuid The UUID of the player
     * @param currency The currency to use
     */
    public void setPlayerCurrency(UUID playerUuid, Currency currency) {
        playerCurrencyPreferences.put(playerUuid, currency);
    }
    
    /**
     * Resets a player's currency preference to the default.
     * 
     * @param playerUuid The UUID of the player
     */
    public void resetPlayerCurrency(UUID playerUuid) {
        playerCurrencyPreferences.remove(playerUuid);
    }
    
    /**
     * Gets the currency for a player by ID.
     * If the currency doesn't exist, returns the default currency.
     * 
     * @param currencyId The ID of the currency
     * @return The currency, or the default currency if not found
     */
    public Currency getCurrencyById(String currencyId) {
        for (Currency currency : plugin.getEconomyManager().getCurrencies()) {
            if (currency.getId().equals(currencyId)) {
                return currency;
            }
        }
        return plugin.getEconomyManager().getDefaultCurrency();
    }
    
    /**
     * Clears all player preferences.
     * Useful when reloading the plugin.
     */
    public void clearPreferences() {
        playerCurrencyPreferences.clear();
    }
} 