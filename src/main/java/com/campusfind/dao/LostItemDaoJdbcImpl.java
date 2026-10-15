package com.campusfind.dao;

import com.campusfind.config.ConnectionFactory;
import com.campusfind.exception.DatabaseOperationException;
import com.campusfind.model.LostItem;
import com.campusfind.model.LostItemStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link LostItemDao}.
 */
public class LostItemDaoJdbcImpl implements LostItemDao {

    private static final String INSERT_SQL =
            "INSERT INTO lost_items (item_name, category, description, location_lost, date_lost, student_name, student_contact, identifying_details, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT id, item_name, category, description, location_lost, date_lost, date_reported, student_name, student_contact, identifying_details, status " +
            "FROM lost_items WHERE id = ?";

    private static final String SELECT_OPEN_SQL =
            "SELECT id, item_name, category, description, location_lost, date_lost, date_reported, student_name, student_contact, identifying_details, status " +
            "FROM lost_items WHERE status IN ('OPEN', 'MATCHED') ORDER BY date_lost DESC, id DESC";

    private static final String SEARCH_KEYWORD_SQL =
            "SELECT id, item_name, category, description, location_lost, date_lost, date_reported, student_name, student_contact, identifying_details, status " +
            "FROM lost_items WHERE item_name LIKE ? OR description LIKE ? ORDER BY date_lost DESC, id DESC";

    private static final String FILTER_CATEGORY_SQL =
            "SELECT id, item_name, category, description, location_lost, date_lost, date_reported, student_name, student_contact, identifying_details, status " +
            "FROM lost_items WHERE category = ? ORDER BY date_lost DESC, id DESC";

    private static final String FILTER_LOCATION_SQL =
            "SELECT id, item_name, category, description, location_lost, date_lost, date_reported, student_name, student_contact, identifying_details, status " +
            "FROM lost_items WHERE location_lost LIKE ? ORDER BY date_lost DESC, id DESC";

    private static final String SELECT_ALL_SQL =
            "SELECT id, item_name, category, description, location_lost, date_lost, date_reported, student_name, student_contact, identifying_details, status " +
            "FROM lost_items ORDER BY id DESC";

    private static final String UPDATE_STATUS_SQL =
            "UPDATE lost_items SET status = ? WHERE id = ?";

    @Override
    public LostItem save(LostItem item) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setString(3, item.getDescription());
            ps.setString(4, item.getLocationLost());
            ps.setDate(5, Date.valueOf(item.getDateLost()));
            ps.setString(6, item.getStudentName());
            ps.setString(7, item.getStudentContact());
            ps.setString(8, item.getIdentifyingDetails());
            ps.setString(9, item.getStatus().name());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseOperationException("Creating lost item failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseOperationException("Creating lost item failed, no ID obtained.");
                }
            }
            return item;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error persisting lost item: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<LostItem> findById(Integer id) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching lost item by ID " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<LostItem> findAllOpen() {
        return executeQueryList(SELECT_OPEN_SQL, ps -> {});
    }

    @Override
    public List<LostItem> searchByKeyword(String keyword) {
        String pattern = "%" + keyword.trim() + "%";
        return executeQueryList(SEARCH_KEYWORD_SQL, ps -> {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
        });
    }

    @Override
    public List<LostItem> findByCategory(String category) {
        return executeQueryList(FILTER_CATEGORY_SQL, ps -> ps.setString(1, category.trim()));
    }

    @Override
    public List<LostItem> findByLocation(String location) {
        String pattern = "%" + location.trim() + "%";
        return executeQueryList(FILTER_LOCATION_SQL, ps -> ps.setString(1, pattern));
    }

    @Override
    public List<LostItem> findAll() {
        return executeQueryList(SELECT_ALL_SQL, ps -> {});
    }

    @Override
    public boolean updateStatus(Integer id, LostItemStatus newStatus) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return updateStatus(conn, id, newStatus);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating status for lost item ID " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(Connection conn, Integer id, LostItemStatus newStatus) {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS_SQL)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating lost item status in transaction: " + e.getMessage(), e);
        }
    }

    private List<LostItem> executeQueryList(String sql, PreparedStatementConsumer consumer) {
        List<LostItem> results = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            consumer.accept(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error executing lost item query: " + e.getMessage(), e);
        }
    }

    private LostItem mapRow(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        String itemName = rs.getString("item_name");
        String category = rs.getString("category");
        String description = rs.getString("description");
        String locationLost = rs.getString("location_lost");
        LocalDate dateLost = rs.getDate("date_lost").toLocalDate();
        Timestamp ts = rs.getTimestamp("date_reported");
        LocalDateTime dateReported = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        String studentName = rs.getString("student_name");
        String studentContact = rs.getString("student_contact");
        String identifyingDetails = rs.getString("identifying_details");
        LostItemStatus status = LostItemStatus.fromString(rs.getString("status"));

        return new LostItem(id, itemName, category, description, locationLost,
                dateLost, dateReported, studentName, studentContact, identifyingDetails, status);
    }

    @FunctionalInterface
    private interface PreparedStatementConsumer {
        void accept(PreparedStatement ps) throws SQLException;
    }
}
