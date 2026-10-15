package com.campusfind.dao;

import com.campusfind.model.DashboardStatistics;
import com.campusfind.model.FoundItem;
import com.campusfind.model.ItemStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for {@link FoundItem} entities.
 * Decouples business logic from low-level JDBC SQL statements.
 */
public interface FoundItemDao {

    /**
     * Persists a new found item into the database and populates its generated primary key ID.
     *
     * @param item the item to insert
     * @return the saved item with generated ID
     */
    FoundItem save(FoundItem item);

    /**
     * Retrieves an item by its unique ID.
     */
    Optional<FoundItem> findById(Integer id);

    /**
     * Retrieves all items that are currently open (AVAILABLE and CLAIM_PENDING).
     * Excludes RETURNED items from active public view.
     */
    List<FoundItem> findAllAvailable();

    /**
     * Searches items by keyword across item name and public description.
     */
    List<FoundItem> searchByKeyword(String keyword);

    /**
     * Filters items by category (e.g. Electronics, Documents).
     */
    List<FoundItem> findByCategory(String category);

    /**
     * Filters items by campus location found.
     */
    List<FoundItem> findByLocation(String location);

    /**
     * Retrieves all items regardless of status (for history/reporting).
     */
    List<FoundItem> findAll();

    /**
     * Updates the status of an item.
     */
    boolean updateStatus(Integer id, ItemStatus newStatus);

    /**
     * Updates item status within an existing database transaction connection.
     */
    boolean updateStatus(Connection conn, Integer id, ItemStatus newStatus);

    /**
     * Aggregates item and claim statistics from the database for the dashboard.
     */
    DashboardStatistics getStatistics();
}
