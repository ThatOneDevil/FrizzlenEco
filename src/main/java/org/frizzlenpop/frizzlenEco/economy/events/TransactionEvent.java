package org.frizzlenpop.frizzlenEco.economy.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.frizzlenpop.frizzlenEco.economy.Currency;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event that is called when a transaction occurs
 */
public class TransactionEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    
    /**
     * Type of transaction
     */
    public enum Type {
        DEPOSIT,
        WITHDRAW,
        TRANSFER
    }
    
    private final Type type;
    private final UUID fromUuid;
    private final UUID toUuid;
    private final Currency currency;
    private final BigDecimal amount;
    
    /**
     * Creates a new transaction event
     * @param type the transaction type
     * @param fromUuid the UUID of the player money is taken from (can be null for deposits)
     * @param toUuid the UUID of the player money is given to (can be null for withdrawals)
     * @param currency the currency involved
     * @param amount the amount of the transaction
     */
    public TransactionEvent(Type type, UUID fromUuid, UUID toUuid, Currency currency, BigDecimal amount) {
        this.type = type;
        this.fromUuid = fromUuid;
        this.toUuid = toUuid;
        this.currency = currency;
        this.amount = amount;
    }
    
    /**
     * Gets the transaction type
     * @return the transaction type
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Gets the UUID of the player money is taken from
     * @return the player UUID, or null for deposits
     */
    public UUID getFromUuid() {
        return fromUuid;
    }
    
    /**
     * Gets the UUID of the player money is given to
     * @return the player UUID, or null for withdrawals
     */
    public UUID getToUuid() {
        return toUuid;
    }
    
    /**
     * Gets the currency involved in the transaction
     * @return the currency
     */
    public Currency getCurrency() {
        return currency;
    }
    
    /**
     * Gets the amount of the transaction
     * @return the amount
     */
    public BigDecimal getAmount() {
        return amount;
    }
    
    /**
     * Checks if this is a deposit transaction
     * @return true if this is a deposit
     */
    public boolean isDeposit() {
        return type == Type.DEPOSIT;
    }
    
    /**
     * Checks if this is a withdrawal transaction
     * @return true if this is a withdrawal
     */
    public boolean isWithdraw() {
        return type == Type.WITHDRAW;
    }
    
    /**
     * Checks if this is a transfer transaction
     * @return true if this is a transfer
     */
    public boolean isTransfer() {
        return type == Type.TRANSFER;
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
} 