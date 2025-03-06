package org.frizzlenpop.frizzlenEco.api;

import org.bukkit.OfflinePlayer;
import org.frizzlenpop.frizzlenEco.economy.Currency;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Interface for Economy Service Provider
 * This interface defines methods that allow other plugins to interact with the economy
 */
public interface EconomyProvider {
    
    /**
     * Checks if the economy service is enabled
     * @return true if the economy service is enabled
     */
    boolean isEnabled();
    
    /**
     * Gets the name of the economy implementation
     * @return name of the economy implementation
     */
    String getName();
    
    /**
     * Returns the default currency used by this economy implementation
     * @return the default Currency
     */
    Currency getDefaultCurrency();
    
    /**
     * Returns all available currencies
     * @return array of all currencies
     */
    Currency[] getCurrencies();
    
    /**
     * Formats the amount with the default currency symbol
     * @param amount to format
     * @return formatted string
     */
    String format(BigDecimal amount);
    
    /**
     * Formats the amount with the specified currency symbol
     * @param amount to format
     * @param currency to use for formatting
     * @return formatted string
     */
    String format(BigDecimal amount, Currency currency);
    
    /**
     * Checks if the player has an account
     * @param player to check
     * @return true if the player has an account
     */
    boolean hasAccount(OfflinePlayer player);
    
    /**
     * Checks if the player has an account for the specified currency
     * @param player to check
     * @param currency to check for
     * @return true if the player has an account for this currency
     */
    boolean hasAccount(OfflinePlayer player, Currency currency);
    
    /**
     * Gets balance of a player's account
     * @param player of the account
     * @return amount in the account
     */
    BigDecimal getBalance(OfflinePlayer player);
    
    /**
     * Gets balance of a player's account in the specified currency
     * @param player of the account
     * @param currency to get balance in
     * @return amount in the account
     */
    BigDecimal getBalance(OfflinePlayer player, Currency currency);
    
    /**
     * Checks if the player has at least the specified amount
     * @param player to check
     * @param amount to check for
     * @return true if player has at least the amount
     */
    boolean has(OfflinePlayer player, BigDecimal amount);
    
    /**
     * Checks if the player has at least the specified amount in the specified currency
     * @param player to check
     * @param amount to check for
     * @param currency to check in
     * @return true if player has at least the amount
     */
    boolean has(OfflinePlayer player, BigDecimal amount, Currency currency);
    
    /**
     * Withdraw an amount from a player's account
     * @param player to withdraw from
     * @param amount to withdraw
     * @return true if successful
     */
    boolean withdraw(OfflinePlayer player, BigDecimal amount);
    
    /**
     * Withdraw an amount from a player's account in the specified currency
     * @param player to withdraw from
     * @param amount to withdraw
     * @param currency to withdraw from
     * @return true if successful
     */
    boolean withdraw(OfflinePlayer player, BigDecimal amount, Currency currency);
    
    /**
     * Deposit an amount to a player's account
     * @param player to deposit to
     * @param amount to deposit
     * @return true if successful
     */
    boolean deposit(OfflinePlayer player, BigDecimal amount);
    
    /**
     * Deposit an amount to a player's account in the specified currency
     * @param player to deposit to
     * @param amount to deposit
     * @param currency to deposit to
     * @return true if successful
     */
    boolean deposit(OfflinePlayer player, BigDecimal amount, Currency currency);
    
    /**
     * Create a player account
     * @param player to create account for
     * @return true if successful
     */
    boolean createAccount(OfflinePlayer player);
    
    /**
     * Create a player account with a specific currency
     * @param player to create account for
     * @param currency to create account with
     * @return true if successful
     */
    boolean createAccount(OfflinePlayer player, Currency currency);
    
    /**
     * Transfer money from one player to another
     * @param from player to take from
     * @param to player to give to
     * @param amount to transfer
     * @return true if successful
     */
    boolean transfer(OfflinePlayer from, OfflinePlayer to, BigDecimal amount);
    
    /**
     * Transfer money from one player to another in a specific currency
     * @param from player to take from
     * @param to player to give to
     * @param amount to transfer
     * @param currency to use
     * @return true if successful
     */
    boolean transfer(OfflinePlayer from, OfflinePlayer to, BigDecimal amount, Currency currency);
} 