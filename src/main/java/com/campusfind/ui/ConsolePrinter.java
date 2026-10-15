package com.campusfind.ui;

import com.campusfind.model.Claim;
import com.campusfind.model.DashboardStatistics;
import com.campusfind.model.FoundItem;
import com.campusfind.util.DateUtil;

import java.util.List;

/**
 * Utility for formatting and printing console outputs, tables, and alert boxes.
 * 
 * <p><b>Privacy-First Verification Rule:</b>
 * The public item list and general claim list strictly omit secret clues and answers.
 * Only the designated verification review screen displays them side-by-side.
 * </p>
 */
public final class ConsolePrinter {

    private ConsolePrinter() {}

    public static void printBanner() {
        System.out.println("==========================================================================================");
        System.out.println("   ____                                  _____ _           _ ");
        System.out.println("  / ___|__ _ _ __ ___  _ __  _   _ ___  |  ___(_)_ __   __| |");
        System.out.println(" | |   / _` | '_ ` _ \\| '_ \\| | | / __| | |_  | | '_ \\ / _` |");
        System.out.println(" | |__| (_| | | | | | | |_) | |_| \\__ \\ |  _| | | | | | (_| |");
        System.out.println("  \\____\\__,_|_| |_| |_| .__/ \\__,_|___/ |_|   |_|_| |_|\\__,_|");
        System.out.println("                      |_|                                    ");
        System.out.println("   Privacy-First Campus Lost & Found Management System");
        System.out.println("==========================================================================================");
    }

    public static void printMenu() {
        System.out.println("\n-------------------------------- MENU OPTIONS --------------------------------");
        System.out.println(" 1. Report a Found Item");
        System.out.println(" 2. List Available / Open Items");
        System.out.println(" 3. Search Items by Name or Keyword");
        System.out.println(" 4. Filter Items by Category");
        System.out.println(" 5. Filter Items by Location");
        System.out.println(" 6. Submit a Claim for an Item");
        System.out.println(" 7. Review Pending Claims (Privacy-Verified Review)");
        System.out.println(" 8. Approve or Reject a Claim");
        System.out.println(" 9. Mark Item as Returned (Handover to Owner)");
        System.out.println(" 10. Display Statistics Dashboard");
        System.out.println(" 11. View All History & Audit Logs");
        System.out.println(" 12. Exit");
        System.out.println("------------------------------------------------------------------------------");
        System.out.print("Please select an option (1-12): ");
    }

    public static void printItemList(List<FoundItem> items, String title) {
        System.out.println("\n=== " + title + " (" + items.size() + " items found) ===");
        if (items.isEmpty()) {
            System.out.println("No items match the criteria.");
            return;
        }

        String format = "| %-4s | %-28s | %-13s | %-22s | %-10s | %-13s |%n";
        String separator = "+------+------------------------------+---------------+------------------------+------------+---------------+";

        System.out.println(separator);
        System.out.printf(format, "ID", "Item Name", "Category", "Location", "Date Found", "Status");
        System.out.println(separator);

        for (FoundItem item : items) {
            System.out.printf(format,
                    item.getId(),
                    truncate(item.getItemName(), 28),
                    truncate(item.getCategory(), 13),
                    truncate(item.getLocationFound(), 22),
                    DateUtil.formatDate(item.getDateFound()),
                    item.getStatus().getDisplayName());
        }
        System.out.println(separator);
        System.out.println("NOTE: Private verification clues are confidential and hidden from public view.");
    }

    public static void printClaimList(List<Claim> claims, String title) {
        System.out.println("\n=== " + title + " (" + claims.size() + " claims) ===");
        if (claims.isEmpty()) {
            System.out.println("No claims found.");
            return;
        }

        String format = "| %-4s | %-7s | %-20s | %-25s | %-10s | %-19s |%n";
        String separator = "+------+---------+----------------------+---------------------------+------------+---------------------+";

        System.out.println(separator);
        System.out.printf(format, "CID", "Item ID", "Claimant Name", "Contact", "Status", "Submitted At");
        System.out.println(separator);

        for (Claim c : claims) {
            System.out.printf(format,
                    c.getId(),
                    c.getItemId(),
                    truncate(c.getClaimantName(), 20),
                    truncate(c.getClaimantContact(), 25),
                    c.getStatus().name(),
                    DateUtil.formatDateTime(c.getSubmittedAt()));
        }
        System.out.println(separator);
    }

    public static void printVerificationReviewCard(FoundItem item, Claim claim) {
        System.out.println("\n====================== PRIVACY-VERIFIED CLAIM REVIEW ======================");
        System.out.println(" Claim ID          : #" + claim.getId() + " | Status: " + claim.getStatus());
        System.out.println(" Target Item ID    : #" + item.getId() + " (" + item.getItemName() + ")");
        System.out.println(" Item Category     : " + item.getCategory());
        System.out.println(" Location Found    : " + item.getLocationFound());
        System.out.println(" Date Found        : " + DateUtil.formatDate(item.getDateFound()));
        System.out.println(" Reported By       : " + item.getReportedBy());
        System.out.println(" Public Description: " + item.getDescription());
        System.out.println("----------------------------------------------------------------------------");
        System.out.println(" Claimant Name     : " + claim.getClaimantName());
        System.out.println(" Claimant Contact  : " + claim.getClaimantContact());
        System.out.println(" Claimant Story    : " + claim.getClaimDescription());
        System.out.println(" Submitted At      : " + DateUtil.formatDateTime(claim.getSubmittedAt()));
        System.out.println("------------------------- CONFIDENTIAL COMPARISON -------------------------");
        System.out.println(" [!] Finder's Secret Clue : " + item.getPrivateVerificationClue());
        System.out.println(" [?] Claimant's Answer    : " + claim.getVerificationResponse());
        System.out.println("============================================================================");
    }

    public static void printDashboard(DashboardStatistics stats) {
        System.out.println("\n======================= CAMPUSFIND LIVE STATISTICS =======================");
        System.out.println(" Total Items Reported          : " + stats.getTotalItemsReported());
        System.out.println("   * Currently Available       : " + stats.getAvailableItems());
        System.out.println("   * Pending Claims Underway   : " + stats.getClaimPendingItems());
        System.out.println("   * Returned to Owners        : " + stats.getReturnedItems());
        System.out.println("--------------------------------------------------------------------------");
        System.out.println(" Total Claims Filed            : " + stats.getTotalClaims());
        System.out.println("   * Pending Review            : " + stats.getPendingClaims());
        System.out.println("   * Approved                  : " + stats.getApprovedClaims());
        System.out.println("   * Rejected                  : " + stats.getRejectedClaims());
        System.out.println("--------------------------------------------------------------------------");
        System.out.println(" Most Frequent Found Location  : " + stats.getTopLocation());
        System.out.println("==========================================================================");
    }

    public static void printSuccess(String message) {
        System.out.println("\n[SUCCESS] " + message);
    }

    public static void printError(String message) {
        System.out.println("\n[ERROR] " + message);
    }

    public static void printWarning(String message) {
        System.out.println("\n[WARNING] " + message);
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }
}
