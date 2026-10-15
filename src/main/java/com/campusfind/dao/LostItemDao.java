package com.campusfind.dao;

import com.campusfind.model.LostItem;
import com.campusfind.model.LostItemStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for {@link LostItem} entities.
 */
public interface LostItemDao {

    /**
     * Persists a new lost-item report into the database and sets its generated primary key ID.
     */
    LostItem save(LostItem item);

    /**
     * Finds a lost-item report by its unique ID.
     */
    Optional<LostItem> findById(Integer id);

    /**
     * Finds all lost reports currently in OPEN or MATCHED status.
     */
    List<LostItem> findAllOpen();

    /**
     * Searches lost items by keyword across item name and description.
     */
    List<LostItem> searchByKeyword(String keyword);

    /**
     * Filters lost items by category.
     */
    List<LostItem> findByCategory(String category);

    /**
     * Filters lost items by last seen location.
     */
    List<LostItem> findByLocation(String location);

    /**
     * Retrieves all lost items regardless of status (for history/auditing).
     */
    List<LostItem> findAll();

    /**
     * Updates the status of a lost-item report.
     */
    boolean updateStatus(Integer id, LostItemStatus newStatus);

    /**
     * Updates lost item status within an existing database transaction connection.
     */
    boolean updateStatus(Connection conn, Integer id, LostItemStatus newStatus);
}
