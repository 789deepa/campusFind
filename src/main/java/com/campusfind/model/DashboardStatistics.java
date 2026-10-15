package com.campusfind.model;

/**
 * Data Transfer Object (DTO) holding aggregated statistics for the campus dashboard.
 */
public class DashboardStatistics {
    private final int totalItemsReported;
    private final int availableItems;
    private final int claimPendingItems;
    private final int returnedItems;
    private final int totalClaims;
    private final int pendingClaims;
    private final int approvedClaims;
    private final int rejectedClaims;
    private final String topLocation;

    public DashboardStatistics(int totalItemsReported, int availableItems,
                               int claimPendingItems, int returnedItems,
                               int totalClaims, int pendingClaims,
                               int approvedClaims, int rejectedClaims,
                               String topLocation) {
        this.totalItemsReported = totalItemsReported;
        this.availableItems = availableItems;
        this.claimPendingItems = claimPendingItems;
        this.returnedItems = returnedItems;
        this.totalClaims = totalClaims;
        this.pendingClaims = pendingClaims;
        this.approvedClaims = approvedClaims;
        this.rejectedClaims = rejectedClaims;
        this.topLocation = topLocation != null ? topLocation : "N/A";
    }

    public int getTotalItemsReported() {
        return totalItemsReported;
    }

    public int getAvailableItems() {
        return availableItems;
    }

    public int getClaimPendingItems() {
        return claimPendingItems;
    }

    public int getReturnedItems() {
        return returnedItems;
    }

    public int getTotalClaims() {
        return totalClaims;
    }

    public int getPendingClaims() {
        return pendingClaims;
    }

    public int getApprovedClaims() {
        return approvedClaims;
    }

    public int getRejectedClaims() {
        return rejectedClaims;
    }

    public String getTopLocation() {
        return topLocation;
    }

    @Override
    public String toString() {
        return "DashboardStatistics{" +
                "totalItems=" + totalItemsReported +
                ", available=" + availableItems +
                ", pendingClaimsItems=" + claimPendingItems +
                ", returned=" + returnedItems +
                ", totalClaims=" + totalClaims +
                ", pendingClaims=" + pendingClaims +
                ", approvedClaims=" + approvedClaims +
                ", rejectedClaims=" + rejectedClaims +
                ", topLocation='" + topLocation + '\'' +
                '}';
    }
}
