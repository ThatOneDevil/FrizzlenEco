package org.frizzlenpop.frizzlenEco.metrics;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.frizzlenpop.frizzlenEco.FrizzlenEco;
import org.frizzlenpop.frizzlenEco.economy.AccountHolder;
import org.frizzlenpop.frizzlenEco.economy.Currency;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 * Manages plugin metrics and statistics
 */
public class MetricsManager {
    private final FrizzlenEco plugin;
    private final Map<String, AtomicInteger> transactionCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> transactionVolumes = new ConcurrentHashMap<>();
    private final AtomicInteger totalAccounts = new AtomicInteger(0);
    private final Map<String, AtomicInteger> currencyAccounts = new ConcurrentHashMap<>();
    
    private BukkitTask statsTask;
    private Instant startTime;
    
    /**
     * Creates a new MetricsManager
     * @param plugin the FrizzlenEco plugin instance
     */
    public MetricsManager(FrizzlenEco plugin) {
        this.plugin = plugin;
        this.startTime = Instant.now();
    }
    
    /**
     * Initializes the metrics system
     */
    public void initialize() {
        if (!plugin.getConfigManager().getGeneralSettings().isEnableMetrics()) {
            plugin.getLogger().info("Metrics are disabled in config");
            return;
        }
        
        // Initialize metrics
        startTime = Instant.now();
        
        // Reset metrics
        resetMetrics();
        
        // Schedule periodic stats logging
        statsTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::logStats, 12000L, 12000L); // Every 10 minutes
        
        plugin.getLogger().info("Metrics system initialized");
    }
    
    /**
     * Resets all metrics counters
     */
    public void resetMetrics() {
        transactionCounts.clear();
        transactionVolumes.clear();
        totalAccounts.set(0);
        currencyAccounts.clear();
    }
    
    /**
     * Updates account metrics
     * @param accounts the accounts map to analyze
     */
    public void updateAccountMetrics(Map<UUID, Map<String, AccountHolder>> accounts) {
        if (accounts == null) {
            return;
        }
        
        // Reset account counters
        totalAccounts.set(0);
        currencyAccounts.clear();
        
        // Count accounts by currency
        for (Map<String, AccountHolder> playerAccounts : accounts.values()) {
            totalAccounts.addAndGet(playerAccounts.size());
            
            for (AccountHolder account : playerAccounts.values()) {
                currencyAccounts.computeIfAbsent(account.getCurrencyId(), k -> new AtomicInteger(0))
                               .incrementAndGet();
            }
        }
    }
    
    /**
     * Records a transaction for metrics
     * @param type the transaction type
     * @param currency the currency
     * @param amount the transaction amount
     */
    public void recordTransaction(String type, Currency currency, BigDecimal amount) {
        if (!plugin.getConfigManager().getGeneralSettings().isEnableMetrics()) {
            return;
        }
        
        try {
            String key = type + "_" + currency.getId();
            
            // Increment transaction count
            transactionCounts.computeIfAbsent(key, k -> new AtomicInteger(0))
                            .incrementAndGet();
            
            // Add to transaction volume (convert to long cents to avoid BigDecimal in concurrent map)
            long amountCents = amount.movePointRight(currency.getDecimalPlaces()).longValue();
            transactionVolumes.computeIfAbsent(key, k -> new AtomicLong(0))
                             .addAndGet(amountCents);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error recording transaction metrics", e);
        }
    }
    
    /**
     * Logs current stats to the console
     */
    private void logStats() {
        try {
            Duration uptime = Duration.between(startTime, Instant.now());
            long days = uptime.toDays();
            long hours = uptime.toHoursPart();
            long minutes = uptime.toMinutesPart();
            
            plugin.getLogger().info("=== FrizzlenEco Stats ===");
            plugin.getLogger().info("Uptime: " + days + "d " + hours + "h " + minutes + "m");
            plugin.getLogger().info("Total accounts: " + totalAccounts.get());
            
            // Log accounts by currency
            plugin.getLogger().info("=== Accounts by Currency ===");
            for (Map.Entry<String, AtomicInteger> entry : currencyAccounts.entrySet()) {
                Currency currency = plugin.getEconomyManager().getCurrency(entry.getKey());
                String currencyName = currency != null ? currency.getName() : entry.getKey();
                plugin.getLogger().info(currencyName + ": " + entry.getValue().get());
            }
            
            // Log transaction counts
            plugin.getLogger().info("=== Transaction Counts ===");
            for (Map.Entry<String, AtomicInteger> entry : transactionCounts.entrySet()) {
                plugin.getLogger().info(entry.getKey() + ": " + entry.getValue().get());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error logging stats", e);
        }
    }
    
    /**
     * Gets the metrics start time
     * @return the start time
     */
    public Instant getStartTime() {
        return startTime;
    }
    
    /**
     * Gets the total number of accounts
     * @return the total number of accounts
     */
    public int getTotalAccounts() {
        return totalAccounts.get();
    }
    
    /**
     * Gets the transaction counts map
     * @return the transaction counts map
     */
    public Map<String, AtomicInteger> getTransactionCounts() {
        return transactionCounts;
    }
} 