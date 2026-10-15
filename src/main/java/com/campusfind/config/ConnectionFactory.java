package com.campusfind.config;

import com.campusfind.exception.DatabaseOperationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Factory class responsible for establishing JDBC connections to the MySQL database.
 * 
 * <p>Demonstrates:
 * <ul>
 *   <li>JDBC Driver registration</li>
 *   <li>Centralized connection management</li>
 *   <li>Connection testing and diagnostics</li>
 * </ul>
 * </p>
 */
public class ConnectionFactory {
    private static final Logger logger = Logger.getLogger(ConnectionFactory.class.getName());
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC Driver not found on classpath: " + DRIVER_CLASS, e);
        }
    }

    private ConnectionFactory() {}

    /**
     * Creates and returns a new JDBC connection using the active configuration.
     * 
     * @return an open {@link Connection}
     * @throws DatabaseOperationException if connection fails
     */
    public static Connection getConnection() {
        DatabaseConfig config = DatabaseConfig.getInstance();
        try {
            return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to connect to MySQL database at " + config.getUrl() +
                    ". Please ensure MySQL server is running and credentials are correct. Detail: " + e.getMessage(), e);
        }
    }

    /**
     * Tests if the database is reachable and logs appropriate diagnostics.
     *
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        DatabaseConfig config = DatabaseConfig.getInstance();
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (Exception e) {
            logger.warning("Database connectivity check failed: " + e.getMessage());
            return false;
        }
    }
}
