package com.campusfind.dao;

import com.campusfind.config.ConnectionFactory;
import com.campusfind.exception.DatabaseOperationException;
import com.campusfind.model.DashboardStatistics;
import com.campusfind.model.FoundItem;
import com.campusfind.model.ItemStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * JDBC implementation of {@link FoundItemDao}.
 * 
 * <p>Demonstrates:
 * <ul>
 *   <li>PreparedStatements to prevent SQL Injection</li>
 *   <li>try-with-resources to guarantee connection/statement closure</li>
 *   <li>Retrieval of auto-generated primary keys</li>
 *   <li>Safe mapping of SQL types to modern java.time types</li>
 * </ul>
 * </p>
 */
public class FoundItemDaoJdbcImpl implements FoundItemDao {
    private static final Logger logger = Logger.getLogger(FoundItemDaoJdbcImpl.class.getName());

    private static final String INSERT_SQL =
            "INSERT INTO found_items (item_name, category, description, location_found, date_found, reported_by, private_verification_clue, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT id, item_name, category, description, location_found, date_found, date_reported, reported_by, private_verification_clue, status " +
            "FROM found_items WHERE id = ?";

    private static final String SELECT_AVAILABLE_SQL =
            "SELECT id, item_name, category, description, location_found, date_found, date_reported, reported_by, private_verification_clue, status " +
            "FROM found_items WHERE status IN ('AVAILABLE', 'CLAIM_PENDING') ORDER BY date_found DESC, id DESC";

    private static final String SEARCH_KEYWORD_SQL =
            "SELECT id, item_name, category, description, location_found, date_found, date_reported, reported_by, private_verification_clue, status " +
            "FROM found_items WHERE item_name LIKE ? OR description LIKE ? ORDER BY date_found DESC, id DESC";

    private static final String FILTER_CATEGORY_SQL =
            "SELECT id, item_name, category, description, location_found, date_found, date_reported, reported_by, private_verification_clue, status " +
            "FROM found_items WHERE category = ? ORDER BY date_found DESC, id DESC";

    private static final String FILTER_LOCATION_SQL =
            "SELECT id, item_name, category, description, location_found, date_found, date_reported, reported_by, private_verification_clue, status " +
            "FROM found_items WHERE location_found LIKE ? ORDER BY date_found DESC, id DESC";

    private static final String SELECT_ALL_SQL =
            "SELECT id, item_name, category, description, location_found, date_found, date_reported, reported_by, private_verification_clue, status " +
            "FROM found_items ORDER BY id DESC";

    private static final String UPDATE_STATUS_SQL =
            "UPDATE found_items SET status = ? WHERE id = ?";

    @Override
    public FoundItem save(FoundItem item) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setString(3, item.getDescription());
            ps.setString(4, item.getLocationFound());
            ps.setDate(5, Date.valueOf(item.getDateFound()));
            ps.setString(6, item.getReportedBy());
            ps.setString(7, item.getPrivateVerificationClue());
            ps.setString(8, item.getStatus().name());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseOperationException("Creating found item failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseOperationException("Creating found item failed, no ID obtained.");
                }
            }
            return item;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error persisting found item: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<FoundItem> findById(Integer id) {
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
            throw new DatabaseOperationException("Error fetching item by ID " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<FoundItem> findAllAvailable() {
        return executeQueryList(SELECT_AVAILABLE_SQL, ps -> {});
    }

    @Override
    public List<FoundItem> searchByKeyword(String keyword) {
        String pattern = "%" + keyword.trim() + "%";
        return executeQueryList(SEARCH_KEYWORD_SQL, ps -> {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
        });
    }

    @Override
    public List<FoundItem> findByCategory(String category) {
        return executeQueryList(FILTER_CATEGORY_SQL, ps -> ps.setString(1, category.trim()));
    }

    @Override
    public List<FoundItem> findByLocation(String location) {
        String pattern = "%" + location.trim() + "%";
        return executeQueryList(FILTER_LOCATION_SQL, ps -> ps.setString(1, pattern));
    }

    @Override
    public List<FoundItem> findAll() {
        return executeQueryList(SELECT_ALL_SQL, ps -> {});
    }

    @Override
    public boolean updateStatus(Integer id, ItemStatus newStatus) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return updateStatus(conn, id, newStatus);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating status for item ID " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(Connection conn, Integer id, ItemStatus newStatus) {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS_SQL)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating status in transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public DashboardStatistics getStatistics() {
        String itemStatsSql =
                "SELECT " +
                "  COUNT(*) AS total_items, " +
                "  SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_items, " +
                "  SUM(CASE WHEN status = 'CLAIM_PENDING' THEN 1 ELSE 0 END) AS pending_claim_items, " +
                "  SUM(CASE WHEN status = 'RETURNED' THEN 1 ELSE 0 END) AS returned_items " +
                "FROM found_items";

        String claimStatsSql =
                "SELECT " +
                "  COUNT(*) AS total_claims, " +
                "  SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) AS pending_claims, " +
                "  SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) AS approved_claims, " +
                "  SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected_claims " +
                "FROM claims";

        String topLocSql =
                "SELECT location_found, COUNT(*) as cnt " +
                "FROM found_items " +
                "GROUP BY location_found " +
                "ORDER BY cnt DESC " +
                "LIMIT 1";

        int totalItems = 0, available = 0, pendingClaimItems = 0, returned = 0;
        int totalClaims = 0, pendingClaims = 0, approvedClaims = 0, rejectedClaims = 0;
        String topLocation = "N/A";

        try (Connection conn = ConnectionFactory.getConnection()) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(itemStatsSql)) {
                if (rs.next()) {
                    totalItems = rs.getInt("total_items");
                    available = rs.getInt("available_items");
                    pendingClaimItems = rs.getInt("pending_claim_items");
                    returned = rs.getInt("returned_items");
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(claimStatsSql)) {
                if (rs.next()) {
                    totalClaims = rs.getInt("total_claims");
                    pendingClaims = rs.getInt("pending_claims");
                    approvedClaims = rs.getInt("approved_claims");
                    rejectedClaims = rs.getInt("rejected_claims");
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(topLocSql)) {
                if (rs.next()) {
                    topLocation = rs.getString("location_found") + " (" + rs.getInt("cnt") + " items)";
                }
            }

            return new DashboardStatistics(totalItems, available, pendingClaimItems, returned,
                    totalClaims, pendingClaims, approvedClaims, rejectedClaims, topLocation);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to calculate dashboard statistics: " + e.getMessage(), e);
        }
    }

    private List<FoundItem> executeQueryList(String sql, PreparedStatementConsumer consumer) {
        List<FoundItem> results = new ArrayList<>();
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
            throw new DatabaseOperationException("Error executing item query: " + e.getMessage(), e);
        }
    }

    private FoundItem mapRow(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        String itemName = rs.getString("item_name");
        String category = rs.getString("category");
        String description = rs.getString("description");
        String locationFound = rs.getString("location_found");
        LocalDate dateFound = rs.getDate("date_found").toLocalDate();
        Timestamp ts = rs.getTimestamp("date_reported");
        LocalDateTime dateReported = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        String reportedBy = rs.getString("reported_by");
        String clue = rs.getString("private_verification_clue");
        ItemStatus status = ItemStatus.fromString(rs.getString("status"));

        return new FoundItem(id, itemName, category, description, locationFound,
                dateFound, dateReported, reportedBy, clue, status);
    }

    @FunctionalInterface
    private interface PreparedStatementConsumer {
        void accept(PreparedStatement ps) throws SQLException;
    }
}
