package org.frizzlenpop.frizzlenEco.config;

/**
 * Stores general plugin settings
 */
public class GeneralSettings {
    private final String commandPrefix;
    private final String balanceFormat;
    private final boolean enableInterest;
    private final int interestInterval;
    private final boolean enableMetrics;
    private final boolean enableUpdateChecks;
    
    /**
     * Creates new general settings
     * @param commandPrefix the prefix for command messages
     * @param balanceFormat the format for balance messages
     * @param enableInterest whether interest is enabled
     * @param interestInterval the interval for interest in minutes
     * @param enableMetrics whether metrics are enabled
     * @param enableUpdateChecks whether update checks are enabled
     */
    public GeneralSettings(String commandPrefix, String balanceFormat, boolean enableInterest,
                          int interestInterval, boolean enableMetrics, boolean enableUpdateChecks) {
        this.commandPrefix = commandPrefix;
        this.balanceFormat = balanceFormat;
        this.enableInterest = enableInterest;
        this.interestInterval = interestInterval;
        this.enableMetrics = enableMetrics;
        this.enableUpdateChecks = enableUpdateChecks;
    }
    
    /**
     * Gets the command prefix
     * @return the command prefix
     */
    public String getCommandPrefix() {
        return commandPrefix;
    }
    
    /**
     * Gets the balance format
     * @return the balance format
     */
    public String getBalanceFormat() {
        return balanceFormat;
    }
    
    /**
     * Checks if interest is enabled
     * @return true if interest is enabled
     */
    public boolean isEnableInterest() {
        return enableInterest;
    }
    
    /**
     * Gets the interest interval in minutes
     * @return the interest interval
     */
    public int getInterestInterval() {
        return interestInterval;
    }
    
    /**
     * Checks if metrics are enabled
     * @return true if metrics are enabled
     */
    public boolean isEnableMetrics() {
        return enableMetrics;
    }
    
    /**
     * Checks if update checks are enabled
     * @return true if update checks are enabled
     */
    public boolean isEnableUpdateChecks() {
        return enableUpdateChecks;
    }
} 