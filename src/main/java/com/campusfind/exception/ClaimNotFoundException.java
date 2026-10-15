package com.campusfind.exception;

/**
 * Thrown when an operation references a claim ID that does not exist.
 */
public class ClaimNotFoundException extends CampusFindException {
    private final Integer claimId;

    public ClaimNotFoundException(Integer claimId) {
        super("Claim with ID " + claimId + " was not found.");
        this.claimId = claimId;
    }

    public Integer getClaimId() {
        return claimId;
    }
}
