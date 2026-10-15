package com.campusfind.service;

import com.campusfind.dao.ClaimDao;
import com.campusfind.dao.FoundItemDao;
import com.campusfind.exception.ClaimNotFoundException;
import com.campusfind.exception.DuplicateClaimException;
import com.campusfind.exception.InvalidStatusTransitionException;
import com.campusfind.exception.ItemNotFoundException;
import com.campusfind.model.Claim;
import com.campusfind.model.ClaimStatus;
import com.campusfind.model.FoundItem;
import com.campusfind.model.ItemStatus;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service encapsulating business logic for student claims.
 * 
 * <p>Key Business Rules:
 * <ul>
 *   <li>Cannot claim a non-existent or already returned item</li>
 *   <li>Cannot submit duplicate pending claims for the same item</li>
 *   <li>Approving a claim atomically updates claim status and item status, and rejects competing claims</li>
 *   <li>Rejecting a claim restores item status to AVAILABLE if no other claims remain</li>
 * </ul>
 * </p>
 */
public class ClaimService {
    private static final Logger logger = Logger.getLogger(ClaimService.class.getName());

    private final ClaimDao claimDao;
    private final FoundItemDao foundItemDao;
    private final ValidationService validationService;

    public ClaimService(ClaimDao claimDao, FoundItemDao foundItemDao, ValidationService validationService) {
        this.claimDao = claimDao;
        this.foundItemDao = foundItemDao;
        this.validationService = validationService;
    }

    /**
     * Submits a new claim against an item.
     *
     * @param claim the claim to submit
     * @return saved claim with generated ID
     */
    public Claim submitClaim(Claim claim) {
        validationService.validateClaim(claim);

        FoundItem item = foundItemDao.findById(claim.getItemId())
                .orElseThrow(() -> new ItemNotFoundException(claim.getItemId()));

        if (item.getStatus() == ItemStatus.RETURNED) {
            throw new InvalidStatusTransitionException("Cannot submit a claim for item #" + item.getId() + " because it has already been returned to its owner.");
        }

        // Prevent multiple pending claims by the same claimant for the same item
        if (claimDao.hasActiveClaim(claim.getItemId(), claim.getClaimantContact())) {
            throw new DuplicateClaimException("You already have an active pending claim for item #" + item.getId() + " under contact '" + claim.getClaimantContact() + "'.");
        }

        claim.setStatus(ClaimStatus.PENDING);
        Claim saved = claimDao.save(claim);

        // If item was AVAILABLE, transition its status to CLAIM_PENDING
        if (item.getStatus() == ItemStatus.AVAILABLE) {
            foundItemDao.updateStatus(item.getId(), ItemStatus.CLAIM_PENDING);
            logger.info("Item #" + item.getId() + " status transitioned to CLAIM_PENDING following claim #" + saved.getId());
        }

        return saved;
    }

    public List<Claim> getPendingClaims() {
        return claimDao.findPendingClaims();
    }

    public List<Claim> getClaimsForItem(Integer itemId) {
        return claimDao.findByItemId(itemId);
    }

    public List<Claim> getAllClaimsHistory() {
        return claimDao.findAll();
    }

    public Optional<Claim> getClaimById(Integer claimId) {
        if (claimId == null || claimId <= 0) return Optional.empty();
        return claimDao.findById(claimId);
    }

    /**
     * Approves a claim using an atomic database transaction.
     *
     * @param claimId ID of the claim to approve
     * @return true if successfully approved
     */
    public boolean approveClaim(Integer claimId) {
        Claim claim = claimDao.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new InvalidStatusTransitionException("Cannot approve claim #" + claimId + " because its current status is " + claim.getStatus() + ". Only PENDING claims can be approved.");
        }

        FoundItem item = foundItemDao.findById(claim.getItemId())
                .orElseThrow(() -> new ItemNotFoundException(claim.getItemId()));

        if (item.getStatus() == ItemStatus.RETURNED) {
            throw new InvalidStatusTransitionException("Cannot approve claim #" + claimId + " because item #" + item.getId() + " has already been marked as RETURNED.");
        }

        boolean success = claimDao.approveClaimAtomic(claimId, claim.getItemId());
        if (success) {
            logger.info("Claim #" + claimId + " approved atomically for item #" + claim.getItemId());
        }
        return success;
    }

    /**
     * Rejects a pending claim.
     *
     * @param claimId ID of the claim to reject
     * @return true if successfully rejected
     */
    public boolean rejectClaim(Integer claimId) {
        Claim claim = claimDao.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new InvalidStatusTransitionException("Cannot reject claim #" + claimId + " because its current status is " + claim.getStatus() + ". Only PENDING claims can be rejected.");
        }

        boolean success = claimDao.rejectClaim(claimId);
        if (success) {
            logger.info("Claim #" + claimId + " has been rejected.");

            // Check if there are any remaining active claims for this item
            List<Claim> remainingClaims = claimDao.findByItemId(claim.getItemId());
            boolean hasOtherPending = remainingClaims.stream().anyMatch(c -> c.getStatus() == ClaimStatus.PENDING);
            boolean hasApproved = remainingClaims.stream().anyMatch(c -> c.getStatus() == ClaimStatus.APPROVED);

            if (!hasOtherPending && !hasApproved) {
                // Restore item status back to AVAILABLE
                foundItemDao.updateStatus(claim.getItemId(), ItemStatus.AVAILABLE);
                logger.info("Restored item #" + claim.getItemId() + " status back to AVAILABLE as no pending or approved claims remain.");
            }
        }
        return success;
    }
}
