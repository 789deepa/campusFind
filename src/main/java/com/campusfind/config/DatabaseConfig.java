package com.campusfind.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loads database configuration from environment variables or a local db.properties file.
 * 
 * <p><b>Security Best Practice:</b>
 * Real database passwords are never committed into git or hardcoded into source files.
 * Order of priority:
 * 1. Environment variables (DB_URL, DB_USER, DB_PASSWORD)
 * 2. Local db.properties file in working directory
 * 3. Classpath db.properties resource
 * 4. Safe localhost default fallback
 * </p>
 */
public class DatabaseConfig {
    private static final Logger logger = Logger.getLogger(DatabaseConfig.class.getName());

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/campusfind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private final String url;
    private final String user;
    private final String password;

    private static DatabaseConfig instance;

    private DatabaseConfig() {
        Properties props = new Properties();

        // 1. Try to read from local file system (working directory)
        File localFile = new File("db.properties");
        if (localFile.exists()) {
            try (InputStream is = new FileInputStream(localFile)) {
                props.load(is);
                logger.info("Loaded database configuration from local db.properties");
            } catch (Exception e) {
                logger.warning("Could not load local db.properties: " + e.getMessage());
            }
        } else {
            // 2. Try classpath resource
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
                if (is != null) {
                    props.load(is);
                    logger.info("Loaded database configuration from classpath db.properties");
                }
            } catch (Exception e) {
                logger.warning("Could not load classpath db.properties: " + e.getMessage());
            }
        }

        // Environment variables take precedence over properties files
        String envUrl = System.getenv("DB_URL");
        if (envUrl == null) envUrl = System.getenv("CAMPUSFIND_DB_URL");

        String envUser = System.getenv("DB_USER");
        if (envUser == null) envUser = System.getenv("CAMPUSFIND_DB_USER");

        String envPass = System.getenv("DB_PASSWORD");
        if (envPass == null) envPass = System.getenv("CAMPUSFIND_DB_PASSWORD");

        this.url = envUrl != null ? envUrl : props.getProperty("db.url", DEFAULT_URL);
        this.user = envUser != null ? envUser : props.getProperty("db.user", DEFAULT_USER);
        this.password = envPass != null ? envPass : props.getProperty("db.password", DEFAULT_PASSWORD);
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
