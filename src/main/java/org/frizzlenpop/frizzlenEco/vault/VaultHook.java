package org.frizzlenpop.frizzlenEco.vault;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.frizzlenpop.frizzlenEco.FrizzlenEco;

/**
 * Handles the integration with the Vault plugin.
 */
public class VaultHook {
    private final FrizzlenEco plugin;
    private VaultEconomyProvider economyProvider;
    private boolean hooked = false;

    /**
     * Creates a new VaultHook instance.
     * @param plugin The FrizzlenEco plugin instance
     */
    public VaultHook(FrizzlenEco plugin) {
        this.plugin = plugin;
    }

    /**
     * Hooks into Vault by registering our economy provider.
     * @return true if successfully hooked, false otherwise
     */
    public boolean hook() {
        if (hooked) {
            return true;
        }

        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found! Vault integration disabled.");
            return false;
        }

        try {
            // Register our economy provider with Vault
            economyProvider = new VaultEconomyProvider(plugin);
            Bukkit.getServicesManager().register(Economy.class, economyProvider, plugin, ServicePriority.Highest);
            hooked = true;
            plugin.getLogger().info("Successfully hooked into Vault!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to hook into Vault: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Unhooks from Vault by unregistering our economy provider.
     */
    public void unhook() {
        if (!hooked) {
            return;
        }

        try {
            Bukkit.getServicesManager().unregister(Economy.class, economyProvider);
            hooked = false;
            plugin.getLogger().info("Successfully unhooked from Vault!");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to unhook from Vault: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if the plugin is currently hooked into Vault.
     * @return true if hooked, false otherwise
     */
    public boolean isHooked() {
        return hooked;
    }

    /**
     * Gets the Vault economy provider instance.
     * @return the economy provider
     */
    public VaultEconomyProvider getEconomyProvider() {
        return economyProvider;
    }

    /**
     * Gets the currency adapter used by the economy provider.
     * @return the currency adapter, or null if not hooked
     */
    public VaultCurrencyAdapter getCurrencyAdapter() {
        return economyProvider != null ? economyProvider.getCurrencyAdapter() : null;
    }
} 