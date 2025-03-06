package org.frizzlenpop.frizzlenEco.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.frizzlenpop.frizzlenEco.economy.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * Utility methods for the economy system
 */
public class EconomyUtil {
    
    /**
     * Formats a decimal amount with the specified number of decimal places
     * @param amount the amount to format
     * @param decimalPlaces the number of decimal places
     * @return the formatted amount
     */
    public static String formatDecimal(BigDecimal amount, int decimalPlaces) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        
        // Scale the amount to the correct number of decimal places
        amount = amount.setScale(decimalPlaces, RoundingMode.HALF_UP);
        
        // Create a formatter with the specified number of decimal places
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setMinimumFractionDigits(decimalPlaces);
        formatter.setMaximumFractionDigits(decimalPlaces);
        formatter.setGroupingUsed(true);
        
        return formatter.format(amount);
    }
    
    /**
     * Formats a currency amount according to the currency's format
     * @param amount the amount to format
     * @param currency the currency to use for formatting
     * @return the formatted amount
     */
    public static String formatCurrency(BigDecimal amount, Currency currency) {
        if (currency == null) {
            return formatDecimal(amount, 2);
        }
        
        return currency.format(amount);
    }
    
    /**
     * Gets a player by name or UUID
     * @param nameOrUuid the player's name or UUID
     * @return the player, or null if not found
     */
    public static OfflinePlayer getPlayer(String nameOrUuid) {
        // Try to parse as UUID first
        try {
            UUID uuid = UUID.fromString(nameOrUuid);
            return Bukkit.getOfflinePlayer(uuid);
        } catch (IllegalArgumentException e) {
            // Not a UUID, try as player name
            OfflinePlayer player = Bukkit.getPlayerExact(nameOrUuid);
            
            if (player == null) {
                // Try to find offline player by name
                player = Bukkit.getOfflinePlayers()[0];
                for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                    if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(nameOrUuid)) {
                        return offlinePlayer;
                    }
                }
            }
            
            return player;
        }
    }
    
    /**
     * Parses a string to a BigDecimal
     * @param amount the amount string
     * @return the parsed amount, or null if invalid
     */
    public static BigDecimal parseAmount(String amount) {
        try {
            return new BigDecimal(amount);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Checks if a string is a valid amount
     * @param amount the amount string
     * @return true if the string is a valid amount
     */
    public static boolean isValidAmount(String amount) {
        return parseAmount(amount) != null;
    }
    
    /**
     * Checks if an amount is positive
     * @param amount the amount to check
     * @return true if the amount is positive
     */
    public static boolean isPositiveAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Checks if an amount is non-negative
     * @param amount the amount to check
     * @return true if the amount is non-negative
     */
    public static boolean isNonNegativeAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) >= 0;
    }
} 