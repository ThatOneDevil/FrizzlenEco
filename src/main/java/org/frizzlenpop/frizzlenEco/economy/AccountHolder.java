package org.frizzlenpop.frizzlenEco.economy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a player's account for a specific currency
 */
public class AccountHolder {
    private final UUID playerUuid;
    private final String playerName;
    private final String currencyId;
    private BigDecimal balance;
    private Instant lastTransaction;
    private Instant created;
    
    /**
     * Creates a new account holder
     * @param playerUuid the player's UUID
     * @param playerName the player's name
     * @param currencyId the currency ID
     * @param initialBalance the initial balance
     */
    public AccountHolder(UUID playerUuid, String playerName, String currencyId, BigDecimal initialBalance) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.currencyId = currencyId;
        this.balance = initialBalance;
        this.created = Instant.now();
        this.lastTransaction = Instant.now();
    }
    
    /**
     * Gets the player's UUID
     * @return the player's UUID
     */
    public UUID getPlayerUuid() {
        return playerUuid;
    }
    
    /**
     * Gets the player's name
     * @return the player's name
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Gets the currency ID
     * @return the currency ID
     */
    public String getCurrencyId() {
        return currencyId;
    }
    
    /**
     * Gets the account balance
     * @return the account balance
     */
    public BigDecimal getBalance() {
        return balance;
    }
    
    /**
     * Sets the account balance
     * @param balance the new balance
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
        this.lastTransaction = Instant.now();
    }
    
    /**
     * Gets the time of the last transaction
     * @return the time of the last transaction
     */
    public Instant getLastTransaction() {
        return lastTransaction;
    }
    
    /**
     * Gets the time the account was created
     * @return the creation time
     */
    public Instant getCreated() {
        return created;
    }
    
    /**
     * Sets the creation time
     * @param created the creation time
     */
    public void setCreated(Instant created) {
        this.created = created;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountHolder that = (AccountHolder) o;
        return Objects.equals(playerUuid, that.playerUuid) && Objects.equals(currencyId, that.currencyId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(playerUuid, currencyId);
    }
    
    @Override
    public String toString() {
        return "AccountHolder{" +
                "playerUuid=" + playerUuid +
                ", playerName='" + playerName + '\'' +
                ", currencyId='" + currencyId + '\'' +
                ", balance=" + balance +
                '}';
    }
} 