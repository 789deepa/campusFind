package com.campusfind.model;

/**
 * Data Transfer Object (DTO) holding aggregated statistics for the campus dashboard,
 * including both found-item metrics and lost-item report statistics.
 */
public class DashboardStatistics {
    // Found Items
    private final int totalItemsReported;
    private final int availableItems;
    private final int claimPendingItems;
    private final int returnedItems;

    // Claims
    private final int totalClaims;
    private final int pendingClaims;
    private final int approvedClaims;
    private final int rejectedClaims;

    // Lost Items
    private final int totalLostItems;
    private final int openLostItems;
    private final int matchedLostItems;
    private final int resolvedLostItems;

    private final String topLocation;

    /**
     * Backward-compatible constructor for found items and claims.
     */
    public DashboardStatistics(int totalItemsReported, int availableItems,
                               int claimPendingItems, int returnedItems,
                               int totalClaims, int pendingClaims,
                               int approvedClaims, int rejectedClaims,
                               String topLocation) {
        this(totalItemsReported, availableItems, claimPendingItems, returnedItems,
             totalClaims, pendingClaims, approvedClaims, rejectedClaims,
             0, 0, 0, 0, topLocation);
    }

    /**
     * Comprehensive constructor with lost-item metrics.
     */
    public DashboardStatistics(int totalItemsReported, int availableItems,
                               int claimPendingItems, int returnedItems,
                               int totalClaims, int pendingClaims,
                               int approvedClaims, int rejectedClaims,
                               int totalLostItems, int openLostItems,
                               int matchedLostItems, int resolvedLostItems,
                               String topLocation) {
        this.totalItemsReported = totalItemsReported;
        this.availableItems = availableItems;
        this.claimPendingItems = claimPendingItems;
        this.returnedItems = returnedItems;
        this.totalClaims = totalClaims;
        this.pendingClaims = pendingClaims;
        this.approvedClaims = approvedClaims;
        this.rejectedClaims = rejectedClaims;
        this.totalLostItems = totalLostItems;
        this.openLostItems = openLostItems;
        this.matchedLostItems = matchedLostItems;
        this.resolvedLostItems = resolvedLostItems;
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

    public int getTotalLostItems() {
        return totalLostItems;
    }

    public int getOpenLostItems() {
        return openLostItems;
    }

    public int getMatchedLostItems() {
        return matchedLostItems;
    }

    public int getResolvedLostItems() {
        return resolvedLostItems;
    }

    public String getTopLocation() {
        return topLocation;
    }

    @Override
    public String toString() {
        return "DashboardStatistics{" +
                "totalFound=" + totalItemsReported +
                ", available=" + availableItems +
                ", pendingClaimsItems=" + claimPendingItems +
                ", returned=" + returnedItems +
                ", totalLost=" + totalLostItems +
                ", openLost=" + openLostItems +
                ", matchedLost=" + matchedLostItems +
                ", resolvedLost=" + resolvedLostItems +
                ", totalClaims=" + totalClaims +
                ", pendingClaims=" + pendingClaims +
                ", approvedClaims=" + approvedClaims +
                ", rejectedClaims=" + rejectedClaims +
                ", topLocation='" + topLocation + '\'' +
                '}';
    }
}
