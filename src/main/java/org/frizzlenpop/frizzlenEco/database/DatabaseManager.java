package org.frizzlenpop.frizzlenEco.database;

import org.frizzlenpop.frizzlenEco.FrizzlenEco;
import org.frizzlenpop.frizzlenEco.config.DatabaseSettings;
import org.frizzlenpop.frizzlenEco.economy.AccountHolder;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages database connections and operations for economy data
 */
public class DatabaseManager {
    private final FrizzlenEco plugin;
    private final DatabaseSettings settings;
    
    private Connection connection;
    private boolean initialized;
    
    /**
     * Creates a new DatabaseManager
     * @param plugin the FrizzlenEco plugin instance
     */
    public DatabaseManager(FrizzlenEco plugin) {
        this.plugin = plugin;
        this.settings = plugin.getConfigManager().getDatabaseSettings();
        this.initialized = false;
    }
    
    /**
     * Initializes the database connection and tables
     * @return true if initialization was successful
     */
    public boolean initialize() {
        if (initialized) {
            return true;
        }
        
        try {
            // Create connection
            createConnection();
            
            // Create tables
            createTables();
            
            initialized = true;
            plugin.getLogger().info("Database initialized successfully");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
            return false;
        }
    }
    
    /**
     * Shuts down the database connection
     */
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error closing database connection", e);
        }
    }
    
    /**
     * Creates the database connection
     * @throws SQLException if an error occurs
     */
    private void createConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        
        if (settings.isSQLite()) {
            try {
                Class.forName("org.sqlite.JDBC");
                
                File dataFolder = plugin.getDataFolder();
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }
                
                File dbFile = settings.getSqliteFile();
                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                
                connection = DriverManager.getConnection(url);
                
                if (connection != null) {
                    plugin.getLogger().info("Connected to SQLite database");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to connect to SQLite database", e);
                throw new SQLException("Failed to connect to SQLite database", e);
            }
        } else if (settings.isMySQL()) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                
                String url = "jdbc:mysql://" + settings.getHost() + ":" + settings.getPort() + "/" + settings.getDatabase();
                Properties properties = new Properties();
                properties.setProperty("user", settings.getUsername());
                properties.setProperty("password", settings.getPassword());
                properties.setProperty("useSSL", String.valueOf(settings.isUseSSL()));
                
                connection = DriverManager.getConnection(url, properties);
                
                if (connection != null) {
                    plugin.getLogger().info("Connected to MySQL database");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to connect to MySQL database", e);
                throw new SQLException("Failed to connect to MySQL database", e);
            }
        }
    }
    
    /**
     * Creates the necessary database tables
     * @throws SQLException if an error occurs
     */
    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Create accounts table
            String accountsTable = "CREATE TABLE IF NOT EXISTS accounts (" +
                    "player_uuid VARCHAR(36) NOT NULL, " +
                    "player_name VARCHAR(36) NOT NULL, " +
                    "currency_id VARCHAR(36) NOT NULL, " +
                    "balance TEXT NOT NULL, " +
                    "created BIGINT NOT NULL, " +
                    "last_transaction BIGINT NOT NULL, " +
                    "PRIMARY KEY (player_uuid, currency_id)" +
                    ");";
            
            statement.execute(accountsTable);
            
            // Create transactions table
            String transactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id VARCHAR(36) NOT NULL, " +
                    "transaction_type VARCHAR(36) NOT NULL, " +
                    "from_uuid VARCHAR(36), " +
                    "to_uuid VARCHAR(36), " +
                    "currency_id VARCHAR(36) NOT NULL, " +
                    "amount TEXT NOT NULL, " +
                    "timestamp BIGINT NOT NULL, " +
                    "PRIMARY KEY (id)" +
                    ");";
            
            statement.execute(transactionsTable);
        }
    }
    
    /**
     * Gets a new database connection or reconnects if necessary
     * @return the database connection
     * @throws SQLException if an error occurs
     */
    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            createConnection();
        }
        return connection;
    }
    
    /**
     * Loads all accounts from the database
     * @return map of player UUIDs to their account maps
     */
    public Map<UUID, Map<String, AccountHolder>> loadAllAccounts() {
        Map<UUID, Map<String, AccountHolder>> accounts = new ConcurrentHashMap<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                UUID playerUuid = UUID.fromString(rs.getString("player_uuid"));
                String playerName = rs.getString("player_name");
                String currencyId = rs.getString("currency_id");
                BigDecimal balance = new BigDecimal(rs.getString("balance"));
                long created = rs.getLong("created");
                long lastTransaction = rs.getLong("last_transaction");
                
                AccountHolder account = new AccountHolder(playerUuid, playerName, currencyId, balance);
                account.setCreated(Instant.ofEpochMilli(created));
                
                // Get or create player's account map
                Map<String, AccountHolder> playerAccounts = accounts.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>());
                
                // Add account to player's map
                playerAccounts.put(currencyId, account);
            }
            
            plugin.getLogger().info("Loaded " + accounts.size() + " accounts from database");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading accounts from database", e);
        }
        
        return accounts;
    }
    
    /**
     * Saves all accounts to the database
     * @param accounts map of player UUIDs to their account maps
     */
    public void saveAllAccounts(Map<UUID, Map<String, AccountHolder>> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return;
        }
        
        try (Connection conn = getConnection()) {
            // Use a batch update for efficiency
            String sql = "INSERT OR REPLACE INTO accounts (player_uuid, player_name, currency_id, balance, created, last_transaction) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
            
            if (settings.isMySQL()) {
                sql = "INSERT INTO accounts (player_uuid, player_name, currency_id, balance, created, last_transaction) " +
                      "VALUES (?, ?, ?, ?, ?, ?) " +
                      "ON DUPLICATE KEY UPDATE player_name = VALUES(player_name), balance = VALUES(balance), last_transaction = VALUES(last_transaction)";
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                
                int count = 0;
                
                for (Map.Entry<UUID, Map<String, AccountHolder>> entry : accounts.entrySet()) {
                    for (AccountHolder account : entry.getValue().values()) {
                        stmt.setString(1, account.getPlayerUuid().toString());
                        stmt.setString(2, account.getPlayerName());
                        stmt.setString(3, account.getCurrencyId());
                        stmt.setString(4, account.getBalance().toString());
                        stmt.setLong(5, account.getCreated().toEpochMilli());
                        stmt.setLong(6, account.getLastTransaction().toEpochMilli());
                        
                        stmt.addBatch();
                        count++;
                        
                        // Execute in batches of 100
                        if (count % 100 == 0) {
                            stmt.executeBatch();
                        }
                    }
                }
                
                // Execute any remaining statements
                stmt.executeBatch();
                conn.commit();
                
                plugin.getLogger().info("Saved " + count + " accounts to database");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving accounts to database", e);
        }
    }
    
    /**
     * Saves a single account to the database
     * @param account the account to save
     */
    public void saveAccount(AccountHolder account) {
        if (account == null) {
            return;
        }
        
        try (Connection conn = getConnection()) {
            String sql = "INSERT OR REPLACE INTO accounts (player_uuid, player_name, currency_id, balance, created, last_transaction) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
            
            if (settings.isMySQL()) {
                sql = "INSERT INTO accounts (player_uuid, player_name, currency_id, balance, created, last_transaction) " +
                      "VALUES (?, ?, ?, ?, ?, ?) " +
                      "ON DUPLICATE KEY UPDATE player_name = VALUES(player_name), balance = VALUES(balance), last_transaction = VALUES(last_transaction)";
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, account.getPlayerUuid().toString());
                stmt.setString(2, account.getPlayerName());
                stmt.setString(3, account.getCurrencyId());
                stmt.setString(4, account.getBalance().toString());
                stmt.setLong(5, account.getCreated().toEpochMilli());
                stmt.setLong(6, account.getLastTransaction().toEpochMilli());
                
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving account to database: " + account.getPlayerUuid(), e);
        }
    }
    
    /**
     * Records a transaction in the database
     * @param type the transaction type
     * @param fromUuid the UUID of the player money is taken from (can be null for deposits)
     * @param toUuid the UUID of the player money is given to (can be null for withdrawals)
     * @param currencyId the currency ID
     * @param amount the amount of the transaction
     */
    public void recordTransaction(String type, UUID fromUuid, UUID toUuid, String currencyId, BigDecimal amount) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO transactions (id, transaction_type, from_uuid, to_uuid, currency_id, amount, timestamp) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String id = UUID.randomUUID().toString();
                stmt.setString(1, id);
                stmt.setString(2, type);
                stmt.setString(3, fromUuid != null ? fromUuid.toString() : null);
                stmt.setString(4, toUuid != null ? toUuid.toString() : null);
                stmt.setString(5, currencyId);
                stmt.setString(6, amount.toString());
                stmt.setLong(7, System.currentTimeMillis());
                
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error recording transaction in database", e);
        }
    }
} 