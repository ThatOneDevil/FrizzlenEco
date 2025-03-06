package org.frizzlenpop.frizzlenEco.economy.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.frizzlenpop.frizzlenEco.economy.Currency;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event that is called when a player's balance changes
 */
public class BalanceChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final UUID playerUuid;
    private final Currency currency;
    private final BigDecimal oldBalance;
    private final BigDecimal newBalance;
    
    /**
     * Creates a new balance change event
     * @param playerUuid the player's UUID
     * @param currency the currency that changed
     * @param oldBalance the old balance
     * @param newBalance the new balance
     */
    public BalanceChangeEvent(UUID playerUuid, Currency currency, BigDecimal oldBalance, BigDecimal newBalance) {
        this.playerUuid = playerUuid;
        this.currency = currency;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
    }
    
    /**
     * Gets the player's UUID
     * @return the player's UUID
     */
    public UUID getPlayerUuid() {
        return playerUuid;
    }
    
    /**
     * Gets the currency that changed
     * @return the currency
     */
    public Currency getCurrency() {
        return currency;
    }
    
    /**
     * Gets the old balance
     * @return the old balance
     */
    public BigDecimal getOldBalance() {
        return oldBalance;
    }
    
    /**
     * Gets the new balance
     * @return the new balance
     */
    public BigDecimal getNewBalance() {
        return newBalance;
    }
    
    /**
     * Gets the difference between the old and new balance
     * @return the difference (can be negative)
     */
    public BigDecimal getDifference() {
        return newBalance.subtract(oldBalance);
    }
    
    /**
     * Checks if the balance increased
     * @return true if the balance increased
     */
    public boolean isIncrease() {
        return newBalance.compareTo(oldBalance) > 0;
    }
    
    /**
     * Checks if the balance decreased
     * @return true if the balance decreased
     */
    public boolean isDecrease() {
        return newBalance.compareTo(oldBalance) < 0;
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
} 