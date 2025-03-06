package org.frizzlenpop.frizzlenEco.economy;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a currency in the economy
 */
public class Currency {
    private final String id;
    private final String name;
    private final String symbol;
    private final String format;
    private final int decimalPlaces;
    private final boolean isDefault;
    private final BigDecimal initialBalance;
    private final BigDecimal minBalance;
    private final BigDecimal maxBalance;
    private final BigDecimal interestRate;
    private final boolean allowNegative;
    private final boolean isEnabled;
    
    /**
     * Creates a new Currency with the specified parameters
     * 
     * @param id unique identifier for this currency
     * @param name display name of the currency
     * @param symbol currency symbol (e.g. $)
     * @param format format string for displaying amounts (e.g. "%s%d")
     * @param decimalPlaces number of decimal places to display
     * @param isDefault whether this is the default currency
     * @param initialBalance balance given to new accounts
     * @param minBalance minimum allowed balance
     * @param maxBalance maximum allowed balance
     * @param interestRate interest rate for this currency
     * @param allowNegative whether negative balances are allowed
     * @param isEnabled whether this currency is enabled
     */
    public Currency(String id, String name, String symbol, String format, int decimalPlaces,
                   boolean isDefault, BigDecimal initialBalance, BigDecimal minBalance, 
                   BigDecimal maxBalance, BigDecimal interestRate, boolean allowNegative, boolean isEnabled) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.format = format;
        this.decimalPlaces = decimalPlaces;
        this.isDefault = isDefault;
        this.initialBalance = initialBalance;
        this.minBalance = minBalance;
        this.maxBalance = maxBalance;
        this.interestRate = interestRate;
        this.allowNegative = allowNegative;
        this.isEnabled = isEnabled;
    }
    
    /**
     * Creates a new Currency builder
     * @return a new CurrencyBuilder
     */
    public static CurrencyBuilder builder() {
        return new CurrencyBuilder();
    }
    
    /**
     * Gets the unique identifier for this currency
     * @return the currency ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the display name of the currency
     * @return the currency name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the symbol for this currency (e.g. $)
     * @return the currency symbol
     */
    public String getSymbol() {
        return symbol;
    }
    
    /**
     * Gets the format string for displaying amounts
     * @return the format string
     */
    public String getFormat() {
        return format;
    }
    
    /**
     * Gets the number of decimal places to display
     * @return the number of decimal places
     */
    public int getDecimalPlaces() {
        return decimalPlaces;
    }
    
    /**
     * Checks if this is the default currency
     * @return true if this is the default currency
     */
    public boolean isDefault() {
        return isDefault;
    }
    
    /**
     * Gets the initial balance for new accounts
     * @return the initial balance
     */
    public BigDecimal getInitialBalance() {
        return initialBalance;
    }
    
    /**
     * Gets the minimum allowed balance
     * @return the minimum balance
     */
    public BigDecimal getMinBalance() {
        return minBalance;
    }
    
    /**
     * Gets the maximum allowed balance
     * @return the maximum balance
     */
    public BigDecimal getMaxBalance() {
        return maxBalance;
    }
    
    /**
     * Gets the interest rate for this currency
     * @return the interest rate
     */
    public BigDecimal getInterestRate() {
        return interestRate;
    }
    
    /**
     * Checks if negative balances are allowed
     * @return true if negative balances are allowed
     */
    public boolean isAllowNegative() {
        return allowNegative;
    }
    
    /**
     * Checks if this currency is enabled
     * @return true if this currency is enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Formats the given amount according to this currency's format
     * @param amount the amount to format
     * @return the formatted amount
     */
    public String format(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        
        // Scale the amount to the correct number of decimal places
        amount = amount.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
        
        // Format according to the format string
        return String.format(format, symbol, amount);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Currency currency = (Currency) o;
        return Objects.equals(id, currency.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return name + " (" + symbol + ")";
    }
    
    /**
     * Builder class for Currency
     */
    public static class CurrencyBuilder {
        private String id = UUID.randomUUID().toString();
        private String name = "Coins";
        private String symbol = "$";
        private String format = "%s%s";
        private int decimalPlaces = 2;
        private boolean isDefault = false;
        private BigDecimal initialBalance = BigDecimal.valueOf(100);
        private BigDecimal minBalance = BigDecimal.ZERO;
        private BigDecimal maxBalance = BigDecimal.valueOf(Double.MAX_VALUE);
        private BigDecimal interestRate = BigDecimal.ZERO;
        private boolean allowNegative = false;
        private boolean isEnabled = true;
        
        public CurrencyBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public CurrencyBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public CurrencyBuilder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }
        
        public CurrencyBuilder format(String format) {
            this.format = format;
            return this;
        }
        
        public CurrencyBuilder decimalPlaces(int decimalPlaces) {
            this.decimalPlaces = decimalPlaces;
            return this;
        }
        
        public CurrencyBuilder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }
        
        public CurrencyBuilder initialBalance(BigDecimal initialBalance) {
            this.initialBalance = initialBalance;
            return this;
        }
        
        public CurrencyBuilder minBalance(BigDecimal minBalance) {
            this.minBalance = minBalance;
            return this;
        }
        
        public CurrencyBuilder maxBalance(BigDecimal maxBalance) {
            this.maxBalance = maxBalance;
            return this;
        }
        
        public CurrencyBuilder interestRate(BigDecimal interestRate) {
            this.interestRate = interestRate;
            return this;
        }
        
        public CurrencyBuilder allowNegative(boolean allowNegative) {
            this.allowNegative = allowNegative;
            return this;
        }
        
        public CurrencyBuilder isEnabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }
        
        public Currency build() {
            return new Currency(id, name, symbol, format, decimalPlaces, isDefault, 
                              initialBalance, minBalance, maxBalance, interestRate, allowNegative, isEnabled);
        }
    }
} 