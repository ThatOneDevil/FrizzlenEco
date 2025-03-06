package org.frizzlenpop.frizzlenEco.config;

import java.io.File;

/**
 * Stores database connection settings
 */
public class DatabaseSettings {
    /**
     * Type of database
     */
    public enum DatabaseType {
        SQLITE,
        MYSQL
    }
    
    private final DatabaseType type;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean useSSL;
    private final File sqliteFile;
    
    /**
     * Creates new database settings
     * @param type the database type
     * @param host the MySQL host (null for SQLite)
     * @param port the MySQL port (0 for SQLite)
     * @param database the MySQL database name (null for SQLite)
     * @param username the MySQL username (null for SQLite)
     * @param password the MySQL password (null for SQLite)
     * @param useSSL whether to use SSL for MySQL (false for SQLite)
     * @param sqliteFile the SQLite database file (null for MySQL)
     */
    public DatabaseSettings(DatabaseType type, String host, int port, String database,
                            String username, String password, boolean useSSL, File sqliteFile) {
        this.type = type;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.useSSL = useSSL;
        this.sqliteFile = sqliteFile;
    }
    
    /**
     * Gets the database type
     * @return the database type
     */
    public DatabaseType getType() {
        return type;
    }
    
    /**
     * Gets the MySQL host
     * @return the host, or null for SQLite
     */
    public String getHost() {
        return host;
    }
    
    /**
     * Gets the MySQL port
     * @return the port, or 0 for SQLite
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Gets the MySQL database name
     * @return the database name, or null for SQLite
     */
    public String getDatabase() {
        return database;
    }
    
    /**
     * Gets the MySQL username
     * @return the username, or null for SQLite
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Gets the MySQL password
     * @return the password, or null for SQLite
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Checks if SSL should be used for MySQL
     * @return true if SSL should be used, or false for SQLite
     */
    public boolean isUseSSL() {
        return useSSL;
    }
    
    /**
     * Gets the SQLite database file
     * @return the file, or null for MySQL
     */
    public File getSqliteFile() {
        return sqliteFile;
    }
    
    /**
     * Checks if this is a SQLite database
     * @return true if SQLite
     */
    public boolean isSQLite() {
        return type == DatabaseType.SQLITE;
    }
    
    /**
     * Checks if this is a MySQL database
     * @return true if MySQL
     */
    public boolean isMySQL() {
        return type == DatabaseType.MYSQL;
    }
} 