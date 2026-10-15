package com.campusfind.dao;

import com.campusfind.config.ConnectionFactory;
import com.campusfind.exception.DatabaseOperationException;
import com.campusfind.model.Claim;
import com.campusfind.model.ClaimStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of {@link ClaimDao}.
 * 
 * <p><b>Transaction Management:</b>
 * Demonstrates an explicit ACID transaction in {@link #approveClaimAtomic(Integer, Integer)}.
 * Changes to the approved claim, the target item, and competing pending claims succeed or fail together.
 * </p>
 */
public class ClaimDaoJdbcImpl implements ClaimDao {
    private static final Logger logger = Logger.getLogger(ClaimDaoJdbcImpl.class.getName());

    private static final String INSERT_SQL =
            "INSERT INTO claims (item_id, claimant_name, claimant_contact, claim_description, verification_response, status) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT id, item_id, claimant_name, claimant_contact, claim_description, verification_response, status, submitted_at, reviewed_at " +
            "FROM claims WHERE id = ?";

    private static final String SELECT_BY_ITEM_ID_SQL =
            "SELECT id, item_id, claimant_name, claimant_contact, claim_description, verification_response, status, submitted_at, reviewed_at " +
            "FROM claims WHERE item_id = ? ORDER BY submitted_at DESC";

    private static final String SELECT_PENDING_SQL =
            "SELECT id, item_id, claimant_name, claimant_contact, claim_description, verification_response, status, submitted_at, reviewed_at " +
            "FROM claims WHERE status = 'PENDING' ORDER BY submitted_at ASC";

    private static final String SELECT_ALL_SQL =
            "SELECT id, item_id, claimant_name, claimant_contact, claim_description, verification_response, status, submitted_at, reviewed_at " +
            "FROM claims ORDER BY id DESC";

    private static final String CHECK_ACTIVE_CLAIM_SQL =
            "SELECT COUNT(*) FROM claims WHERE item_id = ? AND claimant_contact = ? AND status = 'PENDING'";

    private static final String UPDATE_STATUS_SQL =
            "UPDATE claims SET status = ?, reviewed_at = CURRENT_TIMESTAMP WHERE id = ?";

    @Override
    public Claim save(Claim claim) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, claim.getItemId());
            ps.setString(2, claim.getClaimantName());
            ps.setString(3, claim.getClaimantContact());
            ps.setString(4, claim.getClaimDescription());
            ps.setString(5, claim.getVerificationResponse());
            ps.setString(6, claim.getStatus().name());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseOperationException("Creating claim failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    claim.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseOperationException("Creating claim failed, no ID obtained.");
                }
            }
            return claim;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error persisting claim: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Claim> findById(Integer id) {
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
            throw new DatabaseOperationException("Error fetching claim by ID " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Claim> findByItemId(Integer itemId) {
        return executeQueryList(SELECT_BY_ITEM_ID_SQL, ps -> ps.setInt(1, itemId));
    }

    @Override
    public List<Claim> findPendingClaims() {
        return executeQueryList(SELECT_PENDING_SQL, ps -> {});
    }

    @Override
    public List<Claim> findAll() {
        return executeQueryList(SELECT_ALL_SQL, ps -> {});
    }

    @Override
    public boolean hasActiveClaim(Integer itemId, String claimantContact) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(CHECK_ACTIVE_CLAIM_SQL)) {

            ps.setInt(1, itemId);
            ps.setString(2, claimantContact.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error checking existing claims: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(Integer id, ClaimStatus status) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS_SQL)) {

            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating claim status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean approveClaimAtomic(Integer claimId, Integer itemId) {
        String approveClaimSql =
                "UPDATE claims SET status = 'APPROVED', reviewed_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND status = 'PENDING'";

        String updateItemSql =
                "UPDATE found_items SET status = 'CLAIM_PENDING' WHERE id = ?";

        String rejectOtherClaimsSql =
                "UPDATE claims SET status = 'REJECTED', reviewed_at = CURRENT_TIMESTAMP " +
                "WHERE item_id = ? AND id != ? AND status = 'PENDING'";

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            // Start transaction
            conn.setAutoCommit(false);

            // 1. Approve target claim
            try (PreparedStatement psClaim = conn.prepareStatement(approveClaimSql)) {
                psClaim.setInt(1, claimId);
                int claimUpdated = psClaim.executeUpdate();
                if (claimUpdated == 0) {
                    conn.rollback();
                    logger.warning("Claim " + claimId + " was not in PENDING state. Transaction rolled back.");
                    return false;
                }
            }

            // 2. Ensure item status reflects active approved claim state
            try (PreparedStatement psItem = conn.prepareStatement(updateItemSql)) {
                psItem.setInt(1, itemId);
                int itemUpdated = psItem.executeUpdate();
                if (itemUpdated == 0) {
                    conn.rollback();
                    logger.warning("Item " + itemId + " could not be updated. Transaction rolled back.");
                    return false;
                }
            }

            // 3. Automatically reject competing pending claims for the same item
            try (PreparedStatement psReject = conn.prepareStatement(rejectOtherClaimsSql)) {
                psReject.setInt(1, itemId);
                psReject.setInt(2, claimId);
                int rejectedCount = psReject.executeUpdate();
                if (rejectedCount > 0) {
                    logger.info("Automatically rejected " + rejectedCount + " competing pending claim(s) for item " + itemId);
                }
            }

            // Commit transaction
            conn.commit();
            logger.info("Transaction committed successfully: Claim " + claimId + " approved for item " + itemId);
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    logger.log(Level.SEVERE, "Transaction failed. Rolling back changes for claim " + claimId, e);
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Failed to rollback transaction", ex);
                }
            }
            throw new DatabaseOperationException("Atomic claim approval transaction failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.warning("Failed to reset auto-commit / close connection: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public boolean rejectClaim(Integer claimId) {
        return updateStatus(claimId, ClaimStatus.REJECTED);
    }

    private List<Claim> executeQueryList(String sql, PreparedStatementConsumer consumer) {
        List<Claim> results = new ArrayList<>();
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
            throw new DatabaseOperationException("Error executing claim query: " + e.getMessage(), e);
        }
    }

    private Claim mapRow(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        Integer itemId = rs.getInt("item_id");
        String name = rs.getString("claimant_name");
        String contact = rs.getString("claimant_contact");
        String desc = rs.getString("claim_description");
        String response = rs.getString("verification_response");
        ClaimStatus status = ClaimStatus.fromString(rs.getString("status"));
        Timestamp subTs = rs.getTimestamp("submitted_at");
        LocalDateTime submittedAt = subTs != null ? subTs.toLocalDateTime() : LocalDateTime.now();
        Timestamp revTs = rs.getTimestamp("reviewed_at");
        LocalDateTime reviewedAt = revTs != null ? revTs.toLocalDateTime() : null;

        return new Claim(id, itemId, name, contact, desc, response, status, submittedAt, reviewedAt);
    }

    @FunctionalInterface
    private interface PreparedStatementConsumer {
        void accept(PreparedStatement ps) throws SQLException;
    }
}
