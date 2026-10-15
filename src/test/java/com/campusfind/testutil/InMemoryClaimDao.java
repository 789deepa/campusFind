package com.campusfind.testutil;

import com.campusfind.dao.ClaimDao;
import com.campusfind.model.Claim;
import com.campusfind.model.ClaimStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory test double for {@link ClaimDao}.
 */
public class InMemoryClaimDao implements ClaimDao {

    private final Map<Integer, Claim> claims = new LinkedHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    private final InMemoryFoundItemDao itemDao;
    private InMemoryLostItemDao lostItemDao;

    public InMemoryClaimDao(InMemoryFoundItemDao itemDao) {
        this.itemDao = itemDao;
    }

    public InMemoryClaimDao(InMemoryFoundItemDao itemDao, InMemoryLostItemDao lostItemDao) {
        this.itemDao = itemDao;
        this.lostItemDao = lostItemDao;
    }

    public void setLostItemDao(InMemoryLostItemDao lostItemDao) {
        this.lostItemDao = lostItemDao;
    }

    @Override
    public Claim save(Claim claim) {
        if (claim.getId() == null) {
            claim.setId(idGenerator.getAndIncrement());
        }
        claims.put(claim.getId(), copy(claim));
        return claim;
    }

    @Override
    public Optional<Claim> findById(Integer id) {
        return Optional.ofNullable(claims.get(id)).map(this::copy);
    }

    @Override
    public List<Claim> findByItemId(Integer itemId) {
        return claims.values().stream()
                .filter(c -> Objects.equals(c.getItemId(), itemId))
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public List<Claim> findPendingClaims() {
        return claims.values().stream()
                .filter(c -> c.getStatus() == ClaimStatus.PENDING)
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public List<Claim> findAll() {
        return claims.values().stream().map(this::copy).collect(Collectors.toList());
    }

    @Override
    public boolean hasActiveClaim(Integer itemId, String claimantContact) {
        return claims.values().stream()
                .anyMatch(c -> Objects.equals(c.getItemId(), itemId) &&
                               c.getClaimantContact().equalsIgnoreCase(claimantContact.trim()) &&
                               c.getStatus() == ClaimStatus.PENDING);
    }

    @Override
    public boolean updateStatus(Integer id, ClaimStatus status) {
        Claim claim = claims.get(id);
        if (claim != null) {
            claim.setStatus(status);
            claim.setReviewedAt(LocalDateTime.now());
            return true;
        }
        return false;
    }

    @Override
    public boolean approveClaimAtomic(Integer claimId, Integer itemId) {
        Claim claim = claims.get(claimId);
        if (claim == null || claim.getStatus() != ClaimStatus.PENDING) {
            return false;
        }

        // 1. Approve target claim
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setReviewedAt(LocalDateTime.now());

        // 2. If associated with lost report, update lost item status to MATCHED
        if (claim.getLostReportId() != null && lostItemDao != null) {
            lostItemDao.updateStatus(claim.getLostReportId(), com.campusfind.model.LostItemStatus.MATCHED);
        }

        // 3. Reject competing pending claims for the same item
        for (Claim other : claims.values()) {
            if (Objects.equals(other.getItemId(), itemId) &&
                !Objects.equals(other.getId(), claimId) &&
                other.getStatus() == ClaimStatus.PENDING) {
                other.setStatus(ClaimStatus.REJECTED);
                other.setReviewedAt(LocalDateTime.now());
            }
        }

        return true;
    }

    @Override
    public boolean rejectClaim(Integer claimId) {
        return updateStatus(claimId, ClaimStatus.REJECTED);
    }

    private Claim copy(Claim o) {
        return new Claim(o.getId(), o.getItemId(), o.getLostReportId(), o.getClaimantName(), o.getClaimantContact(),
                o.getClaimDescription(), o.getVerificationResponse(), o.getStatus(),
                o.getSubmittedAt(), o.getReviewedAt());
    }
}
