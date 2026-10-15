package com.campusfind.dao;

import com.campusfind.model.Claim;
import com.campusfind.model.ClaimStatus;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for {@link Claim} entities.
 */
public interface ClaimDao {

    /**
     * Persists a newly submitted claim into the database.
     */
    Claim save(Claim claim);

    /**
     * Finds a claim by its unique ID.
     */
    Optional<Claim> findById(Integer id);

    /**
     * Finds all claims submitted for a particular item.
     */
    List<Claim> findByItemId(Integer itemId);

    /**
     * Finds all claims currently in PENDING review status.
     */
    List<Claim> findPendingClaims();

    /**
     * Retrieves all claims in the database (for history / audit log).
     */
    List<Claim> findAll();

    /**
     * Checks if a claimant already has an active PENDING claim for this item.
     */
    boolean hasActiveClaim(Integer itemId, String claimantContact);

    /**
     * Updates claim status and timestamp.
     */
    boolean updateStatus(Integer id, ClaimStatus status);

    /**
     * Atomically approves a claim, sets its status to APPROVED, updates reviewed_at timestamp,
     * updates the item status to CLAIM_PENDING, and auto-rejects other pending claims on the same item.
     * All changes execute within a single ACID transaction; any failure triggers rollback.
     *
     * @param claimId target claim ID
     * @param itemId target item ID
     * @return true if successfully approved and committed
     */
    boolean approveClaimAtomic(Integer claimId, Integer itemId);

    /**
     * Rejects a claim.
     */
    boolean rejectClaim(Integer claimId);
}
