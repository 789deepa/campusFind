package com.campusfind.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a student's claim against a found item.
 * 
 * <p><b>Privacy-First Design Principle:</b>
 * The claimant provides a {@code verificationResponse} matching the item's secret clue.
 * This response is kept private and hidden in {@link #toString()} to prevent accidental leaks.
 * </p>
 */
public class Claim {
    private Integer id;
    private Integer itemId;
    private String claimantName;
    private String claimantContact;
    private String claimDescription;
    private String verificationResponse;
    private ClaimStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    public Claim() {
        this.status = ClaimStatus.PENDING;
        this.submittedAt = LocalDateTime.now();
    }

    public Claim(Integer itemId, String claimantName, String claimantContact,
                 String claimDescription, String verificationResponse) {
        this.itemId = itemId;
        this.claimantName = claimantName;
        this.claimantContact = claimantContact;
        this.claimDescription = claimDescription;
        this.verificationResponse = verificationResponse;
        this.status = ClaimStatus.PENDING;
        this.submittedAt = LocalDateTime.now();
    }

    public Claim(Integer id, Integer itemId, String claimantName, String claimantContact,
                 String claimDescription, String verificationResponse, ClaimStatus status,
                 LocalDateTime submittedAt, LocalDateTime reviewedAt) {
        this.id = id;
        this.itemId = itemId;
        this.claimantName = claimantName;
        this.claimantContact = claimantContact;
        this.claimDescription = claimDescription;
        this.verificationResponse = verificationResponse;
        this.status = status != null ? status : ClaimStatus.PENDING;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getClaimantName() {
        return claimantName;
    }

    public void setClaimantName(String claimantName) {
        this.claimantName = claimantName;
    }

    public String getClaimantContact() {
        return claimantContact;
    }

    public void setClaimantContact(String claimantContact) {
        this.claimantContact = claimantContact;
    }

    public String getClaimDescription() {
        return claimDescription;
    }

    public void setClaimDescription(String claimDescription) {
        this.claimDescription = claimDescription;
    }

    /**
     * Access the private response provided by the claimant.
     * To be used strictly during reviewer verification.
     */
    public String getVerificationResponse() {
        return verificationResponse;
    }

    public void setVerificationResponse(String verificationResponse) {
        this.verificationResponse = verificationResponse;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Claim claim)) return false;
        return Objects.equals(id, claim.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Claim{" +
                "id=" + id +
                ", itemId=" + itemId +
                ", claimantName='" + claimantName + '\'' +
                ", claimantContact='" + claimantContact + '\'' +
                ", claimDescription='" + claimDescription + '\'' +
                ", verificationResponse='[REDACTED]'" +
                ", status=" + status +
                ", submittedAt=" + submittedAt +
                ", reviewedAt=" + reviewedAt +
                '}';
    }
}
