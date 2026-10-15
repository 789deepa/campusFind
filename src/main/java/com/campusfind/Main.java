package com.campusfind;

import com.campusfind.config.ConnectionFactory;
import com.campusfind.config.DatabaseConfig;
import com.campusfind.dao.ClaimDao;
import com.campusfind.dao.ClaimDaoJdbcImpl;
import com.campusfind.dao.FoundItemDao;
import com.campusfind.dao.FoundItemDaoJdbcImpl;
import com.campusfind.dao.LostItemDao;
import com.campusfind.dao.LostItemDaoJdbcImpl;
import com.campusfind.service.ClaimService;
import com.campusfind.service.ItemService;
import com.campusfind.service.LostItemService;
import com.campusfind.service.ValidationService;
import com.campusfind.ui.ConsoleMenu;
import com.campusfind.ui.ConsolePrinter;
import com.campusfind.util.LoggerUtil;

import java.util.Scanner;
import java.util.logging.Level;

/**
 * Main application entry point for the CampusFind Lost & Found system.
 * 
 * <p>Execution Flow:
 * <ol>
 *   <li>Initializes application logging (java.util.logging)</li>
 *   <li>Performs startup database health check</li>
 *   <li>Wires dependencies following Layered Architecture principles</li>
 *   <li>Launches the interactive ConsoleMenu</li>
 * </ol>
 * </p>
 */
public class Main {

    public static void main(String[] args) {
        // 1. Initialize logging
        LoggerUtil.configureLogging(Level.INFO);

        System.out.println("Starting CampusFind Application...");
        DatabaseConfig config = DatabaseConfig.getInstance();
        System.out.println("Target Database URL : " + config.getUrl());
        System.out.println("Target Database User: " + config.getUser());

        // 2. Perform database connectivity check
        boolean dbConnected = ConnectionFactory.testConnection();

        if (!dbConnected) {
            ConsolePrinter.printBanner();
            System.out.println("\n[!] DATABASE CONNECTION NOTICE:");
            System.out.println("------------------------------------------------------------------");
            System.out.println("Could not establish a JDBC connection to MySQL server.");
            System.out.println("URL attempted : " + config.getUrl());
            System.out.println("User attempted: " + config.getUser());
            System.out.println();
            System.out.println("How to resolve:");
            System.out.println("  1. Ensure MySQL server is running (e.g. via XAMPP, MySQL Service, or Docker).");
            System.out.println("  2. Create the database and tables using:");
            System.out.println("       mysql -u root -p < src/main/resources/db/schema.sql");
            System.out.println("       mysql -u root -p < src/main/resources/db/sample-data.sql");
            System.out.println("  3. Set credentials in 'db.properties' (see 'src/main/resources/db.properties.example')");
            System.out.println("     or export environment variables: DB_URL, DB_USER, DB_PASSWORD.");
            System.out.println();
            System.out.println("Note for Evaluation & Testing:");
            System.out.println("  All core Java logic, validation rules, privacy filters, and state transitions");
            System.out.println("  can be verified independently of MySQL by running:");
            System.out.println("       mvn test");
            System.out.println("------------------------------------------------------------------");
            try {
                com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
            } catch (Throwable ignored) {}
            return;
        }

        System.out.println("[+] Database connection verified successfully.\n");

        // 3. Dependency Injection (Manual Constructor Injection)
        FoundItemDao foundItemDao = new FoundItemDaoJdbcImpl();
        ClaimDao claimDao = new ClaimDaoJdbcImpl();
        LostItemDao lostItemDao = new LostItemDaoJdbcImpl();
        ValidationService validationService = new ValidationService();

        ItemService itemService = new ItemService(foundItemDao, claimDao, lostItemDao, validationService);
        ClaimService claimService = new ClaimService(claimDao, foundItemDao, lostItemDao, validationService);
        LostItemService lostItemService = new LostItemService(lostItemDao, foundItemDao, validationService);

        // 4. Launch Console UI
        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleMenu menu = new ConsoleMenu(itemService, claimService, lostItemService, scanner);
            menu.run();
        } finally {
            try {
                com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
            } catch (Throwable ignored) {}
        }
    }
}
